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

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyConceptAttribute;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;

/**
 * Folder service.
 *
 * @author Juhan Voolaid
 */
public interface IVocabularyService {

    /**
     * Returns folder.
     *
     * @param folderId
     * @return
     * @throws ServiceException
     */
    Folder getFolder(int folderId) throws ServiceException;

    /**
     * True, when in the folder with given id, are vocabulary folders included.
     *
     * @param folderId
     * @return
     * @throws ServiceException
     */
    boolean isFolderEmpty(int folderId) throws ServiceException;

    /**
     * Deletes folder.
     *
     * @param folderId
     * @throws ServiceException
     */
    void deleteFolder(int folderId) throws ServiceException;

    /**
     * Updates folder.
     *
     * @param folder
     * @throws ServiceException
     */
    void updateFolder(Folder folder) throws ServiceException;

    /**
     * Returns list of folders.
     *
     * @param userName
     * @param expandedFolders
     * @return
     * @throws ServiceException
     */
    List<Folder> getFolders(String userName, int... expandedFolders) throws ServiceException;

    /**
     * Returns released vocabulary folders.
     *
     * @param folderId
     * @return
     * @throws ServiceException
     */
    List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) throws ServiceException;

    /**
     * Returns vocabulary folders.
     *
     * @param userName
     *
     * @return
     * @throws ServiceException
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
     * @throws ServiceException
     */
    List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName)
            throws ServiceException;

    /**
     * Creates vocabulary folder with new folder.
     *
     * @param vocabularyFolder
     * @param newFolder
     *            optional
     * @param userName
     * @return
     * @throws ServiceException
     */
    int createVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder, String userName) throws ServiceException;

    /**
     * Creates copy of vocabulary folder - the concepts will be copied.
     *
     * @param vocabularyFolder
     * @param vocabularyFolderId
     *            id from which the copy will be made of
     * @param userName
     * @param newFolder
     *            optional
     * @return
     */
    int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName, Folder newFolder)
            throws ServiceException;

    /**
     * Updates vocabulary folder. The vocabularyFolder.id must be correctly set. Only fields: identifier, label, regStatus and
     * folderId will be updated.
     *
     * @param vocabularyFolder
     * @param newFolder
     *            optional
     * @throws ServiceException
     */
    void updateVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder) throws ServiceException;

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
     * @param emptyAttributes
     *            when true, then attributes that are not valued are also included
     * @return
     * @throws ServiceException
     */
    VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier, boolean emptyAttributes)
            throws ServiceException;

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyConceptId
     * @param emptyAttributes
     *            when true, then attributes that are not valued are also included
     * @return
     * @throws ServiceException
     */
    VocabularyConcept getVocabularyConcept(int vocabularyConceptId, boolean emptyAttributes) throws ServiceException;

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param filter
     * @return
     * @throws ServiceException
     */
    VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) throws ServiceException;

    /**
     * Returns the vocabulary folder's concepts with additional attributes.
     *
     * @param vocabularyFolderId
     * @param numericConceptIdentifiers
     * @param obsoleteStatus
     * @return
     * @throws ServiceException
     */
    List<VocabularyConcept> getVocabularyConceptsWithAttributes(int vocabularyFolderId, boolean numericConceptIdentifiers,
            ObsoleteStatus obsoleteStatus) throws ServiceException;

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
     * Mark concepts obsolete.
     *
     * @param ids
     * @throws ServiceException
     */
    void markConceptsObsolete(List<Integer> ids) throws ServiceException;

    /**
     * Remove obsolete date from concepts.
     *
     * @param ids
     * @throws ServiceException
     */
    void unMarkConceptsObsolete(List<Integer> ids) throws ServiceException;

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
     * @param folderId
     * @param identifier
     * @param excludedVocabularyFolderIds
     * @return
     * @throws ServiceException
     */
    boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds)
            throws ServiceException;

    /**
     * True, if folder identifier is unique.
     *
     * @param identifier
     * @param excludedId
     * @return
     * @throws ServiceException
     */
    boolean isUniqueFolderIdentifier(String identifier, int excludedId) throws ServiceException;

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

    /**
     * Calls Content Registry REST method for re-harvesting the vocabulary RDF. When REST request fails, then the error is logged in
     * log file. The Exception is only thrown, when vocabulary fodler is not found in DB.
     *
     * @param vocabularyFolderId
     *            Vocabulary folder primary key.
     * @throws ServiceException
     *             System did not find the vocabulary folder from DB.
     */
    void pingCrToReharvestVocabulary(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns user's working copies.
     *
     * @param userName
     * @return
     * @throws ServiceException
     */
    List<VocabularyFolder> getWorkingCopies(String userName) throws ServiceException;

    /**
     * Returns vocabulary folder attributes metadata (without values).
     *
     * @return
     * @throws ServiceException
     */
    List<SimpleAttribute> getVocabularyFolderAttributesMetadata() throws ServiceException;

    /**
     * Returns vocabulary concept attributes metadata (without values).
     *
     * @return
     * @throws ServiceException
     */
    // Old implementation that will be replaced by data element attributes. See #14721.
    @Deprecated
    List<VocabularyConceptAttribute> getVocabularyConceptAttributesMetadata() throws ServiceException;

    /**
     * Get folder by folder identifier.
     *
     * @param folderIdentifier
     *            Folder unique textual identifier.
     * @throws ServiceException
     *             Database error.
     * @return Folder object with metadata.
     */
    Folder getFolderByIdentifier(String folderIdentifier) throws ServiceException;

    /**
     * Adds data element to vocabulary folder.
     *
     * @param vocabularyFolderId
     * @param dataElementId
     * @throws ServiceException
     */
    void addDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Removes data element from vocabulary folder.
     *
     * @param vocabularyFolderId
     * @param dataElementId
     * @throws ServiceException
     */
    void removeDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Returns data elements binded with vocabulary folder.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException
     */
    List<DataElement> getVocabularysDataElemets(int vocabularyFolderId) throws ServiceException;
}
