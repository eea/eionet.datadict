package eionet.datadict.dal;

import eionet.datadict.model.DatasetTable;


public interface DatasetTableDao {
    
    /**
     * Fetches the dataset table with the given id.
     * 
     * @param id the id of the dataset table to be fetched.
     * @return the {@link DatasetTable} with the given id.
     */
    public DatasetTable getById(int id); 
    
    /**
     * Fetches the id of the parent dataset of the dataset table with the given id.
     * 
     * @param tableId the id of the table whose parent dataset is to be fetched.
     * @return an {@link Integer} which corresponds to the id of the parent dataset of the table with the given id.
     */
    public Integer getParentDatasetId(int tableId);
}
