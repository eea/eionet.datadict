package eionet.datadict.model;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

public class DatasetTableElement {

    @ManyToOne
    private DatasetTable datasetTable;
    @OneToOne
    private DataElement dataElement;
    private int position;
    private boolean mandatory;
    private boolean primaryKey;
    private Character multiValueDelimiter;

    public DatasetTableElement() { }
    
    public DatasetTableElement(DatasetTable datasetTable, DataElement DataElement) {
        this.datasetTable = datasetTable;
        this.dataElement = DataElement;
    }

    public DatasetTable getDatasetTable() {
        return datasetTable;
    }

    public void setDatasetTable(DatasetTable datasetTable) {
        this.datasetTable = datasetTable;
    }

    public DataElement getDataElement() {
        return dataElement;
    }

    public void setDataElement(DataElement dataElement) {
        this.dataElement = dataElement;
    }
    
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Character getMultiValueDelimiter() {
        return multiValueDelimiter;
    }

    public void setMultiValueDelimiter(Character multiValueDelimiter) {
        this.multiValueDelimiter = multiValueDelimiter;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof DatasetTableElement)) {
            return false;
        }
        
        if (this.dataElement == null) {
            return false;
        }
        
        DatasetTableElement other = (DatasetTableElement) obj;
        
        return this.dataElement.equals(other.dataElement);
    }

    @Override
    public int hashCode() {
        return this.dataElement == null ?  super.hashCode() : this.dataElement.hashCode();
    }
    
}
