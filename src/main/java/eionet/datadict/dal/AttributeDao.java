package eionet.datadict.dal;

import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Aliki Kopaneli
 */
public interface AttributeDao {

    /**
     * Fetches an attribute with a given id.
     *
     * @param id the id of the attribute to be fetched.
     * @return an {@link Attribute} object corresponding to the given id.
     */
    public Attribute getById(int id);
    
    /**
     * Checks if an attribute with a given id exists.
     * 
     * @param id the id of the attribute.
     * @return a boolean value stating whether the attribute exists or not.
     */
    public boolean exists(int id);

    /**
     * Creates a new attribute.
     *
     * @param attribute the payload of the attribute to be created.
     * @return the id of the attribute that will be created.
     */
    public int create(Attribute attribute);
    
    /**
     * Updates an existing attribute.
     *
     * @param attribute the payload of the attribute to be updated.
     */
    public void update(Attribute attribute);
    
    /**
     * Updates the binding between an attribute and a vocabulary with given ids.
     * 
     * @param attributeId the id of the attribute to be updated.
     * @param vocabularyId the id of the vocabulary to be bound with the attribute.
     */
    public void updateVocabularyBinding(int attributeId, int vocabularyId);
    
    /**
     * Fetches the vocabulary associated with the attribute with the given id
     * 
     * @param attributeId the id of the attribute whose vocabulary binding is to be found.
     * @return the id of the vocabulary associated to the attribute with the given id 
     * or null if no such vocabulary is found.
     */
    public Integer getVocabularyBinding(int attributeId);
    
    /**
     * Deletes an attribute with the given id.
     *
     * @param id the id of the attribute to be deleted.
     */
    public void delete(int id);
    
    /**
     * Removes any vocabulary bindings of an attribute with a given id.
     * 
     * @param attributeId the id of the attribute whose vocabulary bindings to be removed..
     */
    public void deleteVocabularyBinding(int attributeId);
    
    /**
     * Deletes all values of an attribute with a given id.
     * 
     * @param attributeId the id of the attribute whose values will be deleted.
     */
    public void deleteValues(int attributeId);
    
    /**
     * Returns the number of values of an attribute with a given id
     * 
     * @param attributeId the id of the attribute whose values are to be counted 
     * @return  the number of values for the specified attribute
     */
    public int countAttributeValues(int attributeId);
    
    /**
     * Fetches information about all DD concepts for which the attribute of a given id has attribute values.
     * 
     * @param attributeId the id of the attribute.
     * @return a {@link java.util.Map} containing {@link DataDictEntity$Entity} keys and Integer values.
     */
    public Map<DataDictEntity.Entity, Integer> getConceptsWithAttributeValues(int attributeId);
    
    public List<Attribute> getCombinedDataSetAndDataTableAttributes(int datasetTableId,int dataSetId);
    
    public List<Attribute> getByDataDictEntity(DataDictEntity ownerEntity);
    
    List<Attribute> getAttributesOfDataTable(int tableId);
    
    List<AttributeValue> getAttributesValuesOfDataTable(int tableId);
    
    Map<Integer, Set<Attribute>> getAttributesOfDataElementsInTable(int tableId);

    List<Attribute> getAll();
}
