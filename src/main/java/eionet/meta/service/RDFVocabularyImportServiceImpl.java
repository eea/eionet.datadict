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

import eionet.util.Util;
import org.apache.commons.lang.StringUtils;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyRDFImportHandler;
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
     * Namespace service.
     */
    @Autowired
    private INamespaceService namespaceService;
    /**
     * DD Namespace.
     */
    public static final String DD_NAME_SPACE = Props.getRequiredProperty(PropsIF.DD_URL) + "/property/";

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

        //check for valid base uri
        if (!Util.isValidUri(folderCtxRoot)) {
            throw new ServiceException("Vocabulary does not have a valid base URI");
        }

        List<VocabularyConcept> concepts = vocabularyService.getValidConceptsWithAttributes(vocabularyFolder.getId());

        final List<DataElement> boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        if (purgeVocabularyData) {
            String message = "All concepts ";
            purgeConcepts(concepts);
            concepts = new ArrayList<VocabularyConcept>();
            message += "are deleted (with purge operation).";
            this.logMessages.add(message);
        }

        List<RdfNamespace> rdfNamespaceList = this.namespaceService.getRdfNamespaces();

        Map<String, String> boundURIs = new HashMap<String, String>();
        Map<String, Integer> elemToId = new HashMap<String, Integer>();
        Map<String, List<String>> boundElementsByNS = new HashMap<String, List<String>>();
        //add default DD namespace to lists
        boundURIs.put(DD_NAME_SPACE, DD_NAME_SPACE);
        boundElementsByNS.put(DD_NAME_SPACE, new ArrayList<String>());
        for (DataElement elem : boundElements) {
            String identifier = elem.getIdentifier();

            // TODO use dataelem getNameSpacePrefix and isExternalElement
            String[] temp = identifier.split("[:]");

            String namespace = DD_NAME_SPACE;
            String namespacePrefix = identifier;
            if (temp.length == 2) {
                namespace = temp[0];
                namespacePrefix = temp[1];
            }

            if (StringUtils.isNotEmpty(identifier)) {
                elemToId.put(identifier, elem.getId());
            }

            List<String> domainElements = boundElementsByNS.get(namespace);
            if (domainElements == null) {
                domainElements = new ArrayList<String>();
                boundElementsByNS.put(namespace, domainElements);

                RdfNamespace rns = null;
                for (int i = 0; i < rdfNamespaceList.size(); i++) {
                    rns = rdfNamespaceList.get(i);
                    if (StringUtils.equals(rns.getPrefix(), namespace)) {
                        break;
                    }
                }
                if (rns != null) {
                    rdfNamespaceList.remove(rns);
                    boundURIs.put(rns.getUri(), rns.getPrefix());
                }
            }
            domainElements.add(namespacePrefix);
        }

        RDFParser parser = new RDFXMLParser();
        VocabularyRDFImportHandler rdfHandler =
                new VocabularyRDFImportHandler(folderCtxRoot, concepts, elemToId, boundElementsByNS, boundURIs,
                        purgePredicateBasis, Props.getProperty(PropsIF.DD_WORKING_LANGUAGE_KEY), DD_NAME_SPACE);
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
            this.logMessages.add("Number of (uploaded) RDF File errors received from RDF Parser: " + errorLogMessages.size());
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
