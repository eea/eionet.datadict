package eionet.datadict.model;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class FixedValue implements ValueListItem {

    @ManyToOne
    private FixedValuesOwner owner;
    
    @Id
    private Integer id;
    private String code;
    private String label;
    private String definition;

    public FixedValuesOwner getOwner() {
        return owner;
    }

    public void setOwner(FixedValuesOwner owner) {
        this.owner = owner;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
    
    @Override
    public String getDefinition() {
        return this.definition;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof FixedValue)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        FixedValue other = (FixedValue) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}
