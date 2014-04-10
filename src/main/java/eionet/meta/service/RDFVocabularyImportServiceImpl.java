/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */

package eionet.meta.service;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyRDFImportHandler;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Service implementation to import RDF into a Vocabulary Folder.
 *
 * @author enver
 */
@Service
public class RDFVocabularyImportServiceImpl extends VocabularyImportServiceBaseImpl implements IRDFVocabularyImportService {

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public List<String> importRdfIntoVocabulary(Reader contents, final VocabularyFolder vocabularyFolder,
            boolean purgeVocabularyData, boolean purgePredicateBasis) throws ServiceException {
        long start = System.currentTimeMillis();
        this.logMessages = new ArrayList<String>();

        final String folderCtxRoot = VocabularyFolder.getBaseUri(vocabularyFolder);

        List<VocabularyConcept> concepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        final List<DataElement> bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        if (purgeVocabularyData) {
            String message = "All concepts ";
            purgeConcepts(concepts);
            concepts = new ArrayList<VocabularyConcept>();
            message += "are deleted (with purge operation).";
            this.logMessages.add(message);
        }

        Map<String, Integer> elemToId = new HashMap<String, Integer>();
        Map<String, List<String>> bindedElemsByNS = new HashMap<String, List<String>>();
        for (DataElement elem : bindedElements) {
            String identifier = elem.getIdentifier();

            // TODO use dataelem getNameSpacePrefix and isExternalElement
            String[] temp = identifier.split("[:]");

            if (temp.length != 2) {
                continue;
            }

            if (StringUtils.isNotEmpty(identifier)) {
                elemToId.put(identifier, elem.getId());
            }

            List<String> domainElements = bindedElemsByNS.get(temp[0]);
            if (domainElements == null) {
                domainElements = new ArrayList<String>();
                bindedElemsByNS.put(temp[0], domainElements);
            }
            domainElements.add(temp[1]);
        }

        RDFParser parser = new RDFXMLParser();
        VocabularyRDFImportHandler rdfHandler =
                new VocabularyRDFImportHandler(folderCtxRoot, concepts, elemToId, bindedElemsByNS, purgePredicateBasis,
                        Props.getProperty(PropsIF.DD_WORKING_LANGUAGE_KEY));
        parser.setRDFHandler(rdfHandler);
        // parser.setStopAtFirstError(false);
        ParserConfig config = parser.getParserConfig();
        if (config == null) {
            config = new ParserConfig();
        }
        config.addNonFatalError(BasicParserSettings.DATATYPE_HANDLERS);
        config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
        // config.addNonFatalError();
        parser.setParserConfig(config);
        final List<String> errorLogMessages = new ArrayList<String>();
        parser.setParseErrorListener(new ParseErrorListener() {
            @Override
            public void warning(String arg0, int arg1, int arg2) {
                errorLogMessages.add("Warning: " + arg0);
            }

            @Override
            public void fatalError(String arg0, int arg1, int arg2) {
                // TODO to throw ServiceException or not to throw?
                errorLogMessages.add("Fatal Error: " + arg0);
            }

            @Override
            public void error(String arg0, int arg1, int arg2) {
                errorLogMessages.add("Error: " + arg0);
            }
        });

        try {
            parser.parse(contents, folderCtxRoot);
            this.logMessages.addAll(rdfHandler.getLogMessages());
            this.logMessages.add("Number of errors received: " + errorLogMessages.size());
            importIntoDb(vocabularyFolder.getId(), rdfHandler.getToBeUpdatedConcepts(), new ArrayList<DataElement>(),
                    rdfHandler.getElementsRelatedToNotCreatedConcepts());
        } catch (Exception e) {
            // all exceptions should cause rollback operation
            throw new ServiceException(e.getMessage());
        }

        this.logMessages.add("RDF imported to database.");
        long end = System.currentTimeMillis();
        this.logMessages.add("Total time of execution (msecs): " + (end - start));

        return this.logMessages;
    } // end of method importRdfIntoVocabulary

} // end of class RDFVocabularyImportServiceImpl
