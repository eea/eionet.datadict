package eionet.datadict.model;

public class DataElementWithQuantitativeValues extends DataElement {
    
    private FixedValueList suggestedValues;
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_ELEMENT_WITH_QUANTITATIVE_VALUES;
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
        return suggestedValues;
    }

    public FixedValueList getSuggestedValues() {
        return suggestedValues;
    }
    
    public void setSuggestedValues(FixedValueList valueList) {
        this.suggestedValues = valueList;
    }
    
}
