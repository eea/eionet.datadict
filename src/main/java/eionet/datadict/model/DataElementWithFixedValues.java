package eionet.datadict.model;

public class DataElementWithFixedValues extends DataElement implements FixedValuesOwner {
    
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

    @Override
    public FixedValueList getFixedValues() {
        return fixedValues;
    }
    
    @Override
    public void setFixedValues(FixedValueList fixedValues) {
        this.fixedValues = fixedValues;
    }
    
}
