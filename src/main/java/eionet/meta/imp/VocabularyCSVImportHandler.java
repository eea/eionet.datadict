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

package eionet.meta.imp;

import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;

import au.com.bytecode.opencsv.CSVReader;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.util.VocabularyCSVOutputHelper;

/**
 * Includes code for parsing and handling CSV lines.
 *
 * @author enver
 */
@Configurable
public class VocabularyCSVImportHandler extends VocabularyImportBaseHandler {

    /**
     * URI prefix to check related concept.
     */
    private static final String URI_PREFIX = "http://";

    /**
     * CSV file reader.
     */
    private Reader content;

    /**
     *
     * @param folderContextRoot
     *            base uri for vocabulary.
     * @param concepts
     *            concepts of vocabulary
     * @param bindedElements
     *            binded elements to vocabulary
     * @param content
     *            reader to read file contents
     */
    public VocabularyCSVImportHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> bindedElements, Reader content) {
        super(folderContextRoot, concepts, bindedElements);
        this.content = content;
    }

    /**
     * In this method, beans are generated (either created or updated) according to values in CSV file.
     *
     * @throws eionet.meta.service.ServiceException
     *             if there is the input is invalid
     */
    public void generateUpdatedBeans() throws ServiceException {
        // content.
        CSVReader reader = new CSVReader(this.content);
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
                if (!this.bindedElementsIds.containsKey(elementHeader)) {
                    // search for data element
                    DataElementsFilter elementsFilter = new DataElementsFilter();
                    elementsFilter.setRegStatus("Released");
                    elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
                    elementsFilter.setIdentifier(elementHeader);
                    elementsFilter.setIncludeHistoricVersions(false);
                    elementsFilter.setExactIdentifierMatch(true);
                    DataElementsResult elementsResult = this.dataService.searchDataElements(elementsFilter);
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
                            this.bindedElementsIds.put(elementHeader, elementsResult.getDataElements().get(0).getId());
                            this.newBindedElement.add(elem);
                        } else {
                            throw new ServiceException("Found data element does not EXACTLY match with column: " + elementHeader
                                    + ", found: " + elem.getIdentifier());
                        }
                    }
                }
            } // end of for loop iterating on headers

            String[] lineParams;
            // first row is header so start from 2
            for (int rowNumber = 2; (lineParams = reader.readNext()) != null; rowNumber++) {
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
                    this.logMessages.add("Row (" + rowNumber + ") is skipped (Base URI is empty).");
                    continue;
                } else if (StringUtils.startsWith(uri, "//")) {
                    this.logMessages
                            .add("Row (" + rowNumber + ") is skipped (Concept is excluded by user from update operation).");
                    continue;
                } else if (!StringUtils.startsWith(uri, this.folderContextRoot)) {
                    this.logMessages.add("Row (" + rowNumber + ") is skipped (Base URI does not match with Vocabulary).");
                    continue;
                }

                String conceptIdentifier = uri.replace(this.folderContextRoot, "");
                if (StringUtils.contains(conceptIdentifier, "/")) {
                    this.logMessages.add("Row (" + rowNumber + ") does not contain a valid concept identifier.");
                    continue;
                } // end of if identifier matches

                // now we have a valid row
                VocabularyConcept lastFoundConcept = findOrCreateConcept(conceptIdentifier);

                // if vocabulary concept duplicated with another row, importer will ignore it not to repeat
                if (lastFoundConcept == null) {
                    this.logMessages.add("Row (" + rowNumber + ") duplicates with a previous concept, it is skipped.");
                    continue;
                }

                // vocabulary concept found or created
                this.toBeUpdatedConcepts.add(lastFoundConcept);

                lastFoundConcept.setLabel(StringUtils.trimToNull(lineParams[1]));
                lastFoundConcept.setDefinition(StringUtils.trimToNull(lineParams[2]));
                lastFoundConcept.setNotation(StringUtils.trimToNull(lineParams[3]));

                if (StringUtils.isNotEmpty(lineParams[4])) {
                    lastFoundConcept.setCreated(dateFormatter.parse(lineParams[4]));
                }
                if (StringUtils.isNotEmpty(lineParams[5])) {
                    lastFoundConcept.setObsolete(dateFormatter.parse(lineParams[5]));
                }

                // now it is time iterate on rest of the columns, here is the tricky part
                Map<String, Integer> attributePosition = new HashMap<String, Integer>();
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
                        elementsOfConcept = getDataElementValuesByName(elementHeader, lastFoundConcept.getElementAttributes());
                        if (elementsOfConcept == null) {
                            elementsOfConcept = new ArrayList<DataElement>();
                            lastFoundConcept.getElementAttributes().add(elementsOfConcept);
                        }
                    }

                    if (!StringUtils.equals(elementHeader, prevHeader) || !StringUtils.equals(lang, prevLang)) {
                        elementsOfConceptByLang =
                                getDataElementValuesByNameAndLang(elementHeader, lang, lastFoundConcept.getElementAttributes());
                    }

                    if (!attributePosition.containsKey(header[k])) {
                        attributePosition.put(header[k], 0);
                    }
                    int index = attributePosition.get(header[k]);

                    // if lineParams[k] is empty, user wants to delete
                    if (StringUtils.isNotEmpty(lineParams[k])) {
                        DataElement elem;
                        if (index < elementsOfConceptByLang.size()) {
                            elem = elementsOfConceptByLang.get(index);
                        } else {
                            elem = new DataElement();
                            elementsOfConcept.add(elem);
                            elem.setAttributeLanguage(lang);
                            elem.setIdentifier(elementHeader);
                            elem.setId(this.bindedElementsIds.get(elementHeader));
                        }

                        VocabularyConcept foundRelatedConcept = null;
                        // TODO what if user wanted to deleted a relational concept
                        if (StringUtils.startsWith(lineParams[k], URI_PREFIX)) {
                            // it can be a related concept
                            try {
                                URL relatedConceptURL = new URL(lineParams[k]);
                                foundRelatedConcept = findRelatedConcept(relatedConceptURL.toString());
                            } catch (MalformedURLException e) {
                                // it is not a valid url so we don't accept it as a related concept identifier
                                e.printStackTrace();
                            }
                        }
                        if (foundRelatedConcept != null) {
                            elem.setRelatedConceptIdentifier(foundRelatedConcept.getIdentifier());
                            elem.setRelatedConceptId(foundRelatedConcept.getId());
                        } else {
                            elem.setAttributeValue(lineParams[k]);
                        }
                    } else {
                        // if it is empty and if there is such a value then delete it, if there is no value just ignore it
                        if (index < elementsOfConceptByLang.size()) {
                            DataElement elem = elementsOfConceptByLang.get(index);
                            elem.setAttributeValue(lineParams[k]); // so if it is empty, it means delete it right ?
                        }
                    }
                    attributePosition.put(header[k], ++index);

                    prevLang = lang;
                    prevHeader = elementHeader;
                } // end of for loop iterating on rest of the columns (for data elements)

            } // end of row iterator (while loop on rows)

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // end of method generatedUpdatedBeans

} // end of class VocabularyCSVImportHandler
