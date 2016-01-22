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

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyRDFImportHandler;
import eionet.util.Props;
import eionet.util.PropsIF;
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

import java.io.Reader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation to import RDF into a Vocabulary Folder.
 *
 * @author enver
 */
@Service
public class RDFVocabularyImportServiceImpl extends VocabularyImportServiceBaseImpl implements IRDFVocabularyImportService {

    /**
     * Supported RDF upload action before.
     */
    private static final List<UploadActionBefore> SUPPORTED_ACTION_BEFORE;

    /**
     * Supported RDF upload action.
     */
    private static final List<UploadAction> SUPPORTED_ACTION;

    /**
     * Supported RDF upload missing concepts action.
     */
    private static final List<MissingConceptsAction> SUPPORTED_MISSING_CONCEPTS_ACTION;

    /**
     * Static block for static data.
     */
    static {
        //Add RDF upload params
        SUPPORTED_ACTION_BEFORE = new ArrayList<UploadActionBefore>();
        SUPPORTED_ACTION_BEFORE.add(UploadActionBefore.keep);
        SUPPORTED_ACTION_BEFORE.add(UploadActionBefore.remove);

        SUPPORTED_ACTION = new ArrayList<UploadAction>();
        SUPPORTED_ACTION.add(UploadAction.add);
        SUPPORTED_ACTION.add(UploadAction.delete);

        SUPPORTED_MISSING_CONCEPTS_ACTION = new ArrayList<MissingConceptsAction>();
        SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.keep);
        SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.remove);
        SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.invalid);
        SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.deprecated);
        SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.retired);
        SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.superseded);
    } // end of static block

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UploadActionBefore> getSupportedActionBefore(boolean isApiCall) {
        return new ArrayList<UploadActionBefore>(SUPPORTED_ACTION_BEFORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UploadActionBefore getDefaultActionBefore(boolean isApiCall) {
        return UploadActionBefore.keep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UploadAction> getSupportedAction(boolean isApiCall) {
        return new ArrayList<UploadAction>(SUPPORTED_ACTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UploadAction getDefaultAction(boolean isApiCall) {
        return UploadAction.add;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MissingConceptsAction> getSupportedMissingConceptsAction(boolean isApiCall) {
        return new ArrayList<MissingConceptsAction>(SUPPORTED_MISSING_CONCEPTS_ACTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MissingConceptsAction getDefaultMissingConceptsAction(boolean isApiCall) {
        return MissingConceptsAction.keep;
    }

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
    public List<String> importRdfIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder,
                                                UploadActionBefore uploadActionBefore, UploadAction uploadAction,
                                                MissingConceptsAction missingConceptsAction) throws ServiceException {
        long start = System.currentTimeMillis();
        this.logMessages = new ArrayList<String>();

        final String folderCtxRoot = VocabularyFolder.getBaseUri(vocabularyFolder);

        //check for valid base uri
        if (!Util.isValidUri(folderCtxRoot)) {
            throw new ServiceException("Vocabulary does not have a valid base URI");
        }

        List<VocabularyConcept> concepts = vocabularyService.getAcceptedConceptsWithAttributes(vocabularyFolder.getId());

        final List<DataElement> boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        if (UploadActionBefore.remove.equals(uploadActionBefore)) {
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
        //perge per predicate basis option
        boolean createNewDataElementsForPredicates = UploadAction.add_and_purge_per_predicate_basis.equals(uploadAction);
        VocabularyRDFImportHandler rdfHandler =
                new VocabularyRDFImportHandler(folderCtxRoot, new ArrayList<VocabularyConcept>(concepts),
                        elemToId, boundElementsByNS, boundURIs,
                        createNewDataElementsForPredicates,
                        Props.getProperty(PropsIF.DD_WORKING_LANGUAGE_KEY), DD_NAME_SPACE);
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

            List<VocabularyConcept> toBeUpdatedConcepts = rdfHandler.getToBeUpdatedConcepts();
            List<VocabularyConcept> missingConcepts = rdfHandler.getMissingConcepts();
            List<VocabularyConcept> toBeRemovedConcepts = new ArrayList<VocabularyConcept>();

            switch (missingConceptsAction) {
                case remove:
                    this.logMessages.add("Missing concepts will be removed");
                    toBeRemovedConcepts = missingConcepts;
                    break;
                case invalid:
                case deprecated:
                case retired:
                case superseded:
                    StandardGenericStatus conceptStatus = getStatusForMissingConceptAction(missingConceptsAction);
                    if (conceptStatus != null) {
                        this.logMessages.add("Missing concepts status are changed to: " + conceptStatus);
                        boolean notAcceptedSubStatus = conceptStatus.isSubStatus(StandardGenericStatus.NOT_ACCEPTED);
                        boolean acceptedSubStatus = conceptStatus.isSubStatus(StandardGenericStatus.ACCEPTED);
                        for (VocabularyConcept vc : missingConcepts) {
                            StandardGenericStatus initialStatus = vc.getStatus();
                            vc.setStatus(conceptStatus);
                            vc.setStatusModified(new Date(System.currentTimeMillis()));

                            if (notAcceptedSubStatus && (initialStatus == null || initialStatus.isSubStatus(StandardGenericStatus.ACCEPTED))) {
                                vc.setNotAcceptedDate(new Date(System.currentTimeMillis()));
                            } else if (acceptedSubStatus && (initialStatus == null || initialStatus.isSubStatus(StandardGenericStatus.NOT_ACCEPTED))) {
                                vc.setAcceptedDate(new Date(System.currentTimeMillis()));
                            }
                        }

                        //TODO can be checked for duplicate items.
                        toBeUpdatedConcepts.addAll(missingConcepts);
                    }
                    break;
                case keep:
                default:
                    //do no thing
                    break;
            }

            this.logMessages.add("Number of (uploaded) RDF File errors received from RDF Parser: " + errorLogMessages.size());
            importIntoDb(vocabularyFolder.getId(), toBeUpdatedConcepts, toBeRemovedConcepts,
                    new ArrayList<DataElement>(), rdfHandler.getElementsRelatedToNotCreatedConcepts());
        } catch (Exception e) {
            // all exceptions should cause rollback operation
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }

        this.logMessages.add("RDF imported to database.");
        long end = System.currentTimeMillis();
        this.logMessages.add("Total time of execution (msecs): " + (end - start));

        return this.logMessages;
    } // end of method importRdfIntoVocabulary

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> importRdfIntoVocabulary(Reader contents, final VocabularyFolder vocabularyFolder,
                                                boolean purgeVocabularyData, boolean purgePredicateBasis) throws ServiceException {

        UploadActionBefore uploadActionBefore;
        if (purgeVocabularyData) {
            uploadActionBefore = UploadActionBefore.remove;
        } else {
            uploadActionBefore = getDefaultActionBefore(false);
        }

        UploadAction uploadAction;
        if (purgePredicateBasis) {
            uploadAction = UploadAction.add_and_purge_per_predicate_basis;
        } else {
            uploadAction = getDefaultAction(false);
        }

        MissingConceptsAction missingConceptsAction = getDefaultMissingConceptsAction(false);

        return importRdfIntoVocabulary(contents, vocabularyFolder, uploadActionBefore, uploadAction, missingConceptsAction);
    } // end of method importRdfIntoVocabulary

} // end of class RDFVocabularyImportServiceImpl
