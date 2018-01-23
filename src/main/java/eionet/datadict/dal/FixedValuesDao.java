package eionet.datadict.dal;

import eionet.datadict.model.FixedValue;
import java.util.List;

public interface FixedValuesDao {

    List<FixedValue> getValueListCodesOfDataElementsInTable(int tableId);
    
    List<FixedValue> getFixedValues(int DataElementId);
}
