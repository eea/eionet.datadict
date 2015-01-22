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
     *            filter
     * @return concepts as a result
     */
    VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter);

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     * @return list of concepts
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId);

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     * @param conceptIdentifier
     *            concept identifier
     * @return vocabulary concept
     */
    VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier);

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyConceptId
     *            concept id
     * @return vocabulary concept
     */
    VocabularyConcept getVocabularyConcept(int vocabularyConceptId);

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     * @param vocabularyConcept
     *            concept
     * @return created concept id
     */
    int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept);

    /**
     * Updates vocabulary concept.
     *
     * @param vocabularyConcept
     *            concept to be updated
     */
    void updateVocabularyConcept(VocabularyConcept vocabularyConcept);

    /**
     * Deletes vocabulary concepts.
     *
     * @param ids
     *            ids
     */
    void deleteVocabularyConcepts(List<Integer> ids);

    /**
     * Mark concepts invalid.
     *
     * @param ids
     *            ids
     */
    void markConceptsInvalid(List<Integer> ids);

    /**
     * Mark concepts invalid.
     *
     * @param ids
     *            ids
     */
    void markConceptsValid(List<Integer> ids);

    /**
     * Deletes vocabulary concepts.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     */
    void deleteVocabularyConcepts(int vocabularyFolderId);

    /**
     * Moves vocabulary concepts from one folder to another.
     *
     * @param fromVocabularyFolderId
     *            from vocabulary id
     * @param toVocabularyFolderId
     *            to vocabulary id
     */
    void moveVocabularyConcepts(int fromVocabularyFolderId, int toVocabularyFolderId);

    /**
     * True, if identifier is unique.
     *
     * @param identifier
     *            concept identifier
     * @param vocabularyFolderId
     *            vocabulary id
     * @param vocabularyConceptId
     *            concept id
     * @return is unique or not
     */
    boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId);

    /**
     * Returns the next highest vocabulary concept identifier numeric value.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     * @return next identifier value
     */
    int getNextIdentifierValue(int vocabularyFolderId);

    /**
     * Inserts multiple empty concepts.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     * @param amount
     *            number
     * @param startingIdentifier
     *            starting identifier
     * @param label
     *            label
     * @param definition
     *            definition
     */
    void insertEmptyConcepts(int vocabularyFolderId, int amount, int startingIdentifier, String label, String definition);

    /**
     * Returns identifiers that match with the given range.
     *
     * @param vocabularyFolderId
     *            vocabulary id
     * @param amount
     *            number
     * @param startingIdentifier
     *            starting identifier
     * @return list of integer as identifiers
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
     * Checks if any concept has values in the bound data element. Returns list of such elements.
     *
     * @param elementId
     *            element id
     * @param vocabularyId
     *            vocabulary ID
     * @return list of data elements
     */
    List<VocabularyConcept> getConceptsWithValuedElement(int elementId, int vocabularyId);

    /**
     * When vocabulary is checked in this vocabulary concepts get new IDs.
     * If they are referred in other concepts the relations have to be changed to the new ID
     * @param oldVocabularyId old vocabulary record ID
     */
    void updateReferringReferenceConcepts(int oldVocabularyId);

    /**
     * Finds vocabulary concepts of the vocabulary that are accepted.
     * Expects an element to have datatype attribute otherwise not included to the result
     * not included to the result.
     *
     * @param vocabularyId
     *            vocabulary ID
     * @param conceptIdentifier
     *            concept identifier
     * @param label
     *            concept label
     * @param dataElementIdentifier
     *            data element identifier as a filter, if null all data elements
     * @param language
     *            identifier as a filter, if null all languages
     * @param defaultLanguage
     *            default language (applicable when language is not null)
     *
     * @return elements with valued bound elements
     */
    List<VocabularyConcept> getValidConceptsWithValuedElements(int vocabularyId, String conceptIdentifier, String label,
            String dataElementIdentifier, String language, String defaultLanguage);

}
