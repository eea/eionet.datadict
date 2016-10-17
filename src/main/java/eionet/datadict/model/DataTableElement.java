package eionet.datadict.model;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

public class DataTableElement {

    @ManyToOne
    private DataTable dataTable;
    @OneToOne
    private DataElement dataElement;
    private int position;
    private boolean mandatory;
    private boolean primaryKey;
    private Character multiValueDelimiter;

    public DataTableElement() { }
    
    public DataTableElement(DataTable dataTable, DataElement DataElement) {
        this.dataTable = dataTable;
        this.dataElement = DataElement;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
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
        
        if (!(obj instanceof DataTableElement)) {
            return false;
        }
        
        if (this.dataElement == null) {
            return false;
        }
        
        DataTableElement other = (DataTableElement) obj;
        
        return this.dataElement.equals(other.dataElement);
    }

    @Override
    public int hashCode() {
        return this.dataElement == null ?  super.hashCode() : this.dataElement.hashCode();
    }
    
}
