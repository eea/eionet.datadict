package eionet.datadict.model;

import java.util.Objects;

public class ContactDetails {

    private int mAttributeId;
    private String mAttributeName;
    private Integer dataElemId;
    private String value;
    private String parentType;
    private String dataElementIdentifier;
    private String dataElementShortName;
    private Integer dataElementDatasetId;
    private String dataElemRegStatus;
    private Integer dataElemTableId;
    private String dataElemTableIdentifier;
    private String dataElemType;
    private Integer dataElemParentNs;
    private Integer dataElemTopNs;
    private String datasetIdentifier;
    private String datasetShortName;
    private String datasetRegStatus;
    private String datasetWorkingCopy;
    private String dataElemWorkingCopy;

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

    public Integer getDataElemId() {
        return dataElemId;
    }

    public void setDataElemId(Integer dataElemId) {
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

    public Integer getDataElementDatasetId() {
        return dataElementDatasetId;
    }

    public void setDataElementDatasetId(Integer dataElementDatasetId) {
        this.dataElementDatasetId = dataElementDatasetId;
    }

    public String getDataElemRegStatus() {
        return dataElemRegStatus;
    }

    public void setDataElemRegStatus(String dataElemRegStatus) {
        this.dataElemRegStatus = dataElemRegStatus;
    }

    public Integer getDataElemTableId() {
        return dataElemTableId;
    }

    public void setDataElemTableId(Integer dataElemTableId) {
        this.dataElemTableId = dataElemTableId;
    }

    public String getDataElemTableIdentifier() {
        return dataElemTableIdentifier;
    }

    public void setDataElemTableIdentifier(String dataElemTableIdentifier) {
        this.dataElemTableIdentifier = dataElemTableIdentifier;
    }

    public String getDataElemType() {
        return dataElemType;
    }

    public void setDataElemType(String dataElemType) {
        this.dataElemType = dataElemType;
    }

    public Integer getDataElemParentNs() {
        return dataElemParentNs;
    }

    public void setDataElemParentNs(Integer dataElemParentNs) {
        this.dataElemParentNs = dataElemParentNs;
    }

    public Integer getDataElemTopNs() {
        return dataElemTopNs;
    }

    public void setDataElemTopNs(Integer dataElemTopNs) {
        this.dataElemTopNs = dataElemTopNs;
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

    public String getDatasetWorkingCopy() {
        return datasetWorkingCopy;
    }

    public void setDatasetWorkingCopy(String datasetWorkingCopy) {
        this.datasetWorkingCopy = datasetWorkingCopy;
    }

    public String getDataElemWorkingCopy() {
        return dataElemWorkingCopy;
    }

    public void setDataElemWorkingCopy(String dataElemWorkingCopy) {
        this.dataElemWorkingCopy = dataElemWorkingCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactDetails that = (ContactDetails) o;
        return mAttributeId == that.mAttributeId &&
                Objects.equals(parentType, that.parentType) && Objects.equals(dataElemTableId, that.dataElemTableId) &&
                (Objects.equals(dataElementIdentifier, that.dataElementIdentifier) &&
                Objects.equals(dataElementShortName, that.dataElementShortName) ||
                Objects.equals(datasetIdentifier, that.datasetIdentifier) &&
                Objects.equals(datasetShortName, that.datasetShortName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(mAttributeId, parentType, dataElementIdentifier, dataElementShortName, dataElemTableId, datasetIdentifier, datasetShortName);
    }
}