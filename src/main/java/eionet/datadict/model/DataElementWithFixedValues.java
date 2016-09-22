package eionet.datadict.model;

public class DataElementWithFixedValues extends DataElement {
    
    private FixedValueList fixedValues;
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_ELEMENT_WITH_VALUE_LIST;
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.FIXED;
    }

    @Override
    public boolean supportsValueList() {
        return true;
    }

    @Override
    public FixedValueList getValueList() {
        return fixedValues;
    }

    public FixedValueList getFixedValues() {
        return fixedValues;
    }
    
    public void setFixedValues(FixedValueList valueList) {
        this.fixedValues = valueList;
    }
    
}
