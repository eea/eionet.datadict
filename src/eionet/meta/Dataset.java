
package eionet.meta;

import java.util.*;

public class Dataset {
    
    private String id = null;
    private String shortName = null;
    private String version = null;
    private String visual = null;
    private String detailedVisual = null;
    
    private Vector tables = new Vector();
    private Vector simpleAttrs = new Vector();
    private Vector complexAttrs = new Vector();
    
    private String nsID = null;
    
    private String status = null;
    private String workingCopy = null;
    
    private String name = null;

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
    
    public void setNamespaceID(String nsID){
        this.nsID = nsID;
    }
    
    public String getNamespaceID(){
        return this.nsID;
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
    
    public void setVisual(String visual){
        this.visual = visual;
    }
    
    public String getVisual(){
        return visual;
    }
    
    public void setDetailedVisual(String detailedVisual){
        this.detailedVisual = detailedVisual;
    }
    
    public String getDetailedVisual(){
        return detailedVisual;
    }
    
    public void setSimpleAttributes(Vector v){
        this.simpleAttrs = v;
    }
    
    public Vector getSimpleAttributes(){
        return simpleAttrs;
    }
    
    public void setComplexAttributes(Vector v){
        this.complexAttrs = v;
    }
    
    public Vector getComplexAttributes(){
        return complexAttrs;
    }
    
    public String getAttributeValueByShortName(String name){
        
        for (int i=0; i<simpleAttrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr.getValue();
        }
        
        return null;
    }
    
	public DElemAttribute getAttributeByShortName(String name){
        
		// look from simple attributes
		for (int i=0; i<simpleAttrs.size(); i++){
			DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
			if (attr.getShortName().equalsIgnoreCase(name))
				return attr;
		}
    
		// if it wasn't in the simple attributes, look from complex ones
		for (int i=0; i<complexAttrs.size(); i++){
			DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
			if (attr.getShortName().equalsIgnoreCase(name))
				return attr;
		}
    
		return null;
	}
	
	public Vector getVersioningAttributes(){
        if (simpleAttrs==null)
            return null;
        
        Vector set = new Vector();
        for (int i=0; i<simpleAttrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
            if (attr.effectsVersion())
                set.add(attr);
        }
        
        return set;
    }
    
    public boolean isWorkingCopy(){
        if (workingCopy==null)
            return false;
        else if (workingCopy.equals("Y"))
            return true;
        else
            return false;
    }
    
    public void setWorkingCopy(String workingCopy){
        this.workingCopy = workingCopy;
    }
    
    public void setStatus(String status){
        this.status = status;
    }
    
    public String getStatus(){
        return this.status;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
}