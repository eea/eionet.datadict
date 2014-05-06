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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;

/**
 * Service implementation to match references in vocabularies.
 *
 * @author enver
 */
@Service
public class VocabularyReferenceMatchServiceImpl implements IVocabularyReferenceMatchService {

    /**
     * Data elem to job map.
     */
    private static Map<String, Class<? extends ReferenceMatchJobChunk>> dataElementsJobMatchMap =
            new HashMap<String, Class<? extends ReferenceMatchJobChunk>>();
    // static block to populate dataElementsJobMatchMap
    static {
        dataElementsJobMatchMap.put(SkosExactMatchDataElement.DATA_ELEMENT_IDENTIFIER, SkosExactMatchDataElement.class);
        dataElementsJobMatchMap.put(OwlSameAsDataElement.DATA_ELEMENT_IDENTIFIER, OwlSameAsDataElement.class);

    } // end of static block

    /**
     * {@inheritDoc}
     */
    // @Transactional(rollbackFor = ServiceException.class)
    @Override
    public List<String> matchReferences(String[] dataElementIdentifiers) throws ServiceException {
        List<String> logs = new ArrayList<String>();
        // STEP 1. First try to match concepts which may have links for other concepts.
        ReferenceMatchJobChunk referringElementValues = new MatchPotentialReferringElementValues();
        List<String> internalLogs = referringElementValues.execute();
        logs.addAll(internalLogs);

        if (dataElementIdentifiers != null && dataElementIdentifiers.length > 0) {
            for (String dataElemIdentifier : dataElementIdentifiers) {
                Class<? extends ReferenceMatchJobChunk> clazz = dataElementsJobMatchMap.get(dataElemIdentifier);
                if (clazz != null) {
                    try {
                        ReferenceMatchJobChunk jobChunk = clazz.newInstance();
                        internalLogs = jobChunk.execute();
                        logs.addAll(internalLogs);
                    } catch (InstantiationException e) {
                        logs.add("Cannot create job chunk for " + dataElemIdentifier);
                        logs.add(e.getMessage());
                    } catch (IllegalAccessException e) {
                        logs.add("Cannot create job chunk for " + dataElemIdentifier);
                        logs.add(e.getMessage());
                    }
                } else {
                    logs.add("No implementation set for " + dataElemIdentifier);
                }
            }
        }
        return logs;
    } // end of method matchReferences

    /**
     * An interface for job parts for reference match.
     */
    protected interface ReferenceMatchJobChunk {
        /**
         * This method should be implemented by sub classes.
         *
         * @return a list of strings for logging purposes.
         */
        List<String> execute();
    } // end of inner interface MatchJobChunk


    /**
     * Job chunk to convert element values to related concept ids.
     */
    @Configurable
    protected static class MatchPotentialReferringElementValues implements ReferenceMatchJobChunk {
        /**
         * Vocabulary folder DAO.
         */
        @Autowired
        private IVocabularyFolderDAO vocabularyFolderDAO;
        /**
         * Vocabulary concept DAO.
         */
        @Autowired
        private IVocabularyConceptDAO vocabularyConceptDAO;
        /**
         * Data element DAO.
         */
        @Autowired
        private IDataElementDAO dataElementDAO;

        @Override
        public List<String> execute() {
            int numberOfMatchedElementValuesToIds = 0;
            List<String> logs = new ArrayList<String>();
            List<DataElement> elements = this.dataElementDAO.getPotentialReferringVocabularyConceptsElements();
            for (DataElement elem : elements) {
                String elemValue = elem.getAttributeValue();
                String baseUri = elem.getRelatedConceptBaseURI();
                String conceptIdentifier = StringUtils.trimToEmpty(StringUtils.replace(elemValue, baseUri, ""));
                if (StringUtils.isNotEmpty(conceptIdentifier) && !StringUtils.contains(conceptIdentifier, "/")) {
                    // now hope we have a valid concept here, search for it.
                    VocabularyConceptFilter filter = new VocabularyConceptFilter();
                    // !!! ATTENTION: this is really dependent getPotentialReferringVocabularyConceptsElements method implementation
                    filter.setVocabularyFolderId(elem.getRelatedConceptId());
                    filter.setIdentifier(conceptIdentifier);
                    filter.setUsePaging(false);
                    VocabularyConceptResult vocabularyConceptResult = this.vocabularyConceptDAO.searchVocabularyConcepts(filter);
                    // continue if and only if one item found
                    List<VocabularyConcept> concepts = vocabularyConceptResult.getList();
                    if (concepts.size() == 1) {
                        VocabularyConcept concept = vocabularyConceptResult.getList().get(0);
                        elem.setRelatedConceptId(concept.getId());
                        elem.setAttributeValue(null);
                        // now update it
                        this.vocabularyFolderDAO.updateRelatedConceptValueToId(elem);
                        numberOfMatchedElementValuesToIds++;
                    }
                }
            }
            if (numberOfMatchedElementValuesToIds > 0) {
                logs.add("Number of vocabulary concept elements updated to have related concept id from element value: "
                        + numberOfMatchedElementValuesToIds);
            }
            return logs;
        } // end of method onExecute
    } // end of inner class MatchPotentialReferringElementValues

    /**
     * Inner class to match "skos:exactMatch" elements.
     */
    protected static class SkosExactMatchDataElement implements ReferenceMatchJobChunk {
        /**
         * Data element identifier.
         */
        protected static final String DATA_ELEMENT_IDENTIFIER = "skos:exactMatch";

        @Override
        public List<String> execute() {
            List<String> logs = new ArrayList<String>();
            logs.add(DATA_ELEMENT_IDENTIFIER + " job executed.");
            return logs;
        } // end of method onExecute
    } // end of inner class SkosExactMatchDataElement

    /**
     * Inner class to match "owl:sameAs" elements.
     */
    @Configurable
    protected static class OwlSameAsDataElement implements ReferenceMatchJobChunk {
        /**
         * Data element identifier.
         */
        protected static final String DATA_ELEMENT_IDENTIFIER = "owl:sameAs";
        /**
         * Data element DAO.
         */
        @Autowired
        private IDataElementDAO dataElementDAO;

        @Override
        public List<String> execute() {
            List<String> logs = new ArrayList<String>();
            DataElement element = this.dataElementDAO.getDataElement(DATA_ELEMENT_IDENTIFIER);
            logs.add("Id of " + DATA_ELEMENT_IDENTIFIER + " is " + element.getId());
            logs.add(DATA_ELEMENT_IDENTIFIER + " job executed.");
            return logs;
        } // end of method onExecute
    } // end of inner class OwlSameAsDataElement

} // end of class RDFVocabularyImportServiceImpl
