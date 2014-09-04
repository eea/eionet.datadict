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

import au.com.bytecode.opencsv.CSVReader;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.util.Pair;
import eionet.util.Util;
import eionet.util.VocabularyCSVOutputHelper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Includes code for parsing and handling CSV lines.
 *
 * @author enver
 */
// @Configurable
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
     * Elements filter to be used in search.
     */
    private final DataElementsFilter elementsFilter;

    /**
     * @param folderContextRoot
     *            base uri for vocabulary.
     * @param concepts
     *            concepts of vocabulary
     * @param boundElements
     *            bound elements to vocabulary
     * @param content
     *            reader to read file contents
     */
    public VocabularyCSVImportHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> boundElements, Reader content) {
        super(folderContextRoot, concepts, boundElements);
        this.content = content;
        this.elementsFilter = new DataElementsFilter();
        this.elementsFilter.setRegStatus("Released");
        this.elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
        this.elementsFilter.setIncludeHistoricVersions(false);
        this.elementsFilter.setExactIdentifierMatch(true);
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

                // if bound elements do not contain header already, add it (if possible)
                if (!this.boundElementsIds.containsKey(elementHeader)) {
                    // search for data element
                    this.elementsFilter.setIdentifier(elementHeader);
                    DataElementsResult elementsResult = this.dataService.searchDataElements(this.elementsFilter);
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
                            this.boundElementsIds.put(elementHeader, elementsResult.getDataElements().get(0).getId());
                            this.newBoundElement.add(elem);
                        } else {
                            throw new ServiceException("Found data element did not EXACTLY match with column: " + elementHeader
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
                    message.append("did not have same number of columns with header, it was skipped.");
                    message.append(" It should have have same number of columns (empty or filled).");
                    this.logMessages.add(message.toString());
                    continue;
                }

                // do line processing
                String uri = lineParams[0];
                if (StringUtils.isEmpty(uri)) {
                    this.logMessages.add("Row (" + rowNumber + ") was skipped (Base URI was empty).");
                    continue;
                } else if (StringUtils.startsWith(uri, "//")) {
                    this.logMessages.add("Row (" + rowNumber
                            + ") was skipped (Concept was excluded by user from update operation).");
                    continue;
                } else if (!StringUtils.startsWith(uri, this.folderContextRoot)) {
                    this.logMessages.add("Row (" + rowNumber + ") was skipped (Base URI did not match with Vocabulary).");
                    continue;
                }

                String conceptIdentifier = uri.replace(this.folderContextRoot, "");
                if (StringUtils.contains(conceptIdentifier, "/") || !Util.isValidIdentifier(conceptIdentifier)) {
                    this.logMessages.add("Row (" + rowNumber + ") did not contain a valid concept identifier.");
                    continue;
                }

                // now we have a valid row
                Pair<VocabularyConcept, Boolean> foundConceptWithFlag = findOrCreateConcept(conceptIdentifier);

                // if vocabulary concept duplicated with another row, importer will ignore it not to repeat
                if (foundConceptWithFlag == null || foundConceptWithFlag.getRight()) {
                    this.logMessages.add("Row (" + rowNumber + ") duplicated with a previous concept, it was skipped.");
                    continue;
                }

                VocabularyConcept lastFoundConcept = foundConceptWithFlag.getLeft();
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
                    if (StringUtils.isEmpty(lineParams[k])) {
                        // value is empty, no need to proceed
                        continue;
                    }
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

                    prevLang = lang;
                    prevHeader = elementHeader;

                    VocabularyConcept foundRelatedConcept = null;
                    if (Util.isValidUri(lineParams[k])){
                        foundRelatedConcept = findRelatedConcept(lineParams[k]);
                    }

                    // check for pre-existence of the VCE by attribute value or related concept id
                    Integer relatedId = null;
                    if (foundRelatedConcept != null) {
                        relatedId = foundRelatedConcept.getId();
                    }
                    boolean returnFromThisPoint = false;
                    for (DataElement elemByLang : elementsOfConceptByLang) {
                        String elementValueByLang = elemByLang.getAttributeValue();
                        if (StringUtils.equals(lineParams[k], elementValueByLang)) {
                            // vocabulary concept element already in database, no need to continue, return
                            returnFromThisPoint = true;
                            break;
                        }

                        if (relatedId != null) {
                            Integer relatedConceptId = elemByLang.getRelatedConceptId();
                            if (relatedConceptId != null && relatedConceptId.intValue() == relatedId.intValue()) {
                                // vocabulary concept element already in database, no need to continue, return
                                returnFromThisPoint = true;
                                break;
                            }
                        }
                    }
                    // check if an existing VCE found or not
                    if (returnFromThisPoint) {
                        continue;
                    }

                    // create VCE
                    DataElement elem = new DataElement();
                    elementsOfConcept.add(elem);
                    elem.setAttributeLanguage(lang);
                    elem.setIdentifier(elementHeader);
                    elem.setId(this.boundElementsIds.get(elementHeader));
                    // check if there is a found related concept
                    if (foundRelatedConcept != null) {
                        elem.setRelatedConceptIdentifier(foundRelatedConcept.getIdentifier());
                        int id = foundRelatedConcept.getId();
                        elem.setRelatedConceptId(id);
                        elem.setAttributeValue(null);
                        if (id < 0) {
                            addToElementsReferringNotCreatedConcepts(id, elem);
                        }
                    } else {
                        elem.setAttributeValue(lineParams[k]);
                        elem.setRelatedConceptId(null);
                    }
                } // end of for loop iterating on rest of the columns (for data elements)
            } // end of row iterator (while loop on rows)
            processUnseenConceptsForRelatedElements();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } catch (RuntimeException e) {
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
