package eionet.datadict.model;

public class DataElementWithQuantitativeValues extends DataElement implements FixedValuesOwner {
    
    private FixedValueList fixedValues;
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_ELEMENT_WITH_QUANTITATIVE_VALUES;
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.QUANTITATIVE;
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
