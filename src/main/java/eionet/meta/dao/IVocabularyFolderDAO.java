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

import java.util.Date;
import java.util.List;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularySet;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Triple;
import java.util.Collection;

/**
 * Vocabulary DAO interface.
 *
 * @author Juhan Voolaid
 */
public interface IVocabularyFolderDAO {

    /**
     * Returns released vocabulary folders.
     *
     * @param folderId
     *            folder id.
     * @return list of vocabulary folders.
     */
    List<VocabularyFolder> getReleasedVocabularyFolders(int folderId);

    /**
     * Returns vocabulary folders.
     *
     * @param userName
     *            user of folder
     * @return list of vocabulary folders.
     */
    List<VocabularyFolder> getVocabularyFolders(String userName);

    /**
     * Returns vocabulary folders.
     *
     * @param folderId
     *            vocabulary folder id.
     * @param userName
     *            user of folder.
     * @return list of vocabulary folders.
     */
    List<VocabularyFolder> getVocabularyFolders(int folderId, String userName);

    /**
     * Returns versions of the vocabulary folders.
     *
     * @param continuityId
     *            continuity id
     * @param vocabularyFolderId
     *            folder to exclude
     * @param userName
     *            user of folder
     * @return list of vocabulary folders.
     */
    List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName);

    /**
     * Returns working copies.
     *
     * @param userName
     *            user of folder
     * @return list of vocabulary folders.
     */
    List<VocabularyFolder> getWorkingCopies(String userName);

    /**
     * Creates vocabulary folder.
     *
     * @param vocabularyFolder
     *            vocabulary folder.
     * @return id of new folder
     */
    int createVocabularyFolder(VocabularyFolder vocabularyFolder);

    /**
     * Updates vocabulary folder.
     *
     * @param vocabularyFolder
     *            vocabulary folder.
     */
    void updateVocabularyFolder(VocabularyFolder vocabularyFolder);

    /**
     * Returns vocabulary folder.
     *
     * @param folderName
     *            vocabulary set
     * @param identifier
     *            vocabulary identifier
     * @param workingCopy
     *            if to return working copy
     * @return Vocabulary folder or null Object if No VocabularyFolder Is Found
     */
    VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy);

    /**
     * Returns vocabulary folder WITHOUT concepts.
     *
     * @param vocabularyFolderId
     *            vocabulary ID
     * @return vocabulary domain entity
     */
    VocabularyFolder getVocabularyFolder(int vocabularyFolderId);

    /**
     * Returns the checked out version of the given vocabulary folder.
     *
     * @param checkedOutCopyId
     *            vocabulary ID
     * @return Checked out Vocabulary
     */
    VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId);

    /**
     * Deletes vocabulary folders.
     *
     * @param ids
     *            IDs of folders to be deleted
     * @param keepRelatedValues
     *            if flagged relations are kept as uris in values instead of IDs
     */
    void deleteVocabularyFolders(List<Integer> ids, boolean keepRelatedValues);

    /**
     * True, if identifier is unique.
     *
     * @param folderId
     *            folder id
     * @param identifier
     *            new identifier to check
     * @param excludedVocabularyFolderIds
     *            folder ids not to be checked
     * @return true if folder is unique
     */
    boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds);

    /**
     * Forcefully sets notations to identifiers in all concepts within the vocabulary with the given id.
     *
     * @param vocabularyId
     *            The given vocabulary id.
     * @return The number of concepts where the notation was different from identifier.
     */
    int forceNotationsToIdentifiers(int vocabularyId);

    /**
     * Returns the vocabulary by the vocabulary id of the given concept.
     *
     * @param conceptId
     *            Id of the concept whose parent vocabulary is to be returned.
     * @return The vocabulary object as described above.
     */
    VocabularyFolder getVocabularyFolderOfConcept(int conceptId);

    /**
     * returns list of bound element names used in CSV header.
     *
     * @param vocabularyFolderId
     *            vocabulary ID
     * @return list of Pairs where Left = element name and Right=max count of elements in a concept in this vocabulary folder
     */
    List<Triple<String, String, Integer>> getVocabularyFolderBoundElementsMeta(int vocabularyFolderId);

    /**
     * Search vocabularies by the given parameters. No concepts assigned to DAO objects.
     *
     * @param filter
     *            container object for filtering parameters
     * @return Result containing values for the paged request
     */
    VocabularyResult searchVocabularies(VocabularyFilter filter);

    /**
     * Updates concept element values where concepts of this vocabulary are marked as related concepts. element value is updated
     * with base URI + concept identifier.
     *
     * @param vocabularyIds
     *            list of vocabulary IDs to be checked and handled
     */
    void updateRelatedConceptValueToUri(List<Integer> vocabularyIds);

    /**
     * Updates a vocabulary concept element to set related concept id field to a new value and and element value field to NULL.
     *
     * @param element
     *            to be updated element (!!! ATTENTION: id field of this object should be set as VOCABULARY_CONCEPT_ELEMENT Table ID
     *            Column)
     */
    void updateRelatedConceptValueToId(DataElement element);

    /**
     * checks if any of the folders have base uri entered.
     *
     * @param ids
     *            list of vocabularies
     * @return true if at least one base uri exists in vocabularies of the given IDs
     */
    boolean vocabularyHasBaseUri(List<Integer> ids);

    /**
     * Populates empty base uri values in vocabulary table.
     *
     * @param prefix
     *            base uri prefix
     * @return number of affected rows
     */
    int populateEmptyBaseUris(String prefix);

    /**
     * Changes site prefixes in vocabulary base uris.
     *
     * @param oldSitePrefix
     *            old site prefix
     * @param newSitePrefix
     *            new site prefix
     * @return number of affected rows
     */
    int changeSitePrefix(String oldSitePrefix, String newSitePrefix);

    /**
     * Returns list of recently released vocabulary folders. Performs a query to sort vocabulary folders based on descending date
     * modified field.
     *
     * @param limit
     *            maximum number of vocabulary folders
     * @return list of vocabulary folders
     */
    List<VocabularyFolder> getRecentlyReleasedVocabularyFolders(int limit);

    /**
     * Returns a VocabularySet object
     * @param vocabularyID
     *          the vocabulary ID
     * @return a vocabulary set
     */
    VocabularySet getVocabularySet(int vocabularyID);
    

    /**
     * Return information about the relationships with other vocabularies for the vocabulary identified by this ID.
     * Information is a Triple of vocabulary ID - relationship ID - related vocabulary ID 
     * 
     * @param vocabularyID
     * @return list of Triple
     */
    List<Triple<Integer,Integer,Integer>> getVocabulariesRelation(int vocabularyID);

    /**
     * Returns a list of Vocabulary Concept elements IDs with which the Vocabulary Concept identified by vocabularyConceptID is related to
     * 
     * @param vocabularyConceptID a specific vocabulary concept ID
     * @param relationshipElementID a specific data element ID describing the relationship
     * @param relatedVocabularyID a specific related vocabulary ID
     * @return 
     */
    List<Integer> getRelatedVocabularyConcepts(int vocabularyConceptID, int relationshipElementID, int relatedVocabularyID);

    Collection<Integer> getVocabularyIds(Collection<Integer> vocabularyConceptIds);

    Collection<Integer> getWorkingCopyIds(Collection<Integer> vocabularyIds);

    /**
     * Updates the date and user modified columns of Vocabulary
     *
     * @param dateModified
     * @param username
     * @param vocabularyId
     */
    void updateDateAndUserModified(Date dateModified, String username, Integer vocabularyId);


}
