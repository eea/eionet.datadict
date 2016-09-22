package eionet.datadict.model;

public class SimpleAttributeTextArea extends SimpleAttribute {

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.TEXT_AREA;
    }

    @Override
    public boolean supportsValueList() {
        return false;
    }

    @Override
    public ValueList getValueList() {
        throw new UnsupportedOperationException();
    }
    
}
