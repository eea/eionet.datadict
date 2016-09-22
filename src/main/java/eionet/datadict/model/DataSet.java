package eionet.datadict.model;

import java.util.List;

public class DataSet implements SimpleAttributeOwner {

    private Integer id;
    private String identifier;
    
    private Namespace namespace;
    private List<DataSetTable> dataSetTables;
    private List<SimpleAttribute> simpleAttributes;
    private List<SimpleAttributeValues> simpleAttributesValues;

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

    public List<DataSetTable> getDataSetTables() {
        return dataSetTables;
    }

    public void setDataSetTables(List<DataSetTable> dataSetTables) {
        this.dataSetTables = dataSetTables;
    }
    
    @Override
    public List<SimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

    @Override
    public void setSimpleAttributes(List<SimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

    @Override
    public List<SimpleAttributeValues> getSimpleAttributesValues() {
        return simpleAttributesValues;
    }

    @Override
    public void setSimpleAttributesValues(List<SimpleAttributeValues> simpleAttributeValues) {
        this.simpleAttributesValues = simpleAttributeValues;
    }
    
}
