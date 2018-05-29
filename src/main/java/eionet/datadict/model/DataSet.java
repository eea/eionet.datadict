package eionet.datadict.model;

import eionet.meta.dao.domain.DatasetRegStatus;

import java.util.*;
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
    private Integer dispCreateLinks =-1;
    private Integer checkedOutCopyId;

    private String serializedDisplayDownloadLinks;

    private Map<DISPLAY_DOWNLOAD_LINKS,Boolean> deserializedDisplayDownloadLinks = this.initializeDiSplayDownloadLinksMap();

    public  enum DISPLAY_DOWNLOAD_LINKS{
        PDF(""),
        XML_SCHEMA(""),
        XML_SCHEMA_OLD_STRUCTURE(""),
        XML_INSTANCE(""),
        MS_EXCEL(""),
        XLS_VALIDATION_METADATA(""),
        MS_EXCEL_OLD_STRUCTURE(""),
        MS_EXCEL_DROPDOWN_BOXES(""),
        ADVANCED_ACCESS(""),
        CODELISTS_CSV(""),
        CODELISTS_XML("");

        private String value;
        DISPLAY_DOWNLOAD_LINKS(String value){
            this.value=value;
        }
        public String getValue(){
            return value;
        }
        public void setValue(String value){
            this.value=value;
        }
    }


    public DataSet() {
        super();
    }

    public DataSet(Integer id) {
        this.id = id;
    }
    public static final Map<String, Integer> createLinkWeights;

    static {
        createLinkWeights = new HashMap<String, Integer>();
        createLinkWeights.put("PDF", new Integer(1));
        createLinkWeights.put("XLS", new Integer(2));
        createLinkWeights.put("XMLINST", new Integer(4));
        createLinkWeights.put("XMLSCHEMA", new Integer(8));
        createLinkWeights.put("MDB", new Integer(16));
        createLinkWeights.put("ODS", new Integer(32));
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




    private Map<DISPLAY_DOWNLOAD_LINKS, Boolean> initializeDiSplayDownloadLinksMap() {
        Map<DISPLAY_DOWNLOAD_LINKS, Boolean> map = new LinkedHashMap<>();
        for (DISPLAY_DOWNLOAD_LINKS value : DISPLAY_DOWNLOAD_LINKS.values()) {
            map.put(value, true);
        }
        return map;

    }

    public String getSerializedDisplayDownloadLinks() {
        return serializedDisplayDownloadLinks;
    }

    public void setSerializedDisplayDownloadLinks(String serializedDisplayDownloadLinks) {
        this.serializedDisplayDownloadLinks = serializedDisplayDownloadLinks;
    }

    public Map<DISPLAY_DOWNLOAD_LINKS, Boolean> getDeserializedDisplayDownloadLinks() {
        return deserializedDisplayDownloadLinks;
    }

    public void setDeserializedDisplayDownloadLinks(Map<DISPLAY_DOWNLOAD_LINKS, Boolean> deserializedDisplayDownloadLinks) {
        this.deserializedDisplayDownloadLinks = deserializedDisplayDownloadLinks;
    }
}
