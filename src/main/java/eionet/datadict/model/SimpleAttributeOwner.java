package eionet.datadict.model;

import java.util.List;

public interface SimpleAttributeOwner {

    SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory();
    
    Comparable getId();
    
    List<SimpleAttribute> getSimpleAttributes();
    
    void setSimpleAttributes(List<SimpleAttribute> simpleAttributes);
    
    List<SimpleAttributeValues> getSimpleAttributesValues();
    
    void setSimpleAttributesValues(List<SimpleAttributeValues> simpleAttributesValues);
    
}
