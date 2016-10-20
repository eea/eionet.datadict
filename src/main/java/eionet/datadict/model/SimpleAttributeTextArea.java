package eionet.datadict.model;

public class SimpleAttributeTextArea extends SimpleAttribute {

    public SimpleAttributeTextArea() {
        super();
    }

    public SimpleAttributeTextArea(Integer id) {
        super(id);
    }
    
    @Override
    public DisplayType getDisplayType() {
        return DisplayType.TEXT_AREA;
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
        return obj instanceof SimpleAttributeTextArea;
    }
    
}
