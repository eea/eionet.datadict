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

import eionet.datadict.errors.ConceptWithoutNotationException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.web.action.AbstractActionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base abstract class used for vocabulary import from different sources (RDF or CSV).
 *
 * @author enver
 */
public abstract class VocabularyImportServiceBaseImpl implements IVocabularyImportService {

    private static final int BATCH_SIZE = 1000;

    protected static final Logger LOGGER = LoggerFactory.getLogger(VocabularyImportServiceBaseImpl.class);

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
            LOGGER.info("Purging concepts with ids: " + conceptIds);
            this.vocabularyService.deleteVocabularyConcepts(conceptIds);
        }
    }

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
    }

    /**
     * This method import objects into DB. It creates not-existing objects and then updates values. All operation is done Spring
     * Service Layer.
     *
     * @param vocabularyId                        vocabulary id.
     * @param vocabularyConceptsToUpdate          concepts of vocabulary to be updated.
     * @param vocabularyConceptsToDelete          concepts of vocabulary to be deleted.
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

        VocabularyFolder vocabularyFolder = this.vocabularyService.getVocabularyFolder(vocabularyId);
        boolean notationsEqualIdentifiers = vocabularyFolder != null && vocabularyFolder.isNotationsEqualIdentifiers();

        // find and batch insert new concepts
        List<VocabularyConcept> newConcepts = new ArrayList<VocabularyConcept>();

        for (Iterator<VocabularyConcept> it = vocabularyConceptsToUpdate.iterator(); it.hasNext();) {
            VocabularyConcept vocabularyConcept = it.next();
            if (notationsEqualIdentifiers) {
                vocabularyConcept.setNotation(vocabularyConcept.getIdentifier());
            }

            if(vocabularyService.checkIfConceptShouldBeAddedWhenBoundToElement(vocabularyFolder.getId(), vocabularyConcept.getNotation()) == false){
                //abort upload
                throw new ServiceException("Upload aborted. Found concepts without notation for vocabulary referenced by data elements");
            }

            // new concepts
            if (vocabularyConcept.getId() <= 0) {
                if (vocabularyConcept.getStatus() == null) {
                    vocabularyConcept.setStatus(StandardGenericStatus.VALID);
                    vocabularyConcept.setStatusModified(new java.sql.Date(System.currentTimeMillis()));
                }
                vocabularyConcept.setVocabularyId(vocabularyId);
                newConcepts.add(vocabularyConcept);
                it.remove();
            }

            this.vocabularyService.updateAcceptedNotAcceptedDate(vocabularyConcept);
        }

        if (!newConcepts.isEmpty()) {
            List<Integer> insertedIds = this.vocabularyService.batchCreateVocabularyConcepts(newConcepts, BATCH_SIZE);
            if (newConcepts.size() != insertedIds.size()) {
                throw new ServiceException("Not all new concepts were inserted in the database");
            }
           
            // update related concept ids with those generated after db insert
            for (int i = 0; i < newConcepts.size(); i++) {
                VocabularyConcept newConcept = newConcepts.get(i);
                int rdfDummyId = newConcepts.get(i).getId();
                int insertedId = insertedIds.get(i);
                newConcept.setId(insertedId);

                Set<DataElement> elementsRelatedToConcept = elementsRelatedToNotCreatedConcepts.get(rdfDummyId);
                if (elementsRelatedToConcept != null) {
                    for (DataElement elem : elementsRelatedToConcept) {
                        elem.setRelatedConceptId(insertedId);
                    }
                }
            }
            vocabularyConceptsToUpdate.addAll(newConcepts); // db ids
        }

        this.vocabularyService.batchUpdateVocabularyConcepts(vocabularyConceptsToUpdate, BATCH_SIZE);
        this.vocabularyService.batchUpdateVocabularyConceptsDataElementValues(vocabularyConceptsToUpdate, BATCH_SIZE);
        this.vocabularyService.batchFixRelatedReferenceElements(vocabularyConceptsToUpdate, BATCH_SIZE);

        if (vocabularyConceptsToDelete.size() <= BATCH_SIZE) {
            purgeConcepts(vocabularyConceptsToDelete);
        } else {
            int fromIndex = 0;
            int toIndex = BATCH_SIZE;
            while (vocabularyConceptsToDelete.size() <= toIndex) {
                purgeConcepts(vocabularyConceptsToDelete.subList(fromIndex, toIndex));
                fromIndex = toIndex;
                toIndex = toIndex + BATCH_SIZE <= vocabularyConceptsToDelete.size() ?  toIndex + BATCH_SIZE : vocabularyConceptsToDelete.size();
            }
        }
    }

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
    }

}
