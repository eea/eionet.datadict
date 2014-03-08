package eionet.meta.service;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyRDFImportHandler;
import eionet.meta.service.data.ObsoleteStatus;
import org.apache.commons.lang.StringUtils;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
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
public class RDFVocabularyImportServiceImpl implements IRDFVocabularyImportService {

    /**
     * Vocabulary service.
     */
    @Autowired
    private IVocabularyService vocabularyService;

    /**
     * Data elements service.
     */
    @Autowired
    private IDataService dataService;

    /**
     * Log message list.
     */
    private List<String> logMessages = null;


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

        this.logMessages.add("Number of found concepts: " + concepts.size()
                + ", number of binded elements: " + bindedElements.size());

        RDFParser parser = new RDFXMLParser();
        VocabularyRDFImportHandler rdfHandler = new VocabularyRDFImportHandler(folderCtxRoot, concepts, bindedElemsByNS, elemToId, purgePredicateBasis);
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
                errorLogMessages.add("Fatal Error: " + arg0);
            }

            @Override
            public void error(String arg0, int arg1, int arg2) {
                errorLogMessages.add("Error: " + arg0);
            }
        });

        try {
            parser.parse(contents, folderCtxRoot);
            //TODO handle error log messages and handler messages
            this.logMessages.addAll(rdfHandler.getLogs());
            long importStart = System.currentTimeMillis();

            final List<VocabularyConcept> toBeUpdatedConcepts = rdfHandler.getToBeUpdatedConcepts();
            this.logMessages.add("Number of concepts to be updated: " + toBeUpdatedConcepts.size());
            importIntoDb(vocabularyFolder.getId(), toBeUpdatedConcepts, new ArrayList<DataElement>());
            long importEnd = System.currentTimeMillis();
            this.logMessages.add("Import time (msecs): " + (importEnd - importStart));
        } catch (RDFParseException e) {
            this.logMessages.add("Exception Received: " + e.getMessage());
            e.printStackTrace();
        } catch (RDFHandlerException e) {
            this.logMessages.add("Exception Received: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            this.logMessages.add("Exception Received: " + e.getMessage());
            e.printStackTrace();
        }

        this.logMessages.add("RDF imported to database.");
        long end = System.currentTimeMillis();
        this.logMessages.add("Total time for execution (msecs): " + (end - start));

        return this.logMessages;
    } // end of method importCsvIntoVocabulary

    /**
     * Purge/delete concepts from database.
     *
     * @param concepts to be deleted
     * @throws ServiceException if an error occurs during operation
     */
    private void purgeConcepts(List<VocabularyConcept> concepts) throws ServiceException {
        List<Integer> conceptIds = new ArrayList<Integer>();

        if (concepts != null && concepts.size() > 0) {
            for (VocabularyConcept vc : concepts) {
                conceptIds.add(vc.getId());
            }
            this.vocabularyService.deleteVocabularyConcepts(conceptIds);
        }
    } // end of method purgeConcepts

    //TODO copied pasted code refactor

    /**
     * Purge/delete binded elements from vocabulary folder.
     *
     * @param vocabularyFolderId id of vocabulary folder
     * @param bindedElements     binded elements
     * @throws ServiceException if an error occurs during operation
     */
    private void purgeBindedElements(int vocabularyFolderId, List<DataElement> bindedElements) throws ServiceException {
        if (bindedElements != null && bindedElements.size() > 0) {
            for (DataElement elem : bindedElements) {
                this.vocabularyService.removeDataElement(vocabularyFolderId, elem.getId());
            }
        }
    } // end of method purgeBindedElements

    /**
     * This method import objects into DB. It creates not-existing objects and then updates values.
     * All operation is done Spring Service Layer.
     *
     * @param vocabularyId       vocabulary id
     * @param vocabularyConcepts concepts of vocabulary
     * @param newBindedElement   newly binded elements
     * @return
     * @throws ServiceException when an error occurs
     */
    private void importIntoDb(int vocabularyId, List<VocabularyConcept> vocabularyConcepts, List<DataElement> newBindedElement)
            throws ServiceException {
        // first of all insert new binded element
        for (DataElement elem : newBindedElement) {
            this.vocabularyService.addDataElement(vocabularyId, elem.getId());
        }

        for (VocabularyConcept vc : vocabularyConcepts) {
            // STEP 1., UPDATE OR INSERT VOCABULARY CONCEPT
            if (vc.getId() <= 0) {
                // INSERT VOCABULARY CONCEPT
                int insertedId = this.vocabularyService.createVocabularyConcept(vocabularyId, vc);
                // after insert operation get id of the vocabulary and set it!
                vc.setId(insertedId);
            }

            // UPDATE VOCABULARY CONCEPT
            this.vocabularyService.updateVocabularyConceptNonTransactional(vc);
        }
    } // end of method importIntoDb

} // end of class CSVVocabularyImport
