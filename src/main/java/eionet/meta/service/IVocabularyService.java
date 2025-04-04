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
import java.util.Map;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.VocabularyConceptBoundElementFilter;
import eionet.meta.service.data.VocabularyConceptData;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Triple;

/**
 * Folder service.
 *
 * @author Juhan Voolaid
 */
public interface IVocabularyService {

    /**
     * Returns folder.
     *
     * @param folderId folder id
     * @return vocabulary set
     * @throws ServiceException if operation fails
     */
    Folder getFolder(int folderId) throws ServiceException;

    /**
     * True, when in the folder with given id, are vocabulary folders included.
     *
     * @param folderId folder id
     * @return is empty or not
     * @throws ServiceException if operation fails
     */
    boolean isFolderEmpty(int folderId) throws ServiceException;

    /**
     * Deletes folder.
     *
     * @param folderId folder id
     * @throws ServiceException if operation fails
     */
    void deleteFolder(int folderId) throws ServiceException;

    /**
     * Updates folder.
     *
     * @param folder folder
     * @throws ServiceException if operation fails
     */
    void updateFolder(Folder folder) throws ServiceException;

    /**
     * Returns list of folders.
     *
     * @param userName        user name
     * @param expandedFolders expanded folders
     * @return list of folders (vocabulary sets)
     * @throws ServiceException if operation fails
     */
    List<Folder> getFolders(String userName, int... expandedFolders) throws ServiceException;

    /**
     * Returns released vocabulary folders.
     *
     * @param folderId folder id
     * @return list of vocabularies
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) throws ServiceException;

    /**
     * Returns vocabulary folders.
     *
     * @param userName user name
     * @return list of vocabularies
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getVocabularyFolders(String userName) throws ServiceException;

    /**
     * Returns versions of the vocabulary folders.
     *
     * @param continuityId       continuity id
     * @param vocabularyFolderId folder to exclude
     * @param userName           user name
     * @return list of vocabularies
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName)
            throws ServiceException;

    /**
     * Creates vocabulary folder with new folder.
     *
     * @param vocabularyFolder vocabulary folder
     * @param newFolder        optional
     * @param userName         user name
     * @return created vocabulary id
     * @throws ServiceException if operation fails
     */
    int createVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder, String userName) throws ServiceException;

    /**
     * Creates copy of vocabulary folder - the concepts will be copied.
     *
     * @param vocabularyFolder   folder
     * @param vocabularyFolderId id from which the copy will be made of
     * @param userName           user name
     * @param newFolder
     * @param newFolder          optional
     * @return created folder id
     * @throws ServiceException if copy fails
     */
    int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName, Folder newFolder)
            throws ServiceException;

    /**
     * Updates vocabulary folder. The vocabularyFolder.id must be correctly set. Only fields: identifier, label, regStatus and
     * folderId will be updated.
     *
     * @param vocabularyFolder vocabulary folder
     * @param newFolder        optional
     * @throws ServiceException if operation fails
     */
    void updateVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder) throws ServiceException;

    /**
     * Returns vocabulary information with attributes and WITHOUT concepts.
     *
     * @param folderName  vocabulary name vocabulary name
     * @param identifier  vocabulary identifier
     * @param workingCopy true if working copy is needed
     * @return vocabulary information without concepts
     * @throws ServiceException if operation fails
     */
    VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) throws ServiceException;

    /**
     * Returns the checked out version of the given vocabulary folder.
     *
     * @param checkedOutCopyId checked out copy id
     * @return vocabulary
     * @throws ServiceException if operation fails
     */
    VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) throws ServiceException;

    /**
     * Returns vocabulary folder.
     *
     * @param vocabularyFolderId vocabulary id
     * @return vocabulary
     * @throws ServiceException if operation fails
     */
    VocabularyFolder getVocabularyFolder(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns vocabulary concept of a vocabulary by the identifier.
     *
     * @param vocabularyFolderId vocabulary ID
     * @param conceptIdentifier  concept identifier concept identifier
     * @param emptyAttributes    when true, then attributes that are not valued are also included
     * @return vocabulary concept
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
     * @param filter filter parameters
     * @return vocabulary concepts as a result
     * @throws ServiceException if operation fails
     */
    VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) throws ServiceException;

    /**
     * Returns all vocabulary concepts of a vocabulary with additional attributes for RDF.
     *
     * @param vocabularyFolderId vocabulary id
     * @return list of all concepts with attributes
     * @throws ServiceException if operation fails
     */
    List<VocabularyConcept> getAllConceptsWithAttributes(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns vocabulary concepts of a vocabulary with additional attributes for RDF.
     *
     * @param vocabularyFolderId vocabulary ID
     * @param conceptIdentifier  concept identifier to search
     * @param label              concept label or pref label to search
     * @param acceptedOnly       only accepted parameters
     * @return list of concepts
     * @throws ServiceException if operation fails
     */
    List<VocabularyConcept> getConceptsWithAttributes(int vocabularyFolderId, String conceptIdentifier, String label, boolean acceptedOnly) throws ServiceException;

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId vocabulary id
     * @param vocabularyConcept  concept
     * @return created concept id
     * @throws ServiceException if operation fails
     */
    int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId vocabulary id
     * @param vocabularyConcept  concept
     * @return
     * @throws ServiceException if operation fails
     */
    int createVocabularyConceptNonTransactional(int vocabularyFolderId, VocabularyConcept vocabularyConcept)
            throws ServiceException;

    /**
     * Updates vocabulary concept.
     *
     * @param vocabularyConcept concept
     * @throws ServiceException if operation fails
     */
    void updateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Updates vocabulary concept in non-transactional.
     *
     * @param vocabularyConcept concept
     * @throws ServiceException if operation fails
     */
    void updateVocabularyConceptNonTransactional(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Updates vocabulary concept in non-transactional.
     *
     * @param vocabularyConcept concept
     * @param handleInverse     if true inverseOf relations are handled.
     *                          Obligatory in the application bu importer it may cause problems
     * @throws ServiceException if operation fails
     */
    void updateVocabularyConceptNonTransactional(VocabularyConcept vocabularyConcept, boolean handleInverse) throws ServiceException;


    /**
     * Updates vocabulary concept without updating attributes.
     *
     * @param vocabularyConcept concept
     * @throws ServiceException if operation fails
     */
    void quickUpdateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException;

    /**
     * Deletes vocabulary concepts.
     *
     * @param ids list of ids
     * @throws ServiceException if operation fails
     */
    void deleteVocabularyConcepts(List<Integer> ids) throws ServiceException;

    /**
     * Mark concepts obsolete.
     *
     * @param ids list of ids
     * @throws ServiceException if operation fails
     */
    void markConceptsInvalid(List<Integer> ids) throws ServiceException;

    /**
     * Remove obsolete date from concepts.
     *
     * @param ids list of ids
     * @throws ServiceException if operation fails
     */
    void markConceptsValid(List<Integer> ids) throws ServiceException;

     /**
     * Find the Specified VocabularyFolderAnd Mark it as to be deleted.
     *
     * @param vocabularyFolderId the id of the vocabulary Folder
     */
    void markVocabularyFolderToBeDeleted(Integer vocabularyFolderId) throws ServiceException;

    /**
     * Deletes vocabulary folders.
     *
     * @param ids
     * @param ids IDs of folderst to be deleted
     * @param preserveRelations
     * @param preserveRelations shows if to replace relation IDs with
     * baseURi/concept in the related vocabularies the related vocabularies
     * @throws ServiceException if operation fails
     */
    void deleteVocabularyFolders(List<Integer> ids, boolean preserveRelations) throws ServiceException;

    /**
     * Checks out vocabulary folder.
     *
     * @param vocabularyFolderId folder ID
     * @param userName           user name
     * @return
     * @throws ServiceException if operation fails
     */
    int checkOutVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * Checks in the vocabulary folder.
     *
     * @param vocabularyFolderId the id of the checked out object
     * @param userName           user name
     * @return
     * @throws ServiceException if operation fails
     */
    int checkInVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * Discards the checked out version.
     *
     * @param vocabularyFolderId id of the checked out version
     * @param userName           user name
     * @return original id
     * @throws ServiceException if operation fails
     */
    int undoCheckOut(int vocabularyFolderId, String userName) throws ServiceException;

    /**
     * True, if identifier is unique.
     *
     * @param folderId                    folder id
     * @param identifier                  folder identifier
     * @param excludedVocabularyFolderIds excluded folder ids
     * @return is unique or not
     * @throws ServiceException if operation fails
     */
    boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds)
            throws ServiceException;

    /**
     * True, if folder identifier is unique.
     *
     * @param identifier folder identifier
     * @param excludedId excluded id
     * @return is unique or not
     * @throws ServiceException if operation fails
     */
    boolean isUniqueFolderIdentifier(String identifier, int excludedId) throws ServiceException;

    /**
     * True, if identifier is unique.
     *
     * @param identifier          identifier
     * @param vocabularyFolderId  vocabulary id
     * @param vocabularyConceptId concept id
     * @return is unique or not
     * @throws ServiceException if operation fails
     */
    boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId) throws ServiceException;

    /**
     * Reserves free site codes.
     *
     * @param vocabularyFolderId vocabulary id
     * @param amount             number of reservations
     * @param startIdentifier    start identifier
     * @param userName           user name
     * @throws ServiceException if operation fails
     */
    void reserveFreeSiteCodes(int vocabularyFolderId, int amount, int startIdentifier, String userName) throws ServiceException;

    /**
     * Returns the next highest vocabulary concept identifier numeric values.
     *
     * @param vocabularyFolderId vocabulary id
     * @return next identifier value
     * @throws ServiceException if operation fails
     */
    int getNextIdentifierValue(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns identifiers that match with the given range.
     *
     * @param vocabularyFolderId vocabulary id
     * @param amount             number of reservations
     * @param startingIdentifier starting identifier for availaility
     * @return list of integers as available identifiers
     * @throws ServiceException if operation fails
     */
    List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier) throws ServiceException;

    /**
     * Calls Content Registry REST method for re-harvesting the vocabulary RDF. When REST request fails, then the error is logged in
     * log file. The Exception is only thrown, when vocabulary fodler is not found in DB.
     *
     * @param vocabularyFolderId Vocabulary folder primary key.
     * @throws ServiceException if operation fails System did not find the vocabulary folder from DB.
     */
    void pingCrToReharvestVocabulary(int vocabularyFolderId) throws ServiceException;

    /**
     * Returns user's working copies.
     *
     * @param userName user name
     * @return list of vocabularies
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getWorkingCopies(String userName) throws ServiceException;

    /**
     * Returns vocabulary folder attributes metadata (without values).
     *
     * @return list of simple attributes
     * @throws ServiceException if operation fails
     */
    List<SimpleAttribute> getVocabularyFolderAttributesMetadata() throws ServiceException;

    /**
     * Get folder by folder identifier.
     *
     * @param folderIdentifier Folder unique textual identifier.
     * @return Folder object with metadata.
     * @throws ServiceException if operation fails Database error.
     */
    Folder getFolderByIdentifier(String folderIdentifier) throws ServiceException;

    /**
     * Adds data element to vocabulary folder.
     *
     * @param vocabularyFolderId vocabulary id
     * @param dataElementId      element id
     * @throws ServiceException if operation fails
     */
    void addDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Removes data element from vocabulary folder.
     *
     * @param vocabularyFolderId vocabulary id
     * @param dataElementId      element id
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
     *
     * @param vocabularyFolderId vocabulary Id
     * @param dataElementId      element id
     * @return true if binding exits
     * @throws ServiceException if query fails
     */
    boolean vocabularyHasDataElementBinding(int vocabularyFolderId, int dataElementId) throws ServiceException;

    /**
     * Returns list of other concepts that have this dataelement valued in a vocabulary. Checks only editable copy values
     *
     * @param dataElementId element ID
     * @param vocabularyId  vocabulary ID
     * @return list of elements
     * @throws ServiceException if query fails
     */
    List<VocabularyConcept> getConceptsWithElementValue(int dataElementId, int vocabularyId) throws ServiceException;

    /**
     * Returns all namespaces that are used by the bound elements. Both external and internal. Internal namespaces are composed
     * with prefix dd[element.idand uri BASE_URI/dataelement/[element.id]
     *
     * @param vocabularyFolders vocabularies
     * @return container of RDF Namespace objects
     * @throws ServiceException if query fails
     */
    List<RdfNamespace> getVocabularyNamespaces(List<VocabularyFolder> vocabularyFolders) throws ServiceException;

    /**
     * Some data element has special treatment. For example for handling relations between internal concepts we use skos:relation
     * elements that are in this Enum
     *
     * @author Kaido Laine
     */
    public enum RelationalElement {
        /**
         * relation elements.
         */
        BROADER_CONCEPT, NARROWER_CONCEPT, RELATED_CONCEPT;
    }

    /**
     * Checks if the given element represents relation to an external resource.
     *
     * @param id data element ID
     * @return true if element is relational (type = reference)
     */
    boolean isReferenceElement(int id);

    /**
     * Returns name list of bound elements. Each name is repeated as much times as is the max count of the element values for the concepts.
     *
     * @param concepts oncepts of the vocabulary
     * @return slit of bound element names
     */
    List<Triple<String, String, Integer>> getVocabularyBoundElementNamesByLanguage(List<VocabularyConcept> concepts);

    /**
     * Searches vocabularies by the given filter.
     *
     * @param filter filtering parameters
     * @return Result object containing found vocabularies
     * @throws ServiceException if operation fails
     */
    VocabularyResult searchVocabularies(VocabularyFilter filter) throws ServiceException;

    /**
     * Search form all vocabulary concepts.
     *
     * @param filter filter parameters
     * @return list of vocabulary concepts and parent objects: vocabulary, vocabulary set
     * @throws ServiceException if database call fails.
     */
    List<VocabularyConceptData> searchAllVocabularyConcept(VocabularyConceptFilter filter) throws ServiceException;

    /**
     * Sets relation to an external vocabulary.
     *
     * @param elementId    data element id data element id
     * @param vocabularyId vocabulary Id
     */
    void bindVocabulary(int elementId, int vocabularyId);

    /**
     * Returns a vocabulary with ALL concepts - obsolete and valid.
     *
     * @param identifier    vocabulary identifier
     * @param vocabularySet vocabulary set identifier
     * @return vocabulary entity with all concepts assigned
     */
    VocabularyFolder getVocabularyWithConcepts(String identifier, String vocabularySet);

    /**
     * Populates empty base uri values in vocabulary table.
     *
     * @param prefix base uri prefix
     * @return number of affected rows
     * @throws eionet.meta.service.ServiceException if operation fails
     */
    int populateEmptyBaseUris(String prefix) throws ServiceException;

    /**
     * Changes site prefixes in vocabulary base uris.
     *
     * @param oldSitePrefix old site prefix
     * @param newSitePrefix new site prefix
     * @return number of affected rows
     * @throws eionet.meta.service.ServiceException if operation fails
     */
    int changeSitePrefix(String oldSitePrefix, String newSitePrefix) throws ServiceException;

    /**
     * fix inverse relations in other concepts.
     *
     * @param vocabularyId this vocabulary ID
     * @param concepts     concepts of the vocabulary
     */
    void fixRelatedReferenceElements(int vocabularyId, List<VocabularyConcept> concepts);

    /**
     * fix inverse relations in local vocabulary other concepts for import.
     *
     * @param vocabularyId this vocabulary ID
     * @param concepts     concepts of the vocabulary
     */
    void fixRelatedLocalRefElementsForImport(int vocabularyId, List<VocabularyConcept> concepts) throws ServiceException;

    /**
     * Returns list of recently released vocabulary folders.
     *
     * @param limit maximum number of vocabulary folders
     * @return list of vocabulary folders
     * @throws ServiceException if operation fails
     */
    List<VocabularyFolder> getRecentlyReleasedVocabularyFolders(int limit) throws ServiceException;

    /**
     * Returns the list of concept ids for the vocabulary folder
     *
     * @param vocabularyFolderId vocabulary folder id
     * @return list of concept ids
     */
    List<Integer> getVocabularyConceptIds(int vocabularyFolderId);

    /**
     * Creates a filter for the bound data element based on the specified vocabulary concepts
     *
     * @param dataElementId bound data element id
     * @param vocabularyConceptIds list of vocabulary concept ids
     * @return dynamic filter
     */
    VocabularyConceptBoundElementFilter getVocabularyConceptBoundElementFilter(int dataElementId, List<Integer> vocabularyConceptIds);

    /**
     * Returns data element attributes for vocabulary concepts in a folder.
     * Precondition: If emptyAttributes true then this method should be called with a single concept id, i.e.
     *               vocabularyConceptIds.length should be 1.
     * @param vocabularyFolderId
     *            vocabularyID
     * @param vocabularyConceptIds
     *            concept IDs
     * @param emptyAttributes
     *            when true, then attributes that are not valued are also included.
     * @return map of list of lists where each list contains element values of one bound element
     */
    Map<Integer, List<List<DataElement>>> getVocabularyConceptsDataElementValues(int vocabularyFolderId,
            int[] vocabularyConceptIds, boolean emptyAttributes);

    /**
     * Returns true the given vocabulary concept exists in the given vocabulary in the given vocabulary set.
     *
     * @param vocabularySet
     * @param vocabularyIdentifier
     * @param conceptIdentifier
     * @return
     * @throws ServiceException
     */
    boolean vocabularyConceptExists(String vocabularySet, String vocabularyIdentifier, String conceptIdentifier) throws ServiceException;

    /**
     * Batch inserts the list of vocabulary concepts into the database with the provided batch size.
     * Returns the list of newly created concept ids
     */
    List<Integer> batchCreateVocabularyConcepts(List<VocabularyConcept> vocabularyConcepts, int batchSize) throws ServiceException;

    /**
     * Batch updates the list of vocabulary concepts into the database with the provided batch size.
     * Returns an array containing for each batch another array containing the numbers of rows affected by each update in the batch
     */
    int[][] batchUpdateVocabularyConcepts(List<VocabularyConcept> vocabularyConcepts, int batchSize) throws ServiceException;

    void batchUpdateVocabularyConceptsDataElementValues(List<VocabularyConcept> vocabularyConcepts, int batchSize) throws ServiceException;

    void batchFixRelatedReferenceElements(List<VocabularyConcept> concepts, int batchSize);

    void deleteVocabularyConcepts(int vocabularyFolderId);

    /**
    *Check if a Vocabulary is in working Copy Status. 
    **/
    boolean hasVocabularyWorkingCopy(String folderName, String identifier);

    VocabularyResult checkIfVocabulariesCanBeBoundToElements(VocabularyResult vocabularyResult) throws ServiceException;

    Boolean checkIfVocabularyIsBoundToElement(Integer vocabularyId);

    Integer getCheckedOutCopyIdForVocabulary(Integer vocabularyId);

    Boolean checkIfConceptShouldBeAddedWhenBoundToElement(Integer newVocabularyId, String notation);

    void updateAcceptedNotAcceptedDate(VocabularyConcept vocabularyConcept) throws ServiceException;
}
