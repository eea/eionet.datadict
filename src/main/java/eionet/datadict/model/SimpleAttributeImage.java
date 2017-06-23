package eionet.datadict.model;

public class SimpleAttributeImage extends SimpleAttribute {

    public SimpleAttributeImage() {
        super();
    }

    public SimpleAttributeImage(Integer id) {
        super(id);
    }
    
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
    
    @Override
    protected boolean isInstanceOfClass(Object obj) {
        return obj instanceof SimpleAttributeImage;
    }
    
}
