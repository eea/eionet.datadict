package eionet.datadict.model;

public class SimpleAttributeFixedValues extends SimpleAttribute {

    private FixedValueList fixedValueList;
    
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
        return fixedValueList;
    }
    
    public void setValueList(FixedValueList fixedValueList) {
        this.fixedValueList = fixedValueList;
    }
    
}
