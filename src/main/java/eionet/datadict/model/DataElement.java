package eionet.datadict.model;

import java.util.List;

public abstract class DataElement implements SimpleAttributeOwner {
 
    public static enum ValueType {
        FIXED,
        QUANTITATIVE,
        VOCABULARY
    }
    
    public static enum Status {
        INCOMPLETE,
        CANDIDATE,
        RECORDED,
        QUALIFIED,
        RELEASED
    }
    
    private Integer id;
    private String identifier;
    private String shortName;
    private Status status;
    private String workingUser;
    private boolean workingCopy;
    
    private Namespace namespace;
    private DataTable owner;
    private List<SimpleAttribute> simpleAttributes;
    private List<SimpleAttributeValues> simpleAttributesValues;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public DataTable getOwner() {
        return owner;
    }

    public void setOwner(DataTable owner) {
        this.owner = owner;
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
    
    public abstract ValueType getValueType();
    
    public abstract boolean supportsValueList();
    
    public abstract ValueList getValueList();

}
