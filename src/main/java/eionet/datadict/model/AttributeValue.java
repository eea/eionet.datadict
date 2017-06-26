package eionet.datadict.model;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AttributeValue {

    private Integer attributeId;
    private String value;
    private DataDictEntity parentEntity;

    @OneToOne
    private AttributeOwner owner;
    @ManyToOne
    private Attribute attribute;

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataDictEntity getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(DataDictEntity parentEntity) {
        this.parentEntity = parentEntity;
    }

    public AttributeOwner getOwner() {
        return owner;
    }

    public void setOwner(AttributeOwner owner) {
        this.owner = owner;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AttributeValue)) {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
                append(attributeId).
                append(value).
                append(parentEntity.getId()).
                append(parentEntity.getType().name()).
                toHashCode();
    }

}
