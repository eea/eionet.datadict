package eionet.datadict.model;

public class AttributeFields {

    private int attributeId;
    private Integer dataElemId;
    private String value;
    private String identifier;
    private String shortName;

    public int getAttributeId() {
        return attributeId;
    }

    public AttributeFields setAttributeId(int attributeId) {
        this.attributeId = attributeId;
        return this;
    }

    public Integer getDataElemId() {
        return dataElemId;
    }

    public AttributeFields setDataElemId(Integer dataElemId) {
        this.dataElemId = dataElemId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AttributeFields setValue(String value) {
        this.value = value;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public AttributeFields setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public AttributeFields setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }
}
