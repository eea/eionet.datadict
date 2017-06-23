package eionet.datadict.model;

import java.util.Arrays;
import java.util.List;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

public class SimpleAttributeValues {
    
    @OneToOne
    private AttributeOwner owner;
    @ManyToOne
    private SimpleAttribute attribute;
    private List<String> values;

    public AttributeOwner getOwner() {
        return owner;
    }

    public void setOwner(AttributeOwner owner) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof SimpleAttributeValues)) {
            return false;
        }
        
        if (this.owner == null || this.attribute == null) {
            return false;
        }
        
        SimpleAttributeValues other = (SimpleAttributeValues) obj;
        
        return this.owner.equals(other.owner) && this.attribute.equals(other.attribute);
    }

    @Override
    public int hashCode() {
        if (this.owner == null || this.attribute == null) {
            return super.hashCode();
        }
        
        return Arrays.hashCode(new Object[] { this.owner, this.attribute });
    }
    
}
