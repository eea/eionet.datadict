package eionet.datadict.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "DS_TABLE")
public class DatasetTable implements AttributeOwner {

    @Id
    private Integer id;
    private String shortName;
    private String name;
    private Boolean workingCopy;
    private String workingUser;
    private Integer version;
    private Integer date;
    private String user;
    private Namespace correspondingNS;
    private Namespace parentNamespace;
    private String identifier;
    private Integer position;
    @ManyToOne
    private DataSet dataSet;
    @ManyToOne
    private Namespace namespace;

    @JsonIgnore
    @OneToMany(mappedBy = "datasetTable")
    private Set<DataElement> dataElements;
    @JsonIgnore
    private Set<Attribute> attributes;
    @OneToMany(mappedBy = "owner")
    private Set<AttributeValue> attributesValues;

    public DatasetTable() {
        super();
    }

    public DatasetTable(Integer id) {
        this.id = id;
    }

    @Override
    public AttributeOwnerType getAttributeOwnerType() {
        return new AttributeOwnerType(id, AttributeOwnerType.Type.T);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(Boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Namespace getCorrespondingNS() {
        return correspondingNS;
    }

    public void setCorrespondingNS(Namespace correspondingNS) {
        this.correspondingNS = correspondingNS;
    }

    public Namespace getParentNamespace() {
        return parentNamespace;
    }

    public void setParentNamespace(Namespace parentNamespace) {
        this.parentNamespace = parentNamespace;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Set<DataElement> getDataElements() {
        return dataElements;
    }

    public void setDataElements(Set<DataElement> dataElements) {
        this.dataElements = dataElements;
    }

    public Set<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    public Set<AttributeValue> getAttributesValues() {
        return attributesValues;
    }

    public void setAttributesValues(Set<AttributeValue> attributesValues) {
        this.attributesValues = attributesValues;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DatasetTable)) {
            return false;
        }

        if (this.id == null) {
            return false;
        }

        DatasetTable other = (DatasetTable) obj;

        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ? super.hashCode() : this.id.hashCode();
    }

}
