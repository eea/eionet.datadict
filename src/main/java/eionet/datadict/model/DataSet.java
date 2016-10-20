package eionet.datadict.model;

import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

public class DataSet implements SimpleAttributeOwner {

    @Id
    private Integer id;
    private String identifier;
    
    @ManyToOne
    private Namespace namespace;
    @OneToMany(mappedBy = "dataSet")
    private Set<DataTable> dataTables;
    private Set<SimpleAttribute> simpleAttributes;
    @OneToMany(mappedBy = "owner")
    private Set<SimpleAttributeValues> simpleAttributesValues;

    public DataSet() {
        super();
    }
    
    public DataSet(Integer id) {
        this.id = id;
    }
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_SET;
    }
    
    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public Set<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(Set<DataTable> dataTables) {
        this.dataTables = dataTables;
    }
    
    @Override
    public Set<SimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

    @Override
    public void setSimpleAttributes(Set<SimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

    @Override
    public Set<SimpleAttributeValues> getSimpleAttributesValues() {
        return simpleAttributesValues;
    }

    @Override
    public void setSimpleAttributesValues(Set<SimpleAttributeValues> simpleAttributeValues) {
        this.simpleAttributesValues = simpleAttributeValues;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof DataSet)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        DataSet other = (DataSet) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}
