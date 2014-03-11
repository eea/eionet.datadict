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
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;

/**
 * Base abstract class used for vocabulary import from different sources (RDF or CSV).
 *
 * @author enver
 */
public abstract class VocabularyImportServiceBaseImpl {
    /**
     * Vocabulary service.
     */
    @Autowired
    protected IVocabularyService vocabularyService;

    /**
     * Data elements service.
     */
    @Autowired
    protected IDataService dataService;

    /**
     * Log message list.
     */
    protected List<String> logMessages = null;

    /**
     * Cannot be called out of package scope.
     */
    protected VocabularyImportServiceBaseImpl() {
    }

    /**
     * Purge/delete concepts from database.
     *
     * @param concepts
     *            to be deleted
     * @throws ServiceException
     *             if an error occurs during operation
     */
    protected void purgeConcepts(List<VocabularyConcept> concepts) throws ServiceException {
        List<Integer> conceptIds = new ArrayList<Integer>();

        if (concepts != null && concepts.size() > 0) {
            for (VocabularyConcept vc : concepts) {
                conceptIds.add(vc.getId());
            }
            this.vocabularyService.deleteVocabularyConcepts(conceptIds);
        }
    } // end of method purgeConcepts

    /**
     * Purge/delete binded elements from vocabulary folder.
     *
     * @param vocabularyFolderId
     *            id of vocabulary folder
     * @param bindedElements
     *            binded elements
     * @throws ServiceException
     *             if an error occurs during operation
     */
    protected void purgeBindedElements(int vocabularyFolderId, List<DataElement> bindedElements) throws ServiceException {
        if (bindedElements != null && bindedElements.size() > 0) {
            for (DataElement elem : bindedElements) {
                this.vocabularyService.removeDataElement(vocabularyFolderId, elem.getId());
            }
        }
    } // end of method purgeBindedElements

    /**
     * This method import objects into DB. It creates not-existing objects and then updates values. All operation is done Spring
     * Service Layer.
     *
     * @param vocabularyId
     *            vocabulary id
     * @param vocabularyConcepts
     *            concepts of vocabulary
     * @param newBindedElement
     *            newly binded elements
     * @throws ServiceException
     *             when an error occurs
     */
    protected void importIntoDb(int vocabularyId, Set<VocabularyConcept> vocabularyConcepts, List<DataElement> newBindedElement)
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

} // end of abstract class VocabularyImportServiceBaseImpl
