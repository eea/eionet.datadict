package eionet.datadict.model;

import java.util.Set;

public interface AttributeOwner {

    AttributeOwnerType getAttributeOwnerType();
    
    Comparable getId();
    
    Set<Attribute> getAttributes();
    
    void setAttributes(Set<Attribute> attributes);
    
    Set<AttributeValue> getAttributesValues();
    
    void setAttributesValues(Set<AttributeValue> attributesValues);
    
}
