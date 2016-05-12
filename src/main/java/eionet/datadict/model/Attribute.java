package eionet.datadict.model;

import eionet.datadict.model.enums.Enumerations.DDEntitiyType;

/**
 *
 * @author eworx-alk
 */
public class Attribute {
    
    private DDEntitiyType parentType;
    private int attributeDeclaration;
    private int dataElementId;
    private String value;

    public int getAttributeDeclaration() {
        return attributeDeclaration;
    }

    public void setAttributeDeclaration(int attributeDeclaration) {
        this.attributeDeclaration = attributeDeclaration;
    }

    public DDEntitiyType getParentType() {
        return parentType;
    }

    public void setParentType(DDEntitiyType parentType) {
        this.parentType = parentType;
    }

    public int getDataElementId() {
        return dataElementId;
    }

    public void setDataElementId(int dataElementId) {
        this.dataElementId = dataElementId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
