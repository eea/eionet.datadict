package eionet.datadict.dal;

import eionet.datadict.model.AttributeDefinition;
import eionet.meta.application.errors.ResourceNotFoundException;
import java.util.List;

/**
 *
 * @author eworx-alk
 */
public interface AttributeDefinitionDAO {

    public List<AttributeDefinition> getAttributes();

    public AttributeDefinition getAttributeDefinitionById(int id) throws ResourceNotFoundException;
    
    public void save(AttributeDefinition attrDef);
    
    public void delete(int id);
}
