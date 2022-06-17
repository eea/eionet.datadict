package eionet.datadict.model;

public class ContactDetails {

    private int mAttributeId;
    private String mAttributeName;
    private int dataElemId;
    private String value;
    private String parentType;
    private String dataElementIdentifier;
    private String dataElementShortName;
    private String datasetIdentifier;
    private String datasetShortName;

    public int getmAttributeId() {
        return mAttributeId;
    }

    public void setmAttributeId(int mAttributeId) {
        this.mAttributeId = mAttributeId;
    }

    public String getmAttributeName() {
        return mAttributeName;
    }

    public void setmAttributeName(String mAttributeName) {
        this.mAttributeName = mAttributeName;
    }

    public int getDataElemId() {
        return dataElemId;
    }

    public void setDataElemId(int dataElemId) {
        this.dataElemId = dataElemId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public String getDataElementIdentifier() {
        return dataElementIdentifier;
    }

    public void setDataElementIdentifier(String dataElementIdentifier) {
        this.dataElementIdentifier = dataElementIdentifier;
    }

    public String getDataElementShortName() {
        return dataElementShortName;
    }

    public void setDataElementShortName(String dataElementShortName) {
        this.dataElementShortName = dataElementShortName;
    }

    public String getDatasetIdentifier() {
        return datasetIdentifier;
    }

    public void setDatasetIdentifier(String datasetIdentifier) {
        this.datasetIdentifier = datasetIdentifier;
    }

    public String getDatasetShortName() {
        return datasetShortName;
    }

    public void setDatasetShortName(String datasetShortName) {
        this.datasetShortName = datasetShortName;
    }
}
