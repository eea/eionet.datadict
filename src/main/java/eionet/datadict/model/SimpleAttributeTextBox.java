package eionet.datadict.model;

public class SimpleAttributeTextBox extends SimpleAttribute {

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.TEXT_BOX;
    }

    @Override
    public boolean supportsValueList() {
        return false;
    }

    @Override
    public Iterable<FixedValue> getValueList() {
        throw new UnsupportedOperationException();
    }
    
}
