package eionet.datadict.dal;

import eionet.datadict.model.DataSet;

public interface DatasetDao {
    
    /**
     * Fetch the dataset with the given id.
     * 
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     */
    public DataSet getById(int id);
}
