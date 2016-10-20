package eionet.datadict.dal;

import eionet.datadict.model.SimpleAttribute;
import eionet.datadict.model.SimpleAttributeValues;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SimpleAttributeDao {

    List<SimpleAttribute> getSimpleAttributesOfDataTable(int tableId);
    
    List<SimpleAttributeValues> getSimpleAttributesValuesOfDataTable(int tableId);
    
    Map<Integer, Set<SimpleAttribute>> getSimpleAttributesOfDataElementsInTable(int tableId);
    
    List<SimpleAttributeValues> getSimpleAttributesValuesOfDataElementsInTable(int tableId);
    
}
