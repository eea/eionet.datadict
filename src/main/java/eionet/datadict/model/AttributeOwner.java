package eionet.datadict.model;

import java.util.Set;

public interface AttributeOwner {

    AttributeOwnerCategory getAttributeOwnerCategory();
    
    Comparable getId();
    
    Set<Attribute> getAttributes();
    
    void setAttributes(Set<Attribute> attributes);
    
    Set<SimpleAttributeValues> getSimpleAttributesValues();
    
    void setSimpleAttributesValues(Set<SimpleAttributeValues> simpleAttributesValues);
    
}
