package eionet.datadict.services.data;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataTable;

public interface DataTableDataService {

    DataTable getFullDataTableDefinition(int tableId) throws ResourceNotFoundException;
    
}
