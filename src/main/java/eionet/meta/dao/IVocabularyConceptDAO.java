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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;

/**
 * Vocabulary concept DAO interface.
 *
 * @author Juhan Voolaid
 */
public interface IVocabularyConceptDAO {

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param filter
     * @return
     */
    VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter);

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param vocabularyFolderId
     * @return
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId);

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyFolderId
     * @param conceptIdentifier
     * @return
     */
    VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier);

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyConceptId
     * @return
     */
    VocabularyConcept getVocabularyConcept(int vocabularyConceptId);

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId
     * @param vocabularyConcept
     * @return
     */
    int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept);

    /**
     * Updates vocabulary concept.
     *
     * @param vocabularyConcept
     */
    void updateVocabularyConcept(VocabularyConcept vocabularyConcept);

    /**
     * Deletes vocabulary concepts.
     *
     * @param ids
     */
    void deleteVocabularyConcepts(List<Integer> ids);

    /**
     * Mark concepts obsolete.
     *
     * @param ids
     */
    void markConceptsObsolete(List<Integer> ids);

    /**
     * Remove obsolete date from concepts.
     *
     * @param ids
     */
    void unMarkConceptsObsolete(List<Integer> ids);

    /**
     * Deletes vocabulary concepts.
     *
     * @param vocabularyFolderId
     */
    void deleteVocabularyConcepts(int vocabularyFolderId);

    /**
     * Moves vocabulary concepts from one folder to another.
     *
     * @param fromVocabularyFolderId
     * @param toVocabularyFolderId
     */
    void moveVocabularyConcepts(int fromVocabularyFolderId, int toVocabularyFolderId);

    /**
     * True, if identifier is unique.
     *
     * @param identifier
     * @param vocabularyFolderId
     * @param vocabularyConceptId
     * @return
     */
    boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId);

    /**
     * Returns the next highest vocabulary concept identifier numeric value.
     *
     * @param vocabularyFolderId
     * @return
     */
    int getNextIdentifierValue(int vocabularyFolderId);

    /**
     * Inserts multiple empty concepts.
     *
     * @param vocabularyFolderId
     * @param amount
     * @param startingIdentifier
     * @param label
     * @param defintion
     */
    void insertEmptyConcepts(int vocabularyFolderId, int amount, int startingIdentifier, String label, String definition);

    /**
     * Returns identifiers that match with the given range.
     *
     * @param vocabularyFolderId
     * @param amount
     * @param startingIdentifier
     * @return
     */
    List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier);

    /**
     * Copy vocabulary concepts from one folder to another, used by check out functionality.
     *
     * @param oldVocabularyFolderId
     *            Source Vocabulary folder ID the concepts will be copied from.
     * @param newVocabularyFolderId
     *            Destination Vocabulary folder ID the concepts will be copied to.
     */
    void copyVocabularyConcepts(int oldVocabularyFolderId, int newVocabularyFolderId);

    /**
     * Checks if any concept has values in the binded data element. Returns list of such elements.
     * @param elementId element id
     * @param vocabularyId vocabulary ID
     * @return list of data elements
     */
    List<VocabularyConcept> getConceptsWithValuedElement(int elementId, int vocabularyId);

    /**
     * When vocabulary is checked in this vocabulary concepts get new IDs.
     * If they are used in foreign concepts as refence elements the relations have to be changed to the new ID
     * @param oldVocabularyId old vocabulary record ID
     * @param newVocabularyId new record ID
     */
    void moveReferenceConcepts(int oldVocabularyId, int newVocabularyId);
}
