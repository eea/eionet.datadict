
package eionet.datadict.services.data;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DatasetTable;
import eionet.meta.DDUser;


public interface DatasetTableDataService {
    
    /**
     * Fetches the dataset table with the given id.
     * 
     * @param id the id of the dataset table to be fetched.
     * @return the {@link DatasetTable} with the given id.
     * 
     * @throws ResourceNotFoundException 
     */
    public DatasetTable getDatasetTable(int id) throws ResourceNotFoundException;
    
    public boolean isWorkingCopy(DatasetTable table, DDUser user);
}
