package eionet.datadict.model;

public class DataTableElement {

    private DataTable dataTable;
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

}
