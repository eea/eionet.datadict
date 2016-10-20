package eionet.datadict.model;

public class SimpleAttributeTextBox extends SimpleAttribute {

    public SimpleAttributeTextBox() {
        super();
    }

    public SimpleAttributeTextBox(Integer id) {
        super(id);
    }
    
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
    
    @Override
    protected boolean isInstanceOfClass(Object obj) {
        return obj instanceof SimpleAttributeTextBox;
    }
    
}
