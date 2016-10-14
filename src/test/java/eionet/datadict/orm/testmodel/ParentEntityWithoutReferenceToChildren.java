package eionet.datadict.orm.testmodel;

import javax.persistence.Id;

public class ParentEntityWithoutReferenceToChildren {
    
    @Id
    private Long id;
    
    private String value1;
    private String value2;
    private String value3;
    
    public ParentEntityWithoutReferenceToChildren() { }
    
    public ParentEntityWithoutReferenceToChildren(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        
        if (!(obj instanceof ParentEntityWithoutReferenceToChildren)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        ParentEntityWithoutReferenceToChildren other = (ParentEntityWithoutReferenceToChildren) obj;
        
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
