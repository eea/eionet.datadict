package eionet.datadict.services.data;

import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.errors.ResourceNotFoundException;

/**
 *
 * @author Aliki Kopaneli
 */
public interface AttributeDefinitionService {

    /**
     * 
     * @param id
     * @return
     * @throws ResourceNotFoundException 
     */
    AttributeDefinition getAttributeDefinitionById(int id) throws ResourceNotFoundException;
    
}
