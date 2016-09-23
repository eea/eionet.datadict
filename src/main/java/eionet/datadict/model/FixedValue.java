package eionet.datadict.model;

public class FixedValue implements ValueListItem {

    private FixedValueList list;
    
    private String code;
    private String label;
    private String definition;

    public FixedValue() { }
    
    public FixedValue(FixedValueList list, String code, String label, String definition) {
        this.list = list;
        this.code = code;
        this.label = label;
        this.definition = definition;
    }

    public FixedValueList getList() {
        return list;
    }

    public void setList(FixedValueList list) {
        this.list = list;
    }
    
    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
    
    @Override
    public String getDefinition() {
        return this.definition;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
}
