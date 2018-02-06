package eionet.datadict.model;

import eionet.meta.dao.domain.DatasetRegStatus;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Table(name = "DATASET")
public class DataSet implements AttributeOwner {

    @Id
    private Integer id;
    private String identifier;

    @ManyToOne
    private Namespace namespace;
    @OneToMany(mappedBy = "dataSet")
    private Set<DatasetTable> datasetTables;
    private Set<Attribute> attributes;
    @OneToMany(mappedBy = "owner")
    private Set<AttributeValue> attributesValues;
    private String shortName;
    private Integer version;
    private String visual;
    private String detailedVisual;
    private String workingUser;
    private Boolean workingCopy;
    private DatasetRegStatus regStatus;
    private Integer date;
    private String user;
    private Namespace correspondingNS;
    private String deleted;
    private Integer dispCreateLinks;
    private Integer checkedOutCopyId;

    private Boolean allowExcelDownload;

    private Boolean allowMSAccessDownload;

    public DataSet() {
        super();
        this.allowMSAccessDownload = true;
        this.allowExcelDownload = true;
    }

    public DataSet(Integer id) {
        this.allowMSAccessDownload = true;
        this.allowExcelDownload = true;
        this.id = id;
    }

    @Override
    public AttributeOwnerType getAttributeOwnerType() {
        return new AttributeOwnerType(id, AttributeOwnerType.Type.DS);
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

    public Set<DatasetTable> getDatasetTables() {
        return datasetTables;
    }

    public void setDatasetTables(Set<DatasetTable> datasetTables) {
        this.datasetTables = datasetTables;
    }

    public Set<AttributeValue> getAttributesValues() {
        return attributesValues;
    }

    public void setAttributesValues(Set<AttributeValue> attributesValues) {
        this.attributesValues = attributesValues;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public String getDetailedVisual() {
        return detailedVisual;
    }

    public void setDetailedVisual(String detailedVisual) {
        this.detailedVisual = detailedVisual;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public Boolean getWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(Boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public DatasetRegStatus getRegStatus() {
        return regStatus;
    }

    public void setRegStatus(DatasetRegStatus regStatus) {
        this.regStatus = regStatus;
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

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public Integer getDispCreateLinks() {
        return dispCreateLinks;
    }

    public void setDispCreateLinks(Integer dispCreateLinks) {
        this.dispCreateLinks = dispCreateLinks;
    }

    public Integer getCheckedOutCopyId() {
        return checkedOutCopyId;
    }

    public void setCheckedOutCopyId(Integer checkedOutCopyId) {
        this.checkedOutCopyId = checkedOutCopyId;
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
        return this.id == null ? super.hashCode() : this.id.hashCode();
    }

    @Override
    public Set<Attribute> getAttributes() {
        return this.attributes;
    }

    @Override
    public void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    public Boolean getAllowExcelDownload() {
        return allowExcelDownload;
    }

    public void setAllowExcelDownload(Boolean allowExcelDownload) {
        this.allowExcelDownload = allowExcelDownload;
    }

    public Boolean getAllowMSAccessDownload() {
        return allowMSAccessDownload;
    }

    public void setAllowMSAccessDownload(Boolean allowMSAccessDownload) {
        this.allowMSAccessDownload = allowMSAccessDownload;
    }

}
