package eionet.meta.service;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
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
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.VocabularyCSVOutputHelper;

@Service
public class CSVVocabularyImportServiceImpl implements ICSVVocabularyImportService {

    /** Vocabulary service. */
    @Autowired
    private IVocabularyService vocabularyService;

    /** Data elements service. */
    @Autowired
    private IDataService dataService;

    @Override
    @Transactional
    public void importCsvIntoVocabulary(Reader content, VocabularyFolder vocabularyFolder, boolean purgeVocabularyData)
            throws ServiceException {

        List<VocabularyConcept> concepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        List<DataElement> bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        if (purgeVocabularyData) {
            purgeConceptsAndBindedElements(vocabularyFolder.getId(), concepts, bindedElements);
            concepts = new ArrayList<VocabularyConcept>();
            bindedElements = new ArrayList<DataElement>();
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

        List<DataElement> newBindedElement = new ArrayList<DataElement>();

        // TODO if headers includes elements other than associated dataelements then there will be a problem!!!
        List<VocabularyConcept> foundConcepts =
                generateUpdatedBeans(content, folderContextRoot, concepts, elementToId, newBindedElement);

        boolean result = importIntoDb(foundConcepts, vocabularyFolder.getId(), newBindedElement);

        if (result == false) {
            throw new ServiceException("Import operation failed");
        }
    }// end of method importCsvIntoVocabulary

    /**
     *
     * @param vocabularyFolderId
     * @param concepts
     * @param bindedElements
     * @throws ServiceException
     */
    private void purgeConceptsAndBindedElements(int vocabularyFolderId, List<VocabularyConcept> concepts,
            List<DataElement> bindedElements) throws ServiceException {
        List<Integer> conceptIds = new ArrayList<Integer>();

        for (VocabularyConcept vc : concepts) {
            conceptIds.add(vc.getId());
        }
        this.vocabularyService.deleteVocabularyConcepts(conceptIds);

        for (DataElement elem : bindedElements) {
            this.vocabularyService.removeDataElement(vocabularyFolderId, elem.getId());
        }

    }// end of method purgeConceptsAndBindedElements

    /**
     *
     * @param content
     * @param vocabularyFolder
     * @param concepts
     * @return
     * @throws ServiceException
     */
    private List<VocabularyConcept> generateUpdatedBeans(Reader content, String folderContextRoot,
            List<VocabularyConcept> concepts, Map<String, Integer> bindedElementsToFolder, List<DataElement> newBindedElement)
            throws ServiceException {
        List<VocabularyConcept> foundConcepts = new ArrayList<VocabularyConcept>();

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
                throw new ServiceException("Missing headers!");
            }

            int i = 0;
            for (i = VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT; i < header.length; i++) {
                String elementHeader = header[i];
                String[] tempStrArray = elementHeader.split("[@]");
                if (tempStrArray.length == 2) {
                    elementHeader = tempStrArray[0];
                }
                if (!bindedElementsToFolder.containsKey(elementHeader)) {
                    DataElementsFilter elementsFilter = new DataElementsFilter();
                    elementsFilter.setRegStatus("Released");
                    elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
                    elementsFilter.setIdentifier(elementHeader);
                    DataElementsResult elementsResult = dataService.searchDataElements(elementsFilter);
                    if (elementsResult.getTotalResults() != 1) {
                        throw new ServiceException("Cannot find single data element for column: " + elementHeader);
                    } else {
                        // this.vocabularyService.addDataElement(vocabularyFolder.getId(), elementId);
                        DataElement elem = elementsResult.getDataElements().get(0);
                        if (StringUtils.equals(elementHeader, elem.getIdentifier())) {
                            bindedElementsToFolder.put(elementHeader, elementsResult.getDataElements().get(0).getId());
                            newBindedElement.add(elem);
                        } else {
                            throw new ServiceException("Found data element does not EXACTLY match with column: " + elementHeader
                                    + ", found: " + elem.getIdentifier());
                        }
                    }
                }
            }

            // TODO relational and purge
            String[] lineParams;
            i = 2;
            while ((lineParams = reader.readNext()) != null) {

                if (lineParams.length == header.length) {
                    // do line processing
                    String uri = lineParams[0];

                    if (StringUtils.isEmpty(uri) || StringUtils.startsWith(uri, "//")
                            || !StringUtils.startsWith(uri, folderContextRoot)) {
                        continue;
                    }

                    String identifier = uri.replace(folderContextRoot, "");
                    if (!StringUtils.contains(identifier, "/")) {
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
                            for (j = 0; j < foundConcepts.size(); j++) {
                                VocabularyConcept vc = foundConcepts.get(j);
                                if (StringUtils.equals(identifier, vc.getIdentifier())) {
                                    break;
                                }
                            }
                            if (j == foundConcepts.size()) {
                                // if there is already such a concept, ignore that line. if not, add a new concept with params.
                                found = new VocabularyConcept();

                                found.setIdentifier(identifier);
                                // TODO set other properties
                                List<List<DataElement>> newConceptElementAttributes = new ArrayList<List<DataElement>>();
                                found.setElementAttributes(newConceptElementAttributes);
                            }
                        }

                        if (found != null) {
                            foundConcepts.add(found);

                            found.setLabel(lineParams[1]);
                            found.setDefinition(lineParams[2]);
                            found.setNotation(lineParams[3]);
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
                                            VocabularyCSVOutputHelper.getDataElementValuesByName(elementHeader,
                                                    found.getElementAttributes());

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
                                    // TODO what if user wanted to deleted a relatinal concept
                                    if (StringUtils.contains(lineParams[k], folderContextRoot)) { // it is a relational element
                                        String relatedConceptIdentifier = lineParams[k].replace(folderContextRoot, "");
                                        // identifier should not contain extra slashes, if it does, then it is not valid!
                                        if (!StringUtils.contains(relatedConceptIdentifier, "/")) {
                                            if (!elem.isRelationalElement()
                                                    || !StringUtils.equals(elem.getRelatedConceptIdentifier(),
                                                            relatedConceptIdentifier)) {
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

                        }// end of block when vocabulary concept found or created (if it is duplicated with another row, importer
                         // will ignore repeating rows)

                    }// end of if identifier matches
                }// end of if line has same number of columns with header

                i++;
            }// end of row iterator (while loop on rows)
            i--;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }

        return foundConcepts;
    }// end of method generatedUpdatedBeans

    /**
     *
     * @param vocabularyConcepts
     * @param vocabularyId
     * @return
     * @throws ServiceException
     */
    private boolean importIntoDb(List<VocabularyConcept> vocabularyConcepts, int vocabularyId, List<DataElement> newBindedElement)
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
        return true;
    }// end of method importIntoDb

}// end of class CSVVocabularyImport
