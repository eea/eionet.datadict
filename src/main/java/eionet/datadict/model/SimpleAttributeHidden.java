package eionet.datadict.model;

public class SimpleAttributeHidden extends SimpleAttribute {

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.NONE;
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
