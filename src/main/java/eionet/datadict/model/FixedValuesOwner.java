package eionet.datadict.model;

public interface FixedValuesOwner {

    Comparable getId();
    
    FixedValueList getFixedValues();
    
    void setFixedValues(FixedValueList fixedValues);
    
}
