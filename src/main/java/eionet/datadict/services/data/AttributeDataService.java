package eionet.datadict.services.data;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.model.Attribute;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.meta.dao.domain.FixedValue;
import java.util.List;
import java.util.Map;


public interface AttributeDataService {

    /**
     * Checks if the attribute with the given id exists.
     * 
     * @param id the id of the attribute.
     * @return true if the attribute exists, false otherwise.
     */
    boolean existsAttribute(int id);
    
    /**
     * Fetches the attribute with the given id.
     * 
     * @param id the id of the attribute to be saved.
     * @return the {@link Attribute} with the given id.
     * 
     * @throws ResourceNotFoundException 
     */
    Attribute getAttribute(int id) throws ResourceNotFoundException;

    List<Attribute> getAllAttributes();

    /**
     * Fetches the original (not inherited) attribute values of the attribute with the given id corresponding to the specified owner.
     * 
     * @param attributeId the id of the attribute whose values are to be fetched.
     * @param owner the {@link DataDictEntity] which owns the attribute values to be fetched.
     * @return a {@link List} of the {@link AttributeValue} objects.
     */
    List<AttributeValue> getOriginalAttributeValues(int attributeId, DataDictEntity owner);
    
    /**
     * Fetches the inherited attribute values of the attribute with the given id corresponding to the specified owner.
     * @param attributeId the id of the attribute whose values are to be fetched.
     * @param owner the {@link DataDictEntity] which owns the attribute values to be fetched.
     * @return a {@link List} of the {@link AttributeValue} objects.
     * 
     * @throws ResourceNotFoundException thrown when parent was not found (while searching for inheritance). 
     */
    List<AttributeValue> getInheritedAttributeValues(int attributeId, DataDictEntity owner) throws ResourceNotFoundException;
    
    /**
     * Fetches all attribute values of the attribute with the given id corresponding to the specified owner according to the level of inheritance
     * .
     * @param attributeId the id of the attribute whose values are to be fetched.
     * @param owner the {@link DataDictEntity] which owns the attribute values to be fetched.
     * @param inheritance the {@link Attribute.ValueInheritanceMode} of the attribute whose values are to be fetched.
     * @return {@link List} of the {@link AttributeValue} objects.
     * 
     * @throws ResourceNotFoundException thrown when parent is not found (while searching for inheritance).
     * @throws EmptyParameterException thrown when inheritance is null.
     */
    List<AttributeValue> getAttributeValues(int attributeId, DataDictEntity owner, Attribute.ValueInheritanceMode inheritance) 
            throws ResourceNotFoundException, EmptyParameterException;
    
    /**
     * Counts the attribute values of the attribute with the given id.
     * 
     * @param attributeId the id of the attribute whose values are to be counted.
     * @return the number of the attribute values of the attribute with the given id.
     */
    int countAttributeValues(int attributeId);
    
    /**
     * Fetches the fixed values corresponding to the attribute with the given id.
     * 
     * @param attributeId the id of the attribute whose values are to be fetched.
     * @return a [@link List} of {@link FixedValue} objects.
     */
    List<FixedValue> getFixedValues(int attributeId);

    /**
     * Creates the given attribute.
     * 
     * @param attribute the {@link Attribute} to be created.
     * @return the id of the created attribute.
     */
    int createAttribute(Attribute attribute);
    
    /**
     * Updates the given attribute.
     * 
     * @param attribute the {@link Attribute} to be updated.
     */
    void updateAttribute(Attribute attribute);
    
    /**
     * Deletes the attribute with the given id.
     * 
     * @param attributeId the id of the attribute to be deleted.
     */
    void deleteAttributeById(int attributeId);
    
    /**
     * Deletes all fixed values corresponding to the attribute with the given id.
     * 
     * @param attributeId the id of the attribute whose fixed values are to be deleted.
     */
    void deleteRelatedFixedValues(int attributeId);

     /**
     * Fetches information about all DD concepts for which the attribute of a given id has attribute values.
     * 
     * @param attributeId the id of the attribute.
     * @return a {@link java.util.Map} containing {@link DataDictEntity$Entity} keys and Integer values.
     */
    Map<DataDictEntity.Entity, Integer> getDistinctTypesWithAttributeValues(int attributeId);
    
    List<Attribute> getAllByDatasetId(int datasetId);
}
