package eionet.datadict.model;

import java.util.List;

public class SimpleAttributeValues {
    
    private SimpleAttributeOwner owner;
    private SimpleAttribute attribute;
    private List<String> values;

    public SimpleAttributeOwner getOwner() {
        return owner;
    }

    public void setOwner(SimpleAttributeOwner owner) {
        this.owner = owner;
    }

    public SimpleAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(SimpleAttribute attribute) {
        this.attribute = attribute;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
    
}
