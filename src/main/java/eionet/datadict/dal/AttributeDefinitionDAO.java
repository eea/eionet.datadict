package eionet.datadict.dal;

import eionet.datadict.model.AttributeDefinition;
import eionet.meta.application.errors.ResourceNotFoundException;

/**
 *
 * @author eworx-alk
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
}
