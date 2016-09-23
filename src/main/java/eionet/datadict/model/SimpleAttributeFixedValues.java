package eionet.datadict.model;

public class SimpleAttributeFixedValues extends SimpleAttribute {

    private FixedValueList fixedValues;
    
    @Override
    public DisplayType getDisplayType() {
        return DisplayType.DROPDOWN_FIXED_VALUES;
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
    
    public void setFixedValues(FixedValueList fixedValues) {
        this.fixedValues = fixedValues;
    }
    
}
