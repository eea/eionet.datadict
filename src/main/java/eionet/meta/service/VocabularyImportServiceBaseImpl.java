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
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base abstract class used for vocabulary import from different sources (RDF or CSV).
 *
 * @author enver
 */
public abstract class VocabularyImportServiceBaseImpl implements IVocabularyImportService {
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
     * @param concepts to be deleted
     * @throws ServiceException if an error occurs during operation
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
     * Purge/delete bound elements from vocabulary folder.
     *
     * @param vocabularyFolderId id of vocabulary folder
     * @param boundElements      bound elements
     * @throws ServiceException if an error occurs during operation
     */
    protected void purgeBoundElements(int vocabularyFolderId, List<DataElement> boundElements) throws ServiceException {
        if (boundElements != null && boundElements.size() > 0) {
            for (DataElement elem : boundElements) {
                this.vocabularyService.removeDataElement(vocabularyFolderId, elem.getId());
            }
        }
    } // end of method purgeBoundElements

    /**
     * This method import objects into DB. It creates not-existing objects and then updates values. All operation is done Spring
     * Service Layer.
     *
     * @param vocabularyId                        vocabulary id.
     * @param vocabularyConceptsToUpdate          concepts of vocabulary to be updated.
     * @param vocabularyConceptsToUpdate          concepts of vocabulary to be deleted.
     * @param newBoundElements                    newly bound elements.
     * @param elementsRelatedToNotCreatedConcepts data elements which are related to newly created concepts.
     * @throws ServiceException when an error occurs.
     */
    protected void importIntoDb(int vocabularyId, List<VocabularyConcept> vocabularyConceptsToUpdate,
                                List<VocabularyConcept> vocabularyConceptsToDelete, List<DataElement> newBoundElements,
                                Map<Integer, Set<DataElement>> elementsRelatedToNotCreatedConcepts) throws ServiceException {
        // first of all insert new bound element
        for (DataElement elem : newBoundElements) {
            this.vocabularyService.addDataElement(vocabularyId, elem.getId());
        }

        for (VocabularyConcept vc : vocabularyConceptsToUpdate) {
            // STEP 1. INSERT VOCABULARY CONCEPT and UPDATE DATAELEMENT WHO ARE RELATED TO NEWLY CREATED CONCEPT
            int id = vc.getId();
            if (id <= 0) {
                // INSERT VOCABULARY CONCEPT
                int insertedId = this.vocabularyService.createVocabularyConceptNonTransactional(vocabularyId, vc);
                // after insert operation get id of the vocabulary and set it!
                vc.setId(insertedId);
                Set<DataElement> elementsRelatedToConcept = elementsRelatedToNotCreatedConcepts.get(id);
                if (elementsRelatedToConcept != null) {
                    for (DataElement elem : elementsRelatedToConcept) {
                        elem.setRelatedConceptId(insertedId);
                    }
                }
            }
        }

        // STEP 2. UPDATE VOCABULARY CONCEPT
        for (VocabularyConcept vc : vocabularyConceptsToUpdate) {
            this.vocabularyService.updateVocabularyConceptNonTransactional(vc);
        }

        // STEP 3. FIX RELATED REFERENCE ELEMENTS
        this.vocabularyService.fixRelatedReferenceElements(vocabularyId, vocabularyConceptsToUpdate);

        // STEP 4. DELETE VOCABULARY CONCEPT
        purgeConcepts(vocabularyConceptsToDelete);
    } // end of method importIntoDb

    /**
     * {@inheritDoc}
     */
    @Override
    public StandardGenericStatus getStatusForMissingConceptAction(MissingConceptsAction missingConceptsAction) {
        switch (missingConceptsAction) {
            case invalid:
                return StandardGenericStatus.INVALID;
            case retired:
                return StandardGenericStatus.DEPRECATED_RETIRED;
            case deprecated:
                return StandardGenericStatus.DEPRECATED;
            case superseded:
                return StandardGenericStatus.DEPRECATED_SUPERSEDED;
            case keep:
            case remove:
            default:
                return null;

        }
    } // end of method getStatusForMissingConceptAction

} // end of abstract class VocabularyImportServiceBaseImpl
