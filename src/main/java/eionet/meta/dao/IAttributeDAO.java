package eionet.meta.dao;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SimpleAttribute;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public interface IAttributeDAO {

    /**
     * Lists RDF namespaces.
     *
     * @return
     */
    List<RdfNamespace> getRdfNamespaces();

    /**
     *
     * @param parentId
     * @param parentType
     * @param newParentId
     */
    void copySimpleAttributes(int parentId, String parentType, int newParentId);

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
     * @return
     * @throws DAOException
     */
    List<Attribute> getAttributes(DElemAttribute.ParentType parentType) throws DAOException;

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
     * Returns simple attributes.
     *
     * @param parentId
     * @param parentType
     * @return
     */
    List<SimpleAttribute> getSimpleAttributeValues(int parentId, String parentType);

    /**
     * Returns attribute by shortName.
     *
     * @param shortName
     * @return
     */
    Attribute getAttributeByName(String shortName);

    /**
     * Returns attributes.
     *
     * @param vocabularyFolderId
     * @param emptyAttributes
     * @return
     */
    List<List<SimpleAttribute>> getVocabularyFolderAttributes(int vocabularyFolderId, boolean emptyAttributes);

    /**
     * First removes all the object's attributes and then inserts new ones.
     *
     * @param objectId
     * @param parentType
     * @param attributes
     */
    void updateSimpleAttributes(int objectId, String parentType, List<List<SimpleAttribute>> attributes);

    /**
     * Returns the attribute metadata according to the typeWeight (DISP_WHEN field).
     *
     * @param typeWeight
     * @return
     */
    List<SimpleAttribute> getAttributesMetadata(int typeWeight);

    /**
     * Returns a simple attribute with a specific id.
     * @param id the id for the attribute to fetch.
     * @return the corresponding {@link SimpleAttribute} instance if an attribute with the given id is found; null otherwise.
     */
    SimpleAttribute getById(int id);
    
    /**
     * Returns attribute's fixed values.
     * @param attributeId
     * @return list of fixed values
     */
    public List<FixedValue> getFixedValues(int attributeId);

    /**
     * updates an attribute value.
     * @param attrName attribute short name
     * @param dataElemId parent object ID
     * @param parentType parent type of an attribute
     * @param value new attr value
     */
    void updateSimpleAttributeValue(String attrName, int dataElemId , String parentType, String value);
    
}
