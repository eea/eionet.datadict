package eionet.datadict.dal;

import eionet.datadict.model.Attribute;

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
}
