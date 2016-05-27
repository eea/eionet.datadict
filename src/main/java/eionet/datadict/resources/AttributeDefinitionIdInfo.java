/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.resources;

/**
 *
 * @author exorx-alk
 */
public class AttributeDefinitionIdInfo implements ResourceIdInfo{
    
    private final String identifier;
    
    public AttributeDefinitionIdInfo(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
