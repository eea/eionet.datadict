
package eionet.meta;

import java.util.*;

public class ClsfScheme {
    
    private String id = null;
    private String name = null;
    private String type = null;
    private String version = null;
    private String description = null;
    
    private Vector items = new Vector();

    public ClsfScheme(String id, String name, String version){
        this.id = id;
        this.name = name;
        this.version = version;
    }
    
    public String getID(){
        return id;
    }

    public String getName(){
        return name;
    }
    
    public String getVersion(){
        return version;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public void addItem(Object o){
        items.add(o);
    }
    
    public void setItems(Vector items){
        this.items = items;
    }
    
    public Vector getItems(){
        return items;
    }
}