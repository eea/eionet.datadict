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
    public Iterable<FixedValue> getValueList() {
        throw new UnsupportedOperationException();
    }
    
}
