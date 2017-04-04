package eionet.datadict.services.data;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DatasetTable;

public interface DataTableDataService {

    DatasetTable getFullDatasetTableDefinition(int tableId) throws ResourceNotFoundException;
    
}
