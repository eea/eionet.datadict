
package eionet.meta;

import java.util.*;

public class DsTable {
    
    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_LOOKUP = "lookup";
    
    private String id = null;
    private String dsID = null;
    private String shortName = null;
    private String name = null;
    private String definition = null;
    private String type = TYPE_NORMAL;
    
    private String nsID = null;
    
    private Vector elements = new Vector();

    public DsTable(String id, String dsID, String shortName){
        this.id = id;
        this.shortName = shortName;
        this.dsID = dsID;
    }
    
    public String getID(){
        return id;
    }
    
    public String getDatasetID(){
        return dsID;
    }

    public String getShortName(){
        return shortName;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    public void setDefinition(String definition){
        this.definition = definition;
    }
    
    public String getDefinition(){
        return definition;
    }
    
    public void setType(String type){
        this.type = type;
    }
    
    public String getType(){
        return type;
    }
    
    public void addElement(DataElement element){
        elements.add(element);
    }
    
    public void setElements(Vector elements){
        this.elements = elements;
    }
    
    public Vector getElements(){
        return elements;
    }
    
    public void setNamespace(String nsID){
        this.nsID = nsID;
    }
    
    public String getNamespace(){
        return nsID;
    }
}