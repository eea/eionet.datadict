package eionet.datadict.model;

public class DataElementAttribute extends AttributeFields {

    private Integer tableId;
    private String tableIdentifier;
    private String type;
    private Integer parentNs;
    private Integer topNs;

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getTableIdentifier() {
        return tableIdentifier;
    }

    public void setTableIdentifier(String tableIdentifier) {
        this.tableIdentifier = tableIdentifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getParentNs() {
        return parentNs;
    }

    public void setParentNs(Integer parentNs) {
        this.parentNs = parentNs;
    }

    public Integer getTopNs() {
        return topNs;
    }

    public void setTopNs(Integer topNs) {
        this.topNs = topNs;
    }
}
