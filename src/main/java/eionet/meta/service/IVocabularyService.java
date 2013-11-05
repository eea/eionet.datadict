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
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;

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
     * @throws ServiceException if operation fails
     */
    Folder getFolder(int folderId) throws ServiceException;

    /**
     * True, when in the folder with given id, are vocabulary folders included.
     *
     * @param folderId
     * @return
     * @throws ServiceException if operation fails
     */
    boolean isFolderEmpty(int folderId) throws ServiceException;

    /**
     * Deletes folder.
     *
     * @param folderId
     * @throws ServiceException if operation fails
     */
    void deleteFolder(int folderId) throws ServiceException;

    /**
     * Updates folder.
     *
     * @param folder
     * @throws ServiceException if operation fails
     */
    void updateFolder(Folder folder) throws ServiceException;

    /**
     * Returns list of folders.
     *
     * @param userName
     * @param expandedFolders
     * @return
     * @throws ServiceException if operation fails
     */
    List<Folder> getFolders(String userName, int... expandedFolders) throws ServiceException;

    /**
     * Returns released vocabulary folders.
     *
     * @param folderId
     * @return
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) throws ServiceException;

    /**
     * Returns vocabulary folders.
     *
     * @param userName
     *
     * @return
     * @throws ServiceException if operation fails
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
     * @throws ServiceException if operation fails
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
     * @throws ServiceException if operation fails
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
     * @throws ServiceException if operation fails
     */
    void updateVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder) throws ServiceException;

    /**
     * Returns vocabulary folder.
     *
     * @param folderName
     * @param identifier
     * @param workingCopy
     * @return
     * @throws ServiceException if operation fails
     */
    VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) throws ServiceException;

    /**
     * Returns the checked out version of the given vocabulary folder.
     *
     * @param checkedOutCopyId
     * @return
     * @throws ServiceException if operation fails
     */
    VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) throws ServiceException;

    /**
     * Returns vocabulary folder.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException if operation fails
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
     * @throws ServiceException if operation fails
     */
    VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier, boolean emptyAttributes)
            throws ServiceException;

    /**
     * Returns vocabulary concept.
     *
     * @param vocabularyConceptId concept id
     * @return Vocabulary concept
     * @throws ServiceException if operation fails
     */
    VocabularyConcept getVocabularyConcept(int vocabularyConceptId) throws ServiceException;

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param filter
     * @return
     * @throws ServiceException if operation fails
     */
    VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) throws ServiceException;

    /**
     * Returns the vocabulary folder's concepts with additional attributes.
     *
     * @param vocabularyFolderId
     * @param numericConceptIdentifiers
     * @param obsoleteStatus
     * @return
     * @throws ServiceException if operation fails
     */
    List<VocabularyConcept> getVocabularyConceptsWithAttributes(int vocabularyFolderId, boolean numericConceptIdentifiers,
            ObsoleteStatus obsoleteStatus) throws ServiceException;

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId
     * @param vocabularyConcept
     * @return
     * @throws ServiceException if operation fails
     */
    int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Updates vocabulary concept.
     *
     * @param vocabularyConcept
     * @throws ServiceException if operation fails
     */
    void updateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Updates vocabulary concept without updating attributes.
     *
     * @param vocabularyConcept
     * @throws ServiceException if operation fails
     */
    void quickUpdateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Deletes vocabulary concepts.
     *
     * @param ids
     * @throws ServiceException if operation fails
     */
    void deleteVocabularyConcepts(List<Integer> ids) throws ServiceException;

    /**
     * Mark concepts obsolete.
     *
     * @param ids
     * @throws ServiceException if operation fails
     */
    void markConceptsObsolete(List<Integer> ids) throws ServiceException;

    /**
     * Remove obsolete date from concepts.
     *
     * @param ids
     * @throws ServiceException if operation fails
     */
    void unMarkConceptsObsolete(List<Integer> ids) throws ServiceException;

    /**
     * Deletes vocabulary folders.
     *
     * @param ids
     * @throws ServiceException if operation fails
     */
    void deleteVocabularyFolders(List<Integer> ids) throws ServiceException;

    /**
     * Checks out vocabulary folder.
     *
     * @param vocabularyFolderId
     * @param userName
     * @return
     * @throws ServiceException if operation fails
     */
    int checkOutVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * Checks in the vocabulary folder.
     *
     * @param vocabularyFolderId
     *            the id of the checked out object
     * @param userName
     * @return
     * @throws ServiceException if operation fails
     */
    int checkInVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * Discards the checked out version.
     *
     * @param vocabularyFolderId
     *            id of the checked out version
     * @param userName
     * @return original id
     * @throws ServiceException if operation fails
     */
    int undoCheckOut(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * True, if identifier is unique.
     *
     * @param folderId
     * @param identifier
     * @param excludedVocabularyFolderIds
     * @return
     * @throws ServiceException if operation fails
     */
    boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds)
            throws ServiceException;

    /**
     * True, if folder identifier is unique.
     *
     * @param identifier
     * @param excludedId
     * @return
     * @throws ServiceException if operation fails
     */
    boolean isUniqueFolderIdentifier(String identifier, int excludedId) throws ServiceException;

    /**
     * True, if identifier is unique.
     *
     * @param identifier
     * @param vocabularyFolderId
     * @param vocabularyConceptId
     * @return
     * @throws ServiceException if operation fails
     */
    boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId) throws ServiceException;

    /**
     * Reserves free site codes.
     *
     * @param vocabularyFolderId
     * @param amount
     * @param startIdentifier
     * @param userName
     * @throws ServiceException if operation fails
     */
    void reserveFreeSiteCodes(int vocabularyFolderId, int amount, int startIdentifier, String userName) throws ServiceException;

    /**
     * Returns the next highest vocabulary concept identifier numeric values.
     *
     * @param vocabularyFolderId
     * @return
     * @throws ServiceException if operation fails
     */
    int getNextIdentifierValue(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns identifiers that match with the given range.
     *
     * @param vocabularyFolderId
     * @param amount
     * @param startingIdentifier
     * @return
     * @throws ServiceException if operation fails
     */
    List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier) throws ServiceException;

    /**
     * Calls Content Registry REST method for re-harvesting the vocabulary RDF. When REST request fails, then the error is logged in
     * log file. The Exception is only thrown, when vocabulary fodler is not found in DB.
     *
     * @param vocabularyFolderId
     *            Vocabulary folder primary key.
     * @throws ServiceException if operation fails
     *             System did not find the vocabulary folder from DB.
     */
    void pingCrToReharvestVocabulary(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns user's working copies.
     *
     * @param userName
     * @return
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getWorkingCopies(String userName) throws ServiceException;

    /**
     * Returns vocabulary folder attributes metadata (without values).
     *
     * @return
     * @throws ServiceException if operation fails
     */
    List<SimpleAttribute> getVocabularyFolderAttributesMetadata() throws ServiceException;

    /**
     * Get folder by folder identifier.
     *
     * @param folderIdentifier
     *            Folder unique textual identifier.
     * @throws ServiceException if operation fails
     *             Database error.
     * @return Folder object with metadata.
     */
    Folder getFolderByIdentifier(String folderIdentifier) throws ServiceException;

    /**
     * Adds data element to vocabulary folder.
     *
     * @param vocabularyFolderId
     * @param dataElementId
     * @throws ServiceException if operation fails
     */
    void addDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Removes data element from vocabulary folder.
     *
     * @param vocabularyFolderId
     * @param dataElementId
     * @throws ServiceException if operation fails
     */
    void removeDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Returns data elements bound to the vocabulary folder.
     *
     * @param vocabularyFolderId folder ID
     * @return list of elements
     * @throws ServiceException if call fails
     */
    List<DataElement> getVocabularyDataElements(int vocabularyFolderId) throws ServiceException;

    /**
     * Checks if vocabulary has binding for the data element.
     * @param vocabularyFolderId vocabulary Id
     * @param dataElementId element id
     * @return true if binding exits
     * @throws ServiceException if query fails
     */
    boolean vocabularyHasDataElementBinding(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Returns list of other concepts that have this dataelement valued.
     * Checks only editable copy values
     * @param dataElementId element ID
     * @return list of elements
     * @throws ServiceException if query fails
     */
    List<VocabularyConcept> getConceptsWithElementValue(int dataElementId) throws ServiceException;

    /**
     * Returns all namespaces that are used by the binded elements. Both external and internal.
     * Internal namespaces are composed with prefix dd[element.idand uri BASE_URI/dataelement/[element.id]
     * @param vocabularyFolders vocabularies
     * @return container of RDF Namespace objects
     * @throws ServiceException if query fails
     */
    List<RdfNamespace> getVocabularyNamespaces(List<VocabularyFolder> vocabularyFolders) throws ServiceException;

    /**
     * Some data element has special treatment.
     * For example for handling relations between internal concepts we use skos:relation elements
     * that are in this Enum
     *
     * @author Kaido Laine
     */
    public enum RelationalElement {
        /** relation elements. */
        BROADER_CONCEPT, NARROWER_CONCEPT, RELATED_CONCEPT;
    }

    /**
     * Checks if given element has some special behaviour.
     *
     * @param relationalElement
     *            special relational element
     * @return String prefix in RDF
     */
    String getRelationalElementPrefix(RelationalElement relationalElement);

    /**
     * Checks if the given element represents relation to an external resource.
     * @param id data element ID
     * @return true if element is relational (type = reference)
     */
    boolean isReferenceElement(int id);

    /**
     * Returns name list of bound elements. Each name is repeated as much times as is the max count of the element values in a
     * concept in this folder.
     * @param vocabularyFolder vocabulary folder
     * @return slit of bound element names
     */
    List<String> getVocabularyBoundElementNames(VocabularyFolder vocabularyFolder);

    /**
     * Searches vocabularies by the given filter.
     *
     * @param filter filtering parameters
     * @return Result object containing found vocabularies
     * @throws ServiceException if operation fails
     */
    VocabularyResult searchVocabularies(VocabularyFilter filter) throws ServiceException;


}
