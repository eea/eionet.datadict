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

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.InferenceRule;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.DataElementsFilter;

import java.sql.SQLException;
import eionet.meta.service.data.VocabularyConceptBoundElementFilter;
import eionet.util.Pair;
import eionet.util.Triple;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data element DAO.
 *
 * @author Juhan Voolaid
 */
public interface IDataElementDAO {

    /**
     * Search data elements.
     *
     * @param filter
     *            search filter
     * @return list of data elements
     */
    List<DataElement> searchDataElements(DataElementsFilter filter);

    /**
     * Returns data element attributes.
     *
     * @return list of data element attributes
     * @throws java.sql.SQLException
     *             if dao fails
     */
    List<Attribute> getDataElementAttributes() throws SQLException;

    /**
     * Returns data element's fixed values.
     *
     * @param dataElementId
     *            dataelem id
     * @return list of fixed values
     */
    List<FixedValue> getFixedValues(int dataElementId);

    /**
     * Returns data element by id.
     *
     * @param id
     *            data elem id
     * @return return data element
     */
    DataElement getDataElement(int id);

    /**
     * Returns latest version of the data element.
     *
     * @param identifier
     *            data element identifier
     * @return element
     */
    DataElement getDataElement(String identifier);

    /**
     * Checks if data element with specific id exists
     * @param id
     * @return 
     */
    boolean dataElementExists(int id);

    /**
     * Returns latest version of the COMMON data element id.
     *
     * @param identifier
     *            common element identifier
     * @return ID in DATAELEM table
     */
    int getCommonDataElementId(String identifier);

    /**
     * Returns the parent dataset of the non-common element with the given id.
     * 
     * @param dataElementId the id of a non-common element
     * @return a {@link eionet.meta.dao.domain.DataSet} instance of the parent of the data element; null if no parent is found.
     */
    DataSet getParentDataSet(int dataElementId);

    /**
     *
     * @param dataElementId
     *            dataelem id
     * @return data type of data element
     */
    String getDataElementDataType(int dataElementId);

    /**
     * Adds data element to vocabulary folder.
     *
     * @param vocabularyFolderId
     *            vocabulary Id
     * @param dataElementId
     *            dataelem id
     */
    void addDataElement(int vocabularyFolderId, int dataElementId);

    /**
     * Removes data element from vocabulary folder.
     *
     * @param vocabularyFolderId
     *            vocabulary Id
     * @param dataElementId
     *            dataelem id
     */
    void removeDataElement(int vocabularyFolderId, int dataElementId);

    /**
     * Returns data elements bound with vocabulary folder.
     *
     * @param vocabularyFolderId
     *            folder ID
     * @return list of data elements for the Vocabulary
     */
    List<DataElement> getVocabularyDataElements(int vocabularyFolderId);

    /**
     * Deletes all vocabulary's data element relations.
     *
     * @param vocabularyFolderId
     *            vocabulary Id
     */
    void deleteVocabularyDataElements(int vocabularyFolderId);

    /**
     * Deletes all vocabulary concept's data element values.
     *
     * @param vocabularyConceptId
     *            concept ID concept ID
     */
    void deleteVocabularyConceptDataElementValues(int vocabularyConceptId);

    /**
     * Inserts data element values.
     *
     * @param vocabularyConceptId
     *            concept ID
     * @param dataElementValues
     *            list of data elements to be added for concept
     */
    void insertVocabularyConceptDataElementValues(int vocabularyConceptId, List<DataElement> dataElementValues);

    /**
     * Moves all vocabulary's data element relations to other vocabulary.
     *
     * @param sourceVocabularyFolderId
     *            source vocabulary id
     * @param targetVocabularyFolderId
     *            target vocabulary id
     */
    void moveVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId);

    /**
     * Copy all vocabulary's data element relations to other vocabulary.
     *
     * @param sourceVocabularyFolderId
     *            source vocabulary id
     * @param targetVocabularyFolderId
     *            target vocabulary id
     */
    void copyVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId);

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
     * Copies data element values from old concepts to new concepts. Can be used when checking out the vocabulary
     *
     * @param newVocabularyFolderId
     *            new vocabulary Folder ID
     */
    void checkoutVocabularyConceptDataElementValues(int newVocabularyFolderId);

    /**
     * Copies data element values from old vocabulary concepts to new vocabulary concepts.
     *
     * @param oldVocabularyFolderId
     *            old vocabulary Folder ID
     * @param newVocabularyFolderId
     *            new vocabulary Folder ID
     */
    void copyVocabularyConceptDataElementValues(int oldVocabularyFolderId, int newVocabularyFolderId);

    /**
     * Checks if the vocabulary has binding of this element.
     *
     * @param vocabularyFolderId
     *            vocabulary Id
     * @param elementId
     *            element id
     * @return true if binding exists
     */
    boolean vocabularyHasElemendBinding(int vocabularyFolderId, int elementId);

    /**
     * Updates the VOCABULARY_CONCEPT_ELEMENT.RELATED_CONCEPT_ID to what the new checked out concept's id currently is.
     *
     * @param newVocabularyFolderId
     */
    // void updateRelatedConceptIds(int newVocabularyFolderId);

    /**
     * Deletes related concept elements of this concept.
     *
     * @param vocabularyConceptId
     *            concept Id
     */
    void deleteRelatedElements(int vocabularyConceptId);

    /**
     * Finds element attribute values for the data element.
     *
     * @param elementId
     *            element ID
     * @return Map where key is attribute name and value is list of element values
     */
    Map<String, List<String>> getDataElementAttributeValues(int elementId);

    /**
     * Finds unique set of elements used in all dataset tables.
     *
     * @param datasetId
     *            dataset id
     * @return distinct collection of data elements
     */
    List<DataElement> getDataSetElements(int datasetId);

    /**
     * Sets relation to an external vocabulary.
     *
     * @param elementId
     *            data element id
     * @param vocabularyId
     *            vocabulary Id
     */
    void bindVocabulary(int elementId, int vocabularyId);

    /**
     * Finds list of elements where given vocabularies is used as source for values.
     *
     * @param vocabularyIds
     *            vocabulary ids
     * @return collection of data elements
     */
    List<DataElement> getVocabularySourceElements(List<Integer> vocabularyIds);

    /**
     * changes vocabulary reference in CH3 - fxv vocabulary elements.
     *
     * @param originalVocabularyId
     *            old vocabulary ID
     * @param vocabularyId
     *            new vocabulary ID
     */
    void moveVocabularySources(int originalVocabularyId, int vocabularyId);

    /**
     * Calls stored procedure that fixes relational elements.
     *
     * @param dataElementId
     *            data element id
     * @param conceptId
     *            vocabulary concept id
     * @param newRelationalConceptId
     *            new value of the relational concept ID - null if changed to empty
     */
    void createInverseElements(int dataElementId, int conceptId, Integer newRelationalConceptId);

    /**
     * Delete element values in this vocabulary where this concept is referred as related element.
     *
     * @param conceptId
     *            concept id
     * @param dataElements
     *            list of data elements
     */
    void deleteReferringInverseElems(int conceptId, List<DataElement> dataElements);

    /**
     * deletes inverse elements of the element where this concept id is referred.
     *
     * @param conceptId
     *            concept id
     * @param dataElement
     *            data element
     */
    void deleteInverseElemsOfConcept(int conceptId, DataElement dataElement);

    /**
     * Returns inverse element ID if exists.
     *
     * @param dataElementId
     *            element id
     * @return data element id or null if no inverse element
     */
    int getInverseElementID(int dataElementId);

    /**
     * deletes references in other vocabularies for this vocabulary concepts.
     *
     * @param vocabularyId
     *            vocabulary id
     */
    void deleteReferringReferenceElems(int vocabularyId);

    /**
     * Change the given data element's type to the given value.
     *
     * @param elemId
     *            Element id.
     * @param newType
     *            The new type's identifier.
     */
    void changeDataElemType(int elemId, String newType);

    /**
     * Removes the given data element's simple attributes by the given short names.
     *
     * @param elemId
     *            Data element id.
     * @param attrShortNames
     *            Short names of simple attributes to remove.
     */
    void removeSimpleAttrsByShortName(int elemId, String... attrShortNames);

    /**
     * Returns vocabulary concept elements if they may refer other concepts.
     * {@code
     * //!!! ATTENTION: related concept id field is used to store vocabulary id temporarily.
     * //e.g.:
     * DataElement.setRelatedConceptId(rs.getInt("potential_related_vocabulary_id"));
     * }
     *
     * @return list of elements
     */
    List<DataElement> getPotentialReferringVocabularyConceptsElements();

    /**
     * Returns inference rules of specific element 
     * 
     * @param parentElem
     * @return List of rules
     */
    Collection<InferenceRule> getInferenceRules(DataElement parentElem);

    /**
     * Returns inference rules of specific element (not fetching Data Elements, only their IDs)
     * @param parentElem
     * @return List of rules
     */
    Collection<InferenceRule> listInferenceRules(DataElement parentElem);

    /**
     * Creates new inference rule for specific element
     * @param rule 
     */
    void createInferenceRule(InferenceRule rule);

    /**
     * Deletes specific inference rule
     * @param rule 
     */
    void deleteInferenceRule(InferenceRule rule);

    /**
     * Checks if specific inference rule exists
     * @param rule 
     * @return  
     */
    boolean inferenceRuleExists(InferenceRule rule);

    /**
     * Updates rule to newRule
     * @param rule 
     * @param newRule 
     */
    void updateInferenceRule(InferenceRule rule, InferenceRule newRule);
    
    /**
     * Searches for data elements whose short name includes pattern
     * @param pattern
     * @return 
     */
    Collection<DataElement> grepDataElement(String pattern);

    /**
     * Updates given concept element value (VOCABULARY_CONCEPT_ELEMENT)
     * @param id id in the table
     * @param value new value
     * @param language language code
     * @param relatedConceptId related concept id
     */
    void updateVocabularyConceptDataElementValue(int id, String value, String language, Integer relatedConceptId);

    /**
     * Creates a filter for the bound data element based on the specified vocabulary concepts
     *
     * @param dataElementId bound data element id
     * @param vocabularyConceptIds list of vocabulary concept ids
     * @return dynamic filter
     */
    VocabularyConceptBoundElementFilter getVocabularyConceptBoundElementFilter(int dataElementId, List<Integer> vocabularyConceptIds);

    void deleteVocabularyConceptDataElementValues(List<Integer> vocabularyConceptIds);

    int[][] batchInsertVocabularyConceptDataElementValues(List<VocabularyConcept> vocabularyConcepts, int batchSize);

    int[][] batchCreateInverseElements(List<Triple<Integer, Integer, Integer>> relatedReferenceElements, int batchSize);

    Map<Integer, String> getDataElementDataTypes(Collection<Integer> dataElementIds);

    List<DataElement> getCommonDataElementsWorkingCopiesOf(String userName);

    List<Integer> getOrphanNonCommonDataElementIds();

    int delete(List<Integer> ids);

    void deleteRelatedConcepts(int dataElementId, Collection<Integer> relatedConceptIds);

    int[][] batchCreateVocabularyBoundElements(List<Pair<Integer, Integer>> vocabularyIdToDataElementId, int batchSize);

    int[][] batchCreateInverseRelations(List<Triple<Integer, Integer, Integer>> relatedReferenceElements, int batchSize);

}
