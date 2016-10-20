package eionet.datadict.dal;

import eionet.datadict.model.DataTable;

public interface DataTableDao {

    DataTable getDataTableById(int id);
    
}
