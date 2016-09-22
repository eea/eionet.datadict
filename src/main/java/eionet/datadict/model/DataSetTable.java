package eionet.datadict.model;

public class DataSetTable {

    private DataSet dataSet;
    private DataTable dataTable;
    private int position;

    public DataSetTable() { }
    
    public DataSetTable(DataSet dataSet, DataTable dataTable) {
        this.dataSet = dataSet;
        this.dataTable = dataTable;
    }
    
    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
}
