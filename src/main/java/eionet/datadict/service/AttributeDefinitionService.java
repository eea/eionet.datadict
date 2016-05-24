package eionet.datadict.service;

import eionet.datadict.model.AttributeDefinition;
import eionet.meta.application.errors.ResourceNotFoundException;

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
