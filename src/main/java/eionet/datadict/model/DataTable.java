package eionet.datadict.model;

import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

public class DataTable implements SimpleAttributeOwner {
    
    @Id
    private Integer id;
    private String identifier;
    private String shortName;
    private String name;
    
    @ManyToOne
    private DataSet dataSet;
    @ManyToOne
    private Namespace namespace;
    @OneToMany(mappedBy = "dataTable")
    private Set<DataTableElement> dataTableElements;
    private Set<SimpleAttribute> simpleAttributes;
    @OneToMany(mappedBy = "owner")
    private Set<SimpleAttributeValues> simpleAttributesValues;

    public DataTable() {
        super();
    }
    
    public DataTable(Integer id) {
        this.id = id;
    }
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_TABLE;
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }
    
    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public Set<DataTableElement> getDataTableElements() {
        return dataTableElements;
    }

    public void setDataTableElements(Set<DataTableElement> dataTableElements) {
        this.dataTableElements = dataTableElements;
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
        
        if (!(obj instanceof DataTable)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        DataTable other = (DataTable) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}
