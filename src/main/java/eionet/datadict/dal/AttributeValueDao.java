package eionet.datadict.dal;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import java.util.List;


public interface AttributeValueDao {
    
    /**
     * Fetches the attribute values of the attribute with the given id, corresponding to the given dataset, table or element.
     * 
     * @param attributeId
     * @param owner the {@link DataDictEntity} which owns the attribute whose values are to be fetched.
     * @return a list of {@link AttributeValue} objects.
     */
    public List<AttributeValue> getByAttributeAndOwner(int attributeId, DataDictEntity owner);
    
    /**
     * Adds an attribute value for the attribute with the given id.
     *
     * @param attributeId the id of the attribute whose value is added.
     * @param owner the {@link DataDictEntity} who owns the attribute
     * value.
     * @param value the value to be saved for the attribute with the given id.
     * 
     * @throws eionet.datadict.errors.DuplicateResourceException
     */
    public void addAttributeValue(int attributeId, DataDictEntity owner, String value) throws DuplicateResourceException;

    /**
     * Deletes the specified attribute value of the attribute with the given id which corresponds to the specified owner.
     * 
     * @param attributeId the id of the attribute whose value is to be deleted.
     * @param owner the {@link DataDictEntity} which owns the attribute value.
     * @param value the value of the attribute value which is to be deleted. 
     */
    public void deleteAttributeValue(int attributeId, DataDictEntity owner, String value);
    
   /**
    * Deletes all the attribute values of the attribute with the given id which correspond to the specified owner.
    * 
    * @param attributeId the id of the attribute whose values are to be deleted.
    * @param owner the [@link DataDictEntity] which owns the attribute values. 
    */
    public void deleteAllAttributeValues(int attributeId, DataDictEntity owner);
}
