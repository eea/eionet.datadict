
package eionet.meta;

import java.util.*;

public class DsTable implements Comparable {
    
    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_LOOKUP = "lookup";
    
    private String id = null;
    private String dsID = null;
    private String shortName = null;
    private String name = null;
    private String definition = null;
    private String type = TYPE_NORMAL;
    
    private String nsID = null;
    
    private String workingCopy = null;
    
    private Vector elements = new Vector();
    private Vector simpleAttrs = new Vector();
    private Vector complexAttrs = new Vector();
    
    private String version = null;
    private String status = null;
    
    private String datasetName = null;
    
    private String parentNS = null;
	private String identifier = null;
    
    private String workingUser = null;
    
    private String compStr = null;
    private boolean gis = false;
    
    /**
     * 
     * @param id
     * @param dsID
     * @param shortName
     */
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
    
    public Vector simpleAttributesTable(){
        Vector v = new Vector();
        return v;
    }
    
    public Vector complexAttributesTable(){
        Vector v = new Vector();
        return v;
    }
    
    public Vector elementsTable(){
        Vector v = new Vector();
        return v;
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
    
    public void setVersion(String version){
        this.version = version;
    }
    
    public String getVersion(){
        return this.version;
    }
    
    public void setDatasetName(String dsName){
        this.datasetName = dsName;
    }
    
    public String getDatasetName(){
        return this.datasetName;
    }
    
    public void setParentNs(String nsid){
        this.parentNS = nsid;
    }
    
    public String getParentNs(){
        return parentNS;
    }

	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}
    
	public String getIdentifier(){
		return this.identifier;
	}
    
	public void setWorkingUser(String workingUser){
		this.workingUser = workingUser;
	}
    
	public String getWorkingUser(){
		return this.workingUser;
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
    
    public String getAttributeValueByShortName(String name){
        
        DElemAttribute attr = null;
        for (int i=0; i<simpleAttrs.size(); i++){
            attr = (DElemAttribute)simpleAttrs.get(i);
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
    
    public void setCompStr(String compStr){
    	this.compStr = compStr;
    }
    
	public String getCompStr(){
		return compStr;
	}

	public void setGIS(boolean gis){
		this.gis = gis;
	}
    
	public boolean hasGIS(){
		return gis;
	}
    
	public int compareTo(Object o){
		
		if (!o.getClass().getName().endsWith("DsTable")) return 1;
		
		DsTable oTbl = (DsTable)o;
		String oCompStr = oTbl.getCompStr();
		if (oCompStr==null && compStr==null)
			return 0;
		else if (oCompStr==null)
			return 1;
		else if (compStr==null)
			return -1;
		
		return compStr.compareToIgnoreCase(oCompStr);
	}
}