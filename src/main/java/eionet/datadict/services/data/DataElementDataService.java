package eionet.datadict.services.data;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataElement;
import eionet.meta.DDUser;


public interface DataElementDataService {
    
    /**
     * Fetches the data element with the given id.
     * 
     * @param dataElementId the id of the data element to be fetched.
     * @return the {@link DataElement} with the given id.
     * 
     * @throws ResourceNotFoundException 
     */
    public DataElement getDataElement(int dataElementId) throws ResourceNotFoundException;
    
    public boolean isWorkingUser(DataElement dataElement, DDUser user);
}
