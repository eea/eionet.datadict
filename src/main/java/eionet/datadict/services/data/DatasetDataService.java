package eionet.datadict.services.data;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Dataset;


public interface DatasetDataService {
    
    /**
     * Fetches the dataset with the given id.
     * 
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     * 
     * @throws ResourceNotFoundException 
     */
    public Dataset getDataset(int id) throws ResourceNotFoundException;
}