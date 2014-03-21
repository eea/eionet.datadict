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

import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Triple;

import java.util.List;

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
     * @return
     */
    List<VocabularyFolder> getReleasedVocabularyFolders(int folderId);

    /**
     * Returns vocabulary folders.
     *
     * @param userName
     * @return
     */
    List<VocabularyFolder> getVocabularyFolders(String userName);

    /**
     * Returns vocabulary folders.
     *
     * @param folderId
     * @param userName
     * @return
     */
    List<VocabularyFolder> getVocabularyFolders(int folderId, String userName);

    /**
     * Returns versions of the vocabulary folders.
     *
     * @param continuityId
     * @param vocabularyFolderId folder to exclude
     * @param userName
     * @return
     */
    List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName);

    /**
     * Returns working copies.
     *
     * @param userName
     * @return
     */
    List<VocabularyFolder> getWorkingCopies(String userName);

    /**
     * Creates vocabulary folder.
     *
     * @param vocabularyFolder
     * @return
     */
    int createVocabularyFolder(VocabularyFolder vocabularyFolder);

    /**
     * Updates vocabulary folder.
     *
     * @param vocabularyFolder
     */
    void updateVocabularyFolder(VocabularyFolder vocabularyFolder);

    /**
     * Returns vocabulary folder.
     *
     * @param folderName vocabulary set
     * @param identifier vocabulary identifier
     * @param workingCopy if to return working copy
     * @return Vocabulary folder
     */
    VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy);

    /**
     * Returns vocabulary folder WITHOUT concepts.
     *
     * @param vocabularyFolderId vocabulary ID
     * @return vocabulary domain entity
     */
    VocabularyFolder getVocabularyFolder(int vocabularyFolderId);

    /**
     * Returns the checked out version of the given vocabulary folder.
     *
     * @param checkedOutCopyId  vocabulary ID
     * @return Checked out Vocabulary
     */
    VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId);

    /**
     * Deletes vocabulary folders.
     *
     * @param ids IDs of folders to be deleted
     * @param keepRelatedValues if flagged relations are kept as uris in values instead of IDs
     */
    void deleteVocabularyFolders(List<Integer> ids, boolean keepRelatedValues);

    /**
     * True, if identifier is unique.
     *
     * @param folderId                    folder id
     * @param identifier                  new identifier to check
     * @param excludedVocabularyFolderIds folder ids not to be checked
     * @return true if folder is unique
     */
    boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds);

    /**
     * Forcefully sets notations to identifiers in all concepts within the vocabulary with the given id.
     *
     * @param vocabularyId The given vocabulary id.
     * @return The number of concepts where the notation was different from identifier.
     */
    int forceNotationsToIdentifiers(int vocabularyId);

    /**
     * Returns the vocabulary by the vocabulary id of the given concept.
     *
     * @param conceptId Id of the concept whose parent vocabulary is to be returned.
     * @return The vocabulary object as described above.
     */
    VocabularyFolder getVocabularyFolderOfConcept(int conceptId);

    /**
     * returns list of bound element names used in CSV header.
     *
     * @param vocabularyFolderId vocabulary ID
     * @return list of Pairs where Left = element name and Right=max count of elements in a concept in this vocabulary folder
     */
    List<Triple<String, String, Integer>> getVocabularyFolderBoundElementsMeta(int vocabularyFolderId);


    /**
     * Search vocabularies by the given parameters. No concepts assigned to DAO objects.
     *
     * @param filter container object for filtering parameters
     * @return Result containing values for the paged request
     */
    VocabularyResult searchVocabularies(VocabularyFilter filter);

    /**
     * Updates concept element values where concepts of this vocabulary are marked as related concepts.
     * element value is updated with base URI + concept identifier.
     *
     * @param vocabularyIds list of vocabulary IDs to be checked and handled
     */
    void updateRelatedConceptValueToUri(List<Integer> vocabularyIds);

    /**
     * checks if any of the folders have base uri entered.
     *
     * @param ids list of vocabularies
     * @return true if at least one base uri exists in vocabularies of the given IDs
     */
    boolean vocabularyHasBaseUri(List<Integer> ids);
}
