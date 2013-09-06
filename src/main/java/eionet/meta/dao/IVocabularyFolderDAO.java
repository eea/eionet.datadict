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

import eionet.meta.dao.domain.VocabularyFolder;

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
     *
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
     * @param vocabularyFolderId
     *            folder to exclude
     * @param userName
     *
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
     * @param folderName
     * @param identifier
     * @param workingCopy
     * @return
     */
    VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy);

    /**
     * Returns vocabulary folder.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyFolder(int vocabularyFolderId);

    /**
     * Returns the checked out version of the given vocabulary folder.
     *
     * @param checkedOutCopyId
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId);

    /**
     * Deletes vocabulary folders.
     *
     * @param ids
     */
    void deleteVocabularyFolders(List<Integer> ids);

    /**
     * True, if identifier is unique.
     *
     * @param folderId
     * @param identifier
     * @param excludedVocabularyFolderIds
     * @return
     */
    boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds);

    /**
     * Forcefully sets notations to identifiers in all concepts within the vocabulary with the given id.
     * @param vocabularyId The given vocabulary id.
     * @return The number of concepts where the notation was different from identifier.
     */
    int forceNotationsToIdentifiers(int vocabularyId);

    /**
     * Returns the vocabulary by the vocabulary id of the given concept.
     * @param conceptId Id of the concept whose parent vocabulary is to be returned.
     * @return The vocabulary object as described above.
     */
    VocabularyFolder getVocabularyFolderOfConcept(int conceptId);

}
