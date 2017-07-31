package eionet.datadict.orm.testmodel;

import java.util.Collection;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

public class ParentEntityWithReferenceToChildren {
    
    @Id
    private Long id;
    
    private String value1;
    private String value2;
    private String value3;

    @OneToOne(mappedBy = "parent")
    private ParentExtentionEntityReferencedByParent extension;
    
    @OneToMany(mappedBy = "parent")
    private Collection<ChildEntityReferencedByParent> children;
    
    public ParentEntityWithReferenceToChildren() { }
    
    public ParentEntityWithReferenceToChildren(Long id) {
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

    public ParentExtentionEntityReferencedByParent getExtension() {
        return extension;
    }

    public void setExtension(ParentExtentionEntityReferencedByParent extension) {
        this.extension = extension;
    }
    
    public Collection<ChildEntityReferencedByParent> getChildren() {
        return children;
    }

    public void setChildren(Collection<ChildEntityReferencedByParent> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof ParentEntityWithReferenceToChildren)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        ParentEntityWithReferenceToChildren other = (ParentEntityWithReferenceToChildren) obj;
        
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
