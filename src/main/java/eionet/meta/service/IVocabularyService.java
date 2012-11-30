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
     * Creates vocabulary folder.
     *
     * @param vocabularyFolder
     * @return
     */
    int createVocabularyFolder(VocabularyFolder vocabularyFolder) throws ServiceException;

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
     * @param identifier
     * @param workingCopy
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyFolder(String identifier, boolean workingCopy) throws ServiceException;

    /**
     * Returns vocabulary folder.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException
     */
    VocabularyFolder getVocabularyFolder(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId) throws ServiceException;

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
}
