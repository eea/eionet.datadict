package eionet.datadict.dal;

import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.errors.ResourceNotFoundException;

/**
 *
 * @author Aliki Kopaneli
 */
public interface AttributeDefinitionDAO {

    /**
     * Fetch the attribute definition by id
     *
     * @param id
     * @return
     * @throws ResourceNotFoundException
     */
    public AttributeDefinition getAttributeDefinitionById(int id) throws ResourceNotFoundException;

    /**
     * Updates an existing M_ATTRIBUTE
     *
     * @param attrDef
     */
    public void update(AttributeDefinition attrDef);

    /**
     * Adds a new M_ATTRIBUTE
     *
     * @param attrDef
     * @return
     */
    public int add(AttributeDefinition attrDef);

    /**
     * Delete the M_ATTRIBUTE
     *
     * @param id
     */
    public void delete(int id);

    /**
     * Update the table holding the relation between the attribute definition
     * and one vocabulary
     * @param attrDefId
     * @param vocId 
     */
    public void updateAttributeDefinitionVocabulary(int attrDefId, int vocId);
    
    /**
     * Remove any relation between the attribute definition and any vocabulary in
     * the M_ATTRIBUTE_VOCABULARY table
     * 
     * @param attrDefId 
     */
    public void removeAttributeDefinitionVocabulary(int attrDefId);
}
