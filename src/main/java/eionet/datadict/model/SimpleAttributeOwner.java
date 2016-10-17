package eionet.datadict.model;

import java.util.Set;

public interface SimpleAttributeOwner {

    SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory();
    
    Comparable getId();
    
    Set<SimpleAttribute> getSimpleAttributes();
    
    void setSimpleAttributes(Set<SimpleAttribute> simpleAttributes);
    
    Set<SimpleAttributeValues> getSimpleAttributesValues();
    
    void setSimpleAttributesValues(Set<SimpleAttributeValues> simpleAttributesValues);
    
}
