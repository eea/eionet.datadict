package eionet.meta.service;

import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.com.bytecode.opencsv.CSVReader;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.util.Pair;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.VocabularyCSVOutputHelper;

/**
 *
 * Service implementation to import CSV into a Vocabulary Folder.
 *
 * @author enver
 */
@Service
public class CSVVocabularyImportServiceImpl implements ICSVVocabularyImportService {

    /** Vocabulary service. */
    @Autowired
    private IVocabularyService vocabularyService;

    /** Data elements service. */
    @Autowired
    private IDataService dataService;

    private List<String> logMessages = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public List<String> importCsvIntoVocabulary(Reader content, VocabularyFolder vocabularyFolder, boolean purgeVocabularyData,
            boolean purgeBoundedElements) throws ServiceException {

        this.logMessages = new ArrayList<String>();
        List<VocabularyConcept> concepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        List<DataElement> bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        if (purgeVocabularyData) {
            String message = "All concepts ";
            purgeConcepts(concepts);
            concepts = new ArrayList<VocabularyConcept>();
            if (purgeBoundedElements) {
                purgeBindedElements(vocabularyFolder.getFolderId(), bindedElements);
                bindedElements = new ArrayList<DataElement>();
                message += "and bounded elements ";
            }
            message += "are deleted (with purge operation).";
            this.logMessages.add(message);
        }

        Map<String, Integer> elementToId = new HashMap<String, Integer>();
        for (DataElement elem : bindedElements) {
            String identifier = elem.getIdentifier();
            if (StringUtils.isNotEmpty(identifier)) {
                elementToId.put(identifier, elem.getId());
            }
        }

        final String folderContextRoot =
                StringUtils.isNotEmpty(vocabularyFolder.getBaseUri()) ? vocabularyFolder.getBaseUri() : Props
                        .getRequiredProperty(PropsIF.DD_URL)
                        + "/vocabulary/"
                        + vocabularyFolder.getFolderName()
                        + "/"
                        + vocabularyFolder.getIdentifier() + "/";

        Pair<List<VocabularyConcept>, List<DataElement>> willBeUpdatedObjects =
                generateUpdatedBeans(content, folderContextRoot, concepts, elementToId);

        importIntoDb(vocabularyFolder.getId(), willBeUpdatedObjects.getLeft(), willBeUpdatedObjects.getRight());
        this.logMessages.add("CSV imported into Database.");

        return this.logMessages;
    }// end of method importCsvIntoVocabulary

    /**
     * Purge/delete concepts from database
     *
     * @param concepts
     *            to be deleted
     * @throws ServiceException
     *             if an error occurs during operation
     */
    private void purgeConcepts(List<VocabularyConcept> concepts) throws ServiceException {
        List<Integer> conceptIds = new ArrayList<Integer>();

        if (concepts != null && concepts.size() > 0) {
            for (VocabularyConcept vc : concepts) {
                conceptIds.add(vc.getId());
            }
            this.vocabularyService.deleteVocabularyConcepts(conceptIds);
        }
    }// end of method purgeConcepts

    /**
     * Purge/delete binded elements from vocabulary folder
     *
     * @param vocabularyFolderId
     *            id of vocabulary folder
     * @param bindedElements
     *            binded elements
     * @throws ServiceException
     *             if an error occurs during operation
     */
    private void purgeBindedElements(int vocabularyFolderId, List<DataElement> bindedElements) throws ServiceException {
        if (bindedElements != null && bindedElements.size() > 0) {
            for (DataElement elem : bindedElements) {
                this.vocabularyService.removeDataElement(vocabularyFolderId, elem.getId());
            }
        }
    }// end of method purgeBindedElements

    /**
     * In this method, beans are generated (either created or updated) according to values in CSV file.
     *
     * @param content
     *            reader to read file contents
     * @param vocabularyFolder
     *            folder under bulk edit
     * @param concepts
     *            founded concepts before import operation
     * @return generated beans(concepts and dataelements) for update operation
     * @throws ServiceException
     *             if there is the input is invalid
     */
    private Pair<List<VocabularyConcept>, List<DataElement>> generateUpdatedBeans(Reader content, String folderContextRoot,
            List<VocabularyConcept> concepts, Map<String, Integer> bindedElementsToFolder) throws ServiceException {

        List<VocabularyConcept> toBeUpdatedConcepts = new ArrayList<VocabularyConcept>();
        List<DataElement> newBindedElement = new ArrayList<DataElement>();
        Pair<List<VocabularyConcept>, List<DataElement>> returnPair =
                new Pair<List<VocabularyConcept>, List<DataElement>>(toBeUpdatedConcepts, newBindedElement);

        // content.
        CSVReader reader = new CSVReader(content);
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            String[] header = reader.readNext();
            // first check if headers contains fix columns

            String[] fixedHeaders = new String[VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT];
            VocabularyCSVOutputHelper.addFixedEntryHeaders(fixedHeaders);

            // compare contents
            boolean isEqual = Arrays.equals(fixedHeaders, Arrays.copyOf(header, VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT));

            if (!isEqual) {
                reader.close();
                throw new ServiceException("Missing headers! CSV file should contain following headers: "
                        + Arrays.toString(fixedHeaders));
            }

            for (int i = VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT; i < header.length; i++) {

                String elementHeader = header[i];
                if (StringUtils.isEmpty(elementHeader)) {
                    throw new ServiceException("Header for column (" + (i + 1) + ") is empty!");
                }

                // if there is language appended, split it
                String[] tempStrArray = elementHeader.split("[@]");
                if (tempStrArray.length == 2) {
                    elementHeader = tempStrArray[0];
                }

                // if already binded elements does not contain header, add it (if possible)
                if (!bindedElementsToFolder.containsKey(elementHeader)) {
                    // search for data element
                    DataElementsFilter elementsFilter = new DataElementsFilter();
                    elementsFilter.setRegStatus("Released");
                    elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
                    elementsFilter.setIdentifier(elementHeader);
                    DataElementsResult elementsResult = dataService.searchDataElements(elementsFilter);
                    // if there is one and only one element check if header and identifer exactly matches!
                    if (elementsResult.getTotalResults() < 1) {
                        throw new ServiceException("Cannot find any data element for column: " + elementHeader
                                + ". Please bind element manually then upload CSV.");
                    } else if (elementsResult.getTotalResults() > 1) {
                        throw new ServiceException("Cannot find single data element for column: " + elementHeader
                                + ". Search returns: " + elementsResult.getTotalResults()
                                + " elements. Please bind element manually then upload CSV.");
                    } else {
                        DataElement elem = elementsResult.getDataElements().get(0);
                        if (StringUtils.equals(elementHeader, elem.getIdentifier())) {
                            // found it, add to list and map
                            bindedElementsToFolder.put(elementHeader, elementsResult.getDataElements().get(0).getId());
                            newBindedElement.add(elem);
                        } else {
                            throw new ServiceException("Found data element does not EXACTLY match with column: " + elementHeader
                                    + ", found: " + elem.getIdentifier());
                        }
                    }
                }
            }// end of for loop iterating on headers

            String[] lineParams;
            for (int rowNumber = 2; (lineParams = reader.readNext()) != null; rowNumber++) {// first row is header so start from 2
                if (lineParams.length != header.length) {
                    StringBuilder message = new StringBuilder();
                    message.append("Row (").append(rowNumber).append(") ");
                    message.append("does not have same number of columns with header, it is skipped.");
                    message.append(" It should have have same number of columns (empty or filled).");
                    this.logMessages.add(message.toString());
                    continue;
                }

                // do line processing
                String uri = lineParams[0];

                if (StringUtils.isEmpty(uri)) {
                    this.logMessages.add("Row (" + rowNumber + ") is skipped (Base URI is empty).\n");
                    continue;
                } else if (StringUtils.startsWith(uri, "//")) {
                    this.logMessages.add("Row (" + rowNumber
                            + ") is skipped (Concept is excluded by user from update operation).\n");
                    continue;
                } else if (!StringUtils.startsWith(uri, folderContextRoot)) {
                    this.logMessages.add("Row (" + rowNumber + ") is skipped (Base URI does not match with Vocabulary).\n");
                    continue;
                }

                String identifier = uri.replace(folderContextRoot, "");
                if (StringUtils.contains(identifier, "/")) {
                    this.logMessages.add("Row (" + rowNumber + ") does not contain a valid concept identifier.");
                    continue;
                }// end of if identifier matches

                // now we have a valid row
                int j = 0;
                for (j = 0; j < concepts.size(); j++) {
                    VocabularyConcept vc = concepts.get(j);
                    if (StringUtils.equals(identifier, vc.getIdentifier())) {
                        break;
                    }
                }

                VocabularyConcept found = null;
                if (j < concepts.size()) {// concept found
                    found = concepts.remove(j);
                } else {
                    for (j = 0; j < toBeUpdatedConcepts.size(); j++) {
                        VocabularyConcept vc = toBeUpdatedConcepts.get(j);
                        if (StringUtils.equals(identifier, vc.getIdentifier())) {
                            break;
                        }
                    }
                    if (j == toBeUpdatedConcepts.size()) {
                        // if there is already such a concept, ignore that line. if not, add a new concept with params.
                        found = new VocabularyConcept();

                        found.setIdentifier(identifier);
                        // TODO set other properties
                        List<List<DataElement>> newConceptElementAttributes = new ArrayList<List<DataElement>>();
                        found.setElementAttributes(newConceptElementAttributes);
                    }
                }

                // if vocabulary concept duplicated with another row, importer will ignore it not to repeat
                if (found == null) {
                    this.logMessages.add("Row (" + rowNumber + ") duplicates with a previous concept, it is skipped.");
                    continue;
                }

                // vocabulary concept found or created
                toBeUpdatedConcepts.add(found);

                found.setLabel(StringUtils.trimToNull(lineParams[1]));
                found.setDefinition(StringUtils.trimToNull(lineParams[2]));
                found.setNotation(StringUtils.trimToNull(lineParams[3]));
                // TODO if it is not a valid date, then it can be skippped actually
                if (StringUtils.isNotEmpty(lineParams[4])) {
                    found.setCreated(dateFormatter.parse(lineParams[4]));
                }
                if (StringUtils.isNotEmpty(lineParams[5])) {
                    found.setObsolete(dateFormatter.parse(lineParams[5]));
                }

                // now it is time iterate on rest of the columns, here is the tricky part
                HashMap<String, Integer> attributePosition = new HashMap<String, Integer>();
                List<DataElement> elementsOfConcept = null;
                List<DataElement> elementsOfConceptByLang = null;
                String prevHeader = null;
                String prevLang = null;
                for (int k = VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT; k < lineParams.length; k++) {
                    String elementHeader = header[k];

                    String lang = null;
                    String[] tempStrArray = elementHeader.split("[@]");
                    if (tempStrArray.length == 2) {
                        elementHeader = tempStrArray[0];
                        lang = tempStrArray[1];
                    }

                    if (!StringUtils.equals(elementHeader, prevHeader)) {
                        elementsOfConcept =
                                VocabularyCSVOutputHelper.getDataElementValuesByName(elementHeader, found.getElementAttributes());

                        if (elementsOfConcept == null) {
                            elementsOfConcept = new ArrayList<DataElement>();
                            found.getElementAttributes().add(elementsOfConcept);
                        }
                    }

                    if (!StringUtils.equals(elementHeader, prevHeader) || !StringUtils.equals(lang, prevLang)) {
                        elementsOfConceptByLang =
                                VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang(elementHeader, lang,
                                        found.getElementAttributes());
                    }

                    if (!attributePosition.containsKey(header[k])) {
                        attributePosition.put(header[k], 0);
                    }
                    int index = attributePosition.get(header[k]);

                    // if lineParams[k] is empty, user wants to delete
                    if (StringUtils.isNotEmpty(lineParams[k])) {
                        DataElement elem = null;
                        if (index < elementsOfConceptByLang.size()) {
                            elem = elementsOfConceptByLang.get(index);
                        } else {
                            elem = new DataElement();
                            elementsOfConcept.add(elem);
                            elem.setAttributeLanguage(lang);
                            elem.setIdentifier(elementHeader);
                            elem.setId(bindedElementsToFolder.get(elementHeader));
                            // TODO set some properties here
                        }

                        // TODO what if it is a relational element, it makes things more complicated
                        // TODO what if user wanted to deleted a relational concept
                        if (StringUtils.contains(lineParams[k], folderContextRoot)) { // it is a relational element
                            String relatedConceptIdentifier = lineParams[k].replace(folderContextRoot, "");
                            // identifier should not contain extra slashes, if it does, then it is not valid!
                            if (!StringUtils.contains(relatedConceptIdentifier, "/")) {
                                if (!elem.isRelationalElement()
                                        || !StringUtils.equals(elem.getRelatedConceptIdentifier(), relatedConceptIdentifier)) {
                                    elem.setRelatedConceptIdentifier(relatedConceptIdentifier);
                                    elem.setRelatedConceptId(-1);
                                }
                            }
                        } else {
                            elem.setAttributeValue(lineParams[k]);
                        }
                    } else {// if it is empty and if there is such a value then delete it, if there is no value just
                            // ignore it
                        if (index < elementsOfConceptByLang.size()) {
                            DataElement elem = elementsOfConceptByLang.get(index);
                            elem.setAttributeValue(lineParams[k]); // so if it is empty, it means delete it right ?
                        }
                    }
                    attributePosition.put(header[k], ++index);

                    prevLang = lang;
                    prevHeader = elementHeader;
                }// end of for loop iterating on rest of the columns (for data elements)

            }// end of row iterator (while loop on rows)

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }

        return returnPair;
    }// end of method generatedUpdatedBeans

    /**
     * This method import objects into DB. It creates not-existing objects and then updates values.
     * All operation is done Spring Service Layer
     *
     * @param vocabularyConcepts
     * @param vocabularyId
     * @return
     * @throws ServiceException
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
            this.vocabularyService.updateVocabularyConcept(vc);
        }
    }// end of method importIntoDb

}// end of class CSVVocabularyImport
