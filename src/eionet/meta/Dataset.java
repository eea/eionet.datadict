
package eionet.meta;

import java.util.*;

public class Dataset {
    
    private String id = null;
    private String shortName = null;
    private String version = null;
    
    private Vector tables = new Vector();

    public Dataset(String id, String shortName, String version){
        this.id = id;
        this.shortName = shortName;
        this.version = version;
    }
    
    public String getID(){
        return id;
    }

    public String getShortName(){
        return shortName;
    }
    
    public String getVersion(){
        return version;
    }
    
    public void addElement(DsTable table){
        tables.add(table);
    }
    
    public void setTables(Vector tables){
        this.tables = tables;
    }
    
    public Vector getTables(){
        return tables;
    }
}