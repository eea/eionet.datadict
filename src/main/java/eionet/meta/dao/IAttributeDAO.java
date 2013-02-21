package eionet.meta.dao;

import java.util.List;
import java.util.Map;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;
import eionet.meta.dao.domain.VocabularyConceptAttribute;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public interface IAttributeDAO {

    /**
     *
     * @param parentId
     * @param parentType
     * @param newParentId
     */
    void copySimpleAttributes(int parentId, String parentType, int newParentId);

    /**
     *
     * @param parentId
     * @param parentType
     * @param newParentId
     */
    void copyComplexAttributes(int parentId, String parentType, int newParentId);

    /**
     * Deletes all attributes of given parent ids.
     *
     * @param parentIds
     * @param parentType
     */
    void deleteAttributes(List<Integer> parentIds, String parentType);

    /**
     *
     * @param replacedId
     * @param substituteId
     * @param parentType
     */
    void replaceParentId(int replacedId, int substituteId, DElemAttribute.ParentType parentType);

    /**
     * Wraps the DDSearchEngine functionality for getting attributes meta data.
     *
     * @param parentType
     * @param attributeType
     * @return
     * @throws DAOException
     */
    List<Attribute> getAttributes(DElemAttribute.ParentType parentType, String attributeType) throws DAOException;

    /**
     * Returns the values of simple attributes of a parent identified by the given parent id and parent type. The type of returned
     * attributes is given in the method inputs.
     *
     * The method returns a map where the keys are the attributes' short names, and the values are the attributes's values. The type
     * of values is List<String>, as an attribute could have many values.
     *
     * @param parentId
     * @param parentType
     * @return
     */
    Map<String, List<String>> getAttributeValues(int parentId, String parentType);

    /**
     * Returns attribute by shortName.
     *
     * @param shortName
     * @return
     */
    Attribute getAttributeByName(String shortName);

    /**
     * Returns complex attribute object by name.
     *
     * @param complexAttrName
     *            Exact name of attribute.
     * @return CoplexAttribute object
     */
    ComplexAttribute getComplexAttributeByName(String complexAttrName);

    /**
     * Returns dynamic attributes for vocabulary concept.
     *
     * @return
     */
    List<List<VocabularyConceptAttribute>> getVocabularyConceptAttributes(int vocabularyConceptId);

    void createVocabularyConceptAttributes(List<VocabularyConceptAttribute> attributes);

    void updateVocabularyConceptAttributes(List<VocabularyConceptAttribute> attributes);

    void deleteVocabularyConceptAttributes(List<Integer> excludedIds, int vocabularyConceptId);
}
