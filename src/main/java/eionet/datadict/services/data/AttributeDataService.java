package eionet.datadict.services.data;

import eionet.datadict.model.Attribute;
import eionet.datadict.errors.ResourceNotFoundException;

/**
 *
 * @author Aliki Kopaneli
 */
public interface AttributeDataService {

    Attribute getAttribute(int id) throws ResourceNotFoundException;
    
    int createAttribute(Attribute attribute);
    
    void updateAttribute(Attribute attribute);
    
}
