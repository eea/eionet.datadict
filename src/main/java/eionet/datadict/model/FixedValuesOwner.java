package eionet.datadict.model;

import java.util.Set;

public interface FixedValuesOwner {

    Comparable getId();
    
    Set<FixedValue> getFixedValues();
    
    void setFixedValues(Set<FixedValue> fixedValues);
    
}
