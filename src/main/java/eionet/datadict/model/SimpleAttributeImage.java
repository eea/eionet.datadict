package eionet.datadict.model;

public class SimpleAttributeImage extends SimpleAttribute {

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.IMAGE;
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
