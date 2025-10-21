package eionet.datadict.model;

import org.apache.commons.lang3.StringUtils;

public class AttributeOwnerType {

    private Integer id;
    private Type type;

    public AttributeOwnerType(Integer id, Type type) {
        this.id = id;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Type {
        E("DataElement"),
        T("Table"),
        DS("Dataset"),
        SCH("Schema"),
        SCS("Schemaset"),
        VCF("Vocabulary"),
        DATA_ELEMENT_WITH_VALUE_LIST("Element with Value List"),
        DATA_ELEMENT_WITH_QUANTITATIVE_VALUES("Element with Quantitative Values");
        
        private final String label;

        private Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }

        public static Type getFromString(String stringLabel) {
            for (Type type : Type.values()) {
                if (StringUtils.equalsIgnoreCase(type.name(), stringLabel)) {
                    return type;
                }
                if (StringUtils.equalsIgnoreCase(type.getLabel(), stringLabel)) {
                    return type;
                }
            }
            return null;
        }
    }
}
