package eionet.datadict.model;

import java.util.List;

public class DataTable implements SimpleAttributeOwner {
    
    private Integer id;
    private String identifier;
    private String shortName;
    private String name;
    
    private Namespace namespace;
    private List<DataTableElement> dataTableElements;
    private DataSet dataSet;
    private List<SimpleAttribute> simpleAttributes;
    private List<SimpleAttributeValues> simpleAttributesValues;

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

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public List<DataTableElement> getDataTableElements() {
        return dataTableElements;
    }

    public void setDataTableElements(List<DataTableElement> dataTableElements) {
        this.dataTableElements = dataTableElements;
    }
    
    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
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
