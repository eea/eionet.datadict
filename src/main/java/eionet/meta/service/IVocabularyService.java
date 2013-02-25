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

package eionet.meta.service;

import java.util.List;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;

/**
 * Folder service.
 *
 * @author Juhan Voolaid
 */
public interface IVocabularyService {

    /**
     * Returns vocabulary folders.
     *
     * @param userName
     *
     * @return
     */
    List<VocabularyFolder> getVocabularyFolders(String userName) throws ServiceException;

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
    List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName)
            throws ServiceException;

    /**
     * Creates vocabulary folder.
     *
     * @param vocabularyFolder
     * @param userName
     * @return
     */
    int createVocabularyFolder(VocabularyFolder vocabularyFolder, String userName) throws ServiceException;

    /**
     * Creates copy of vocabulary folder - the concepts will be copied.
     *
     * @param vocabularyFolder
     * @param vocabularyFolderId
     *            id from which the copy will be made of
     * @param userName
     * @return
     */
    int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName)
            throws ServiceException;

    /**
     * Updates vocabulary folder. The vocabularyFolder.id must be correctly set. Only fields: identifier, label, regStatus will be
     * updated.
     *
     * @param vocabularyFolder
     * @throws ServiceException
     */
    void updateVocabularyFolder(VocabularyFolder vocabularyFolder) throws ServiceException;

    /**
     * Returns vocabulary folder.
     *
     * @param folderName
     * @param identifier
     * @param workingCopy
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) throws ServiceException;

    /**
     * Returns the checked out version of the given vocabulary folder.
     *
     * @param checkedOutCopyId
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) throws ServiceException;

    /**
     * Returns vocabulary folder.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyFolder(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyFolderId
     * @param conceptIdentifier
     * @return
     * @throws ServiceException
     */
    VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier) throws ServiceException;

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param filter
     * @return
     * @throws ServiceException
     */
    VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) throws ServiceException;

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId
     * @param vocabularyConcept
     * @return
     * @throws ServiceException
     */
    int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Updates vocabulary concept.
     *
     * @param vocabularyConcept
     * @throws ServiceException
     */
    void updateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Updates vocabulary concept without updating attributes.
     *
     * @param vocabularyConcept
     * @throws ServiceException
     */
    void quickUpdateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Deletes vocabulary concepts.
     *
     * @param ids
     * @throws ServiceException
     */
    void deleteVocabularyConcepts(List<Integer> ids) throws ServiceException;

    /**
     * Deletes vocabulary folders.
     *
     * @param ids
     * @throws ServiceException
     */
    void deleteVocabularyFolders(List<Integer> ids) throws ServiceException;

    /**
     * Checks out vocabulary folder.
     *
     * @param vocabularyFolderId
     * @param userName
     * @return
     * @throws ServiceException
     */
    int checkOutVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * Checks in the vocabulary folder.
     *
     * @param vocabularyFolderId
     *            the id of the checked out object
     * @param userName
     * @return
     * @throws ServiceException
     */
    int checkInVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * Discards the checked out version.
     *
     * @param vocabularyFolderId
     *            id of the checked out version
     * @param userName
     * @return original id
     * @throws ServiceException
     */
    int undoCheckOut(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * True, if identifier is unique.
     *
     * @param folderName
     * @param identifier
     * @param excludedVocabularyFolderIds
     * @return
     * @throws ServiceException
     */
    boolean isUniqueFolderIdentifier(String folderName, String identifier, int... excludedVocabularyFolderIds)
            throws ServiceException;

    /**
     * True, if identifier is unique.
     *
     * @param identifier
     * @param vocabularyFolderId
     * @param vocabularyConceptId
     * @return
     * @throws ServiceException
     */
    boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId) throws ServiceException;

    /**
     * Reserves free site codes.
     *
     * @param vocabularyFolderId
     * @param amount
     * @param startIdentifier
     * @param userName
     * @throws ServiceException
     */
    void reserveFreeSiteCodes(int vocabularyFolderId, int amount, int startIdentifier, String userName) throws ServiceException;

    /**
     * Returns the next highest vocabulary concept identifier numeric value.s
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException
     */
    int getNextIdentifierValue(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns identifiers that match with the given range.
     *
     * @param vocabularyFolderId
     * @param amount
     * @param startingIdentifier
     * @return
     * @throws ServiceException
     */
    List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier) throws ServiceException;
}
