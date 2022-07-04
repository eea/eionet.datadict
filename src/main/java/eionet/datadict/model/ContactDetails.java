package eionet.datadict.model;

import java.util.Objects;

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
    private String datasetRegStatus;

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

    public String getDatasetRegStatus() {
        return datasetRegStatus;
    }

    public void setDatasetRegStatus(String datasetRegStatus) {
        this.datasetRegStatus = datasetRegStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactDetails that = (ContactDetails) o;
        return mAttributeId == that.mAttributeId &&
                (Objects.equals(dataElementIdentifier, that.dataElementIdentifier) &&
                Objects.equals(dataElementShortName, that.dataElementShortName) ||
                Objects.equals(datasetIdentifier, that.datasetIdentifier) &&
                Objects.equals(datasetShortName, that.datasetShortName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(mAttributeId, dataElementIdentifier, dataElementShortName, datasetIdentifier, datasetShortName);
    }
}
