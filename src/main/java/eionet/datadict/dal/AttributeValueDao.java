package eionet.datadict.dal;

import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import java.util.List;

public interface AttributeValueDao {

    /**
     * Fetches the attribute values of the attribute with the given id,
     * corresponding to the given dataset, table or element.
     *
     * @param attributeId
     * @param owner the {@link DataDictEntity} which owns the attribute whose
     * values are to be fetched.
     * @return a list of {@link AttributeValue} objects.
     */
    public List<AttributeValue> getByAttributeAndOwner(int attributeId, DataDictEntity owner);

    /**
     * Fetches The attribute Value of the attribute with the given attribute Id and DataDict Entity Id
     * @param attributeId
     * @param dataDictEntityId the {@link DataDictEntity} Id which owns the attribute 
    **/
    public AttributeValue getByAttributeAndEntityId(int attributeId,int dataDictEntityId);
    /**
     * Fetches the AttributeValues by a given dataset , table or element
     *
     * @param owner the {@link DataDictEntity} which owns the attribute whose
     * values are to be fetched.
     * @return a list of {@link AttributeValue} objects.
     **
     */
    public List<AttributeValue> getByOwner(DataDictEntity owner);

    /**
     * Adds a list of attribute values for the attribute with the given id.
     *
     * @param attributeId the id of the attribute whose values are added.
     * @param owner the {@link DataDictEntity} who owns the attribute values.
     * @param values the values to be saved for the attribute with the given id.
     */
    public void addAttributeValues(int attributeId, DataDictEntity owner, List<String> values);

    /**
     * Deletes the specified attribute value of the attribute with the given id
     * which corresponds to the specified owner.
     *
     * @param attributeId the id of the attribute whose value is to be deleted.
     * @param owner the {@link DataDictEntity} which owns the attribute value.
     * @param value the value of the attribute value which is to be deleted.
     */
    public void deleteAttributeValue(int attributeId, DataDictEntity owner, String value);

    /**
     * Deletes all the attribute values of the attribute with the given id which
     * correspond to the specified owner.
     *
     * @param attributeId the id of the attribute whose values are to be
     * deleted.
     * @param owner the [@link DataDictEntity] which owns the attribute values.
     */
    public void deleteAllAttributeValues(int attributeId, DataDictEntity owner);

    /**
     * Deletes all the attribute values of the attribute with the given id.
     *
     * @param attributeId the id of the attribute whose values are to be
     * deleted.
     */
    public void deleteAllAttributeValues(int attributeId);
}
