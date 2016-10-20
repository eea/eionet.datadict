package eionet.datadict.dal;

import eionet.datadict.model.DataTableElement;
import java.util.List;

public interface DataElementDao {

    List<DataTableElement> getDataElementsOfDataTable(int tableId);
    
}
