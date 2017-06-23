package eionet.datadict.orm.testmodel;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class ChildEntityUnreferencedByParent {
    
    @Id
    private Long id;
    
    @ManyToOne
    private ParentEntityWithoutReferenceToChildren parent;
    
    private String value1;
    private String value2;
    private String value3;

    public ChildEntityUnreferencedByParent() { }
    
    public ChildEntityUnreferencedByParent(Long id, ParentEntityWithoutReferenceToChildren parent) {
        this.id = id;
        this.parent = parent;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ParentEntityWithoutReferenceToChildren getParent() {
        return parent;
    }

    public void setParent(ParentEntityWithoutReferenceToChildren parent) {
        this.parent = parent;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof ChildEntityUnreferencedByParent)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        ChildEntityUnreferencedByParent other = (ChildEntityUnreferencedByParent) obj;
        
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        if (this.id == null) {
            return super.hashCode();
        }
        
        return this.id.hashCode();
    }
    
}
