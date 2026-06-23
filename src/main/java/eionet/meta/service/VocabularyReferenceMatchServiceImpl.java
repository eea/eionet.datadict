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

import javax.annotation.PostConstruct;

import eionet.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IVocabularyFolderDAO vocabularyFolderDAO;
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDAO;
    @Autowired
    private IDataElementDAO dataElementDAO;

    private Map<String, ReferenceMatchJobChunk> jobInstanceMap;

    @PostConstruct
    public void init() {
        jobInstanceMap = new HashMap<>();
        jobInstanceMap.put(MatchPotentialReferringElementValues.JOB_IDENTIFIER,
                new MatchPotentialReferringElementValues(vocabularyFolderDAO, vocabularyConceptDAO, dataElementDAO));
    }

    /**
     * {@inheritDoc}
     */
    // @Transactional(rollbackFor = ServiceException.class)
    @Override
    public List<String> matchReferences(String[] jobIdentifiers) {
        List<String> logs = new ArrayList<>();

        if (jobIdentifiers != null) {
            for (String jobChunkIdentifier : jobIdentifiers) {
                ReferenceMatchJobChunk jobChunk = jobInstanceMap.get(jobChunkIdentifier);
                if (jobChunk != null) {
                    logs.add("Starting " + jobChunkIdentifier);
                    List<String> internalLogs = jobChunk.execute();
                    logs.addAll(internalLogs);
                } else {
                    logs.add("No implementation set for " + jobChunkIdentifier);
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
    public static class MatchPotentialReferringElementValues implements ReferenceMatchJobChunk {
        /**
         * Job identifier.
         */
        public static final String JOB_IDENTIFIER = "match:References";

        private final IVocabularyFolderDAO vocabularyFolderDAO;
        private final IVocabularyConceptDAO vocabularyConceptDAO;
        private final IDataElementDAO dataElementDAO;

        public MatchPotentialReferringElementValues(IVocabularyFolderDAO vocabularyFolderDAO,
                IVocabularyConceptDAO vocabularyConceptDAO, IDataElementDAO dataElementDAO) {
            this.vocabularyFolderDAO = vocabularyFolderDAO;
            this.vocabularyConceptDAO = vocabularyConceptDAO;
            this.dataElementDAO = dataElementDAO;
        }

        @Override
        public List<String> execute() {
            int numberOfMatchedElementValuesToIds = 0;
            List<String> logs = new ArrayList<>();
            List<DataElement> elements = this.dataElementDAO.getPotentialReferringVocabularyConceptsElements();
            for (DataElement elem : elements) {
                String elemValue = elem.getAttributeValue();
                String baseUri = elem.getRelatedConceptBaseURI();
                String conceptIdentifier = StringUtils.trimToEmpty(StringUtils.replace(elemValue, baseUri, ""));
                if (Util.isValidIdentifier(conceptIdentifier) && !StringUtils.contains(conceptIdentifier, "/")) {
                    // now hope we have a valid concept here, search for it.
                    VocabularyConceptFilter filter = new VocabularyConceptFilter();
                    // !!! ATTENTION: this is really dependent to implementation of getPotentialReferringVocabularyConceptsElements
                    filter.setVocabularyFolderId(elem.getRelatedConceptId());
                    filter.setIdentifier(conceptIdentifier);
                    filter.setUsePaging(false);
                    VocabularyConceptResult vocabularyConceptResult = this.vocabularyConceptDAO.searchVocabularyConcepts(filter);
                    // continue if and only if one item found
                    List<VocabularyConcept> concepts = vocabularyConceptResult.getList();
                    if (concepts.size() == 1) {
                        VocabularyConcept concept = vocabularyConceptResult.getList().get(0);
                        elem.setRelatedConceptId(concept.getId());
                        String tempElementValue = elem.getAttributeValue();
                        elem.setAttributeValue(null);
                        // update it
                        this.vocabularyFolderDAO.updateRelatedConceptValueToId(elem);
                        numberOfMatchedElementValuesToIds++;
                        logs.add("Vocabulary Concept Element (which has ID \"" + elem.getId()
                                + "\" and is an attribute of concept with id \"" + elem.getVocabularyConceptId()
                                + "\") is updated. \"" + tempElementValue + "\" is replaced by \"" + conceptIdentifier
                                + "\" (with concept id \"" + concept.getId() + "\") in vocabulary \""
                                + concept.getVocabularyIdentifier() + "\"");
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

} // end of class RDFVocabularyImportServiceImpl
