package eionet.datadict.dal;

import eionet.datadict.model.Dataset;

public interface DatasetDao {
    
    /**
     * Fetch the dataset with the given id.
     * 
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     */
    public Dataset getById(int id);
}
