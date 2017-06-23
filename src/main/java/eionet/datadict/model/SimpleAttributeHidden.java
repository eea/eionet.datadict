package eionet.datadict.model;

public class SimpleAttributeHidden extends SimpleAttribute {

    public SimpleAttributeHidden() {
        super();
    }

    public SimpleAttributeHidden(Integer id) {
        super(id);
    }
    
    @Override
    public DisplayType getDisplayType() {
        return DisplayType.NONE;
    }

    @Override
    public boolean supportsValueList() {
        return false;
    }

    @Override
    public Iterable<FixedValue> getValueList() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected boolean isInstanceOfClass(Object obj) {
        return obj instanceof SimpleAttributeHidden;
    }
    
}
