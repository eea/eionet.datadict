
package eionet.meta;

import java.util.*;

public class CsiItem {
    
    private String id = null;
    private String csID = null;
    private String type = null;
    private String value = null;
    private String componentID = null;
    private String componentType = null;
    private String rel_description = null;

    public CsiItem(String id, String value, String componentID, String componentType){
        this.id = id;
        this.value = value;
        this.componentID = componentID;
        this.componentType = componentType;
    }

    public String getID(){
        return id;
    }

    public String getValue(){
        return value;
    }

    public String getComponentID(){
        return componentID;
    }

    public String getComponentType(){
        return componentID;
    }

    public void setCsID(String csID){
        this.csID = csID;
    }

    public String getCsID(){
        return this.csID;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }
    public void setRelDescription(String rel_description){
        this.rel_description = rel_description;
    }
    public String getRelDescription(){
        return this.rel_description;
    }
}