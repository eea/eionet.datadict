
package eionet.meta;

import java.util.*;
import eionet.util.Util;
import eionet.util.Props;
import eionet.util.PropsIF;

/*
 * 
 */
public class DataElement implements Comparable{
    
    private String id = null;
    private String shortName = null;
    private String type = null;
    private String version = null;
    private String status = null;
	private String identifier = null;
	
	private String tableID = null;
	private String datasetID = null;
	private String dstShortName = null; // used in the data elements search
	private String tblShortName = null; // used in the data elements search
	private String tblIdentifier = null; // used in setting target namespaces in schemas
	private String dstIdentifier = null; // used in setting target namespaces in schemas
    
	private String gis = null;
	private String positionInTable = null;
	private boolean isRodParam = true;

	private String workingUser = null;
	private String workingCopy = null;

	private Namespace ns = null; // parent namespace
	private String topNS = null; // top namespace
	
	private String user = null; // element creator
    
    private Vector simpleAttrs = new Vector();
    private Vector complexAttrs = new Vector();
    private Vector fixedValues = null;
	private Vector fks = new Vector();

	private int sortOrder = 1;
	private String sortString = null;

    /*
     * 
     */
	public DataElement(){
    }
        
    public DataElement(String id, String shortName, String type){
        this.id = id;
        this.shortName = shortName;
        this.type = type;
    }
    
    public String getTableID(){
        return tableID;
    }

    public void setTableID(String tableID){
        this.tableID = tableID;
    }

	public String getTblShortName(){
		return tblShortName;
	}

	public void setTblShortName(String tblShortName){
		this.tblShortName = tblShortName;
	}
    
    public String getDatasetID(){
        return datasetID;
    }

    public void setDatasetID(String datasetID){
        this.datasetID = datasetID;
    }

	public String getDstShortName(){
		return dstShortName;
	}

	public void setDstShortName(String dstShortName){
		this.dstShortName = dstShortName;
	}

	public String getTblIdentifier(){
		return tblIdentifier;
	}

	public void setTblIdentifier(String tblIdentifier){
		this.tblIdentifier = tblIdentifier;
	}

	public String getDstIdentifier(){
		return dstIdentifier;
	}

	public void setDstIdentifier(String dstIdentifier){
		this.dstIdentifier = dstIdentifier;
	}

    public String getID(){
        return id;
    }
    
    public String getShortName(){
        return shortName;
    }
    
    public String getType(){
        return type;
    }
    
    public void addAttribute(Object attr){
        simpleAttrs.add(attr);
    }
    public void setAttributes(Vector attrs){
        simpleAttrs = attrs;
    }

    public Vector getAttributes(){
        return simpleAttrs;
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
    
    public void setFixedValues(Vector fixedValues){
        this.fixedValues = fixedValues;
    }
    
    public Vector getFixedValues(){
        return fixedValues;
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
    
    public DElemAttribute getAttributeByName(String name){
        
        for (int i=0; i<simpleAttrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
            if (attr.getName().equalsIgnoreCase(name))
                return attr;
        }
        
        return null;
    }
    
    public DElemAttribute getAttributeById(String id){
        
        for (int i=0; i<simpleAttrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
            if (attr.getID().equalsIgnoreCase(id))
                return attr;
        }
        
        return null;
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
    
    public String getAttributeValueByName(String name){
        
        DElemAttribute attr = null;
        for (int i=0; i<simpleAttrs.size(); i++){
            attr = (DElemAttribute)simpleAttrs.get(i);
            if (attr.getName().equalsIgnoreCase(name))
                return attr.getValue();
        }
        
        return null;
    }
    
    public void setNamespace(Namespace ns){
        this.ns = ns;
    }
    
    public Namespace getNamespace(){
        return ns;
    }
    
    public void setTopNs(String nsid){
        this.topNS = nsid;
    }
    
    public String getTopNs(){
        return topNS;
    }
    
    public String getPositionInTable(){
        return positionInTable;
    }
    
    public void setPositionInTable(String pos){
        this.positionInTable = pos;
    }
    
    public void setComplexAttributes(Vector v){
        this.complexAttrs = v;
    }
    
    public Vector getComplexAttributes(){
        return this.complexAttrs;
    }
    
    public void setVersion(String version){
        this.version = version;
    }
    
    public String getVersion(){
        return this.version;
    }
    
    public void setWorkingUser(String workingUser){
        this.workingUser = workingUser;
    }
    
    public String getWorkingUser(){
        return this.workingUser;
    }
    
    public void setWorkingCopy(String workingCopy){
        this.workingCopy = workingCopy;
    }
    
    public boolean isWorkingCopy(){
        if (workingCopy==null)
            return false;
        else if (workingCopy.equals("Y"))
            return true;
        else
            return false;
    }
    
	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}
    
	public String getIdentifier(){
		return this.identifier;
	}
	
	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return this.status;
	}
    
	public void setGIS(String gis){
		this.gis = gis;
	}

	public String getGIS(){
		return this.gis;
	}
	
	public void setRodParam(boolean isRodParam){
		this.isRodParam = isRodParam;
	}

	public boolean isRodParameter(){
		return isRodParam;
	}
    
    public void setFKRelations(Vector fks){
    	this.fks = fks;
    }
    
	public Vector getFKRelations(){
		return this.fks;
	}
	
	public static Vector getGisTypes(){
		Vector v = new Vector();
		v.add("");
		v.add("class");
		v.add("subclass");
		v.add("subtype");
		return v;
	}
	
	public boolean hasImages(){
		boolean hasImages = false;
		for (int t=0; simpleAttrs!=null && t<simpleAttrs.size(); t++){
			DElemAttribute attr = (DElemAttribute)simpleAttrs.get(t);
			String dispType = attr.getDisplayType();
			Vector values = attr.getValues();
			if (dispType!=null &&
				dispType.equals("image") &&
				values!=null &&
				values.size()>0){
					hasImages = true;
					break;
				}
		}
		
		return hasImages;
	}
	
	public String getRelativeTargetNs(){
		
		if (ns==null || Util.voidStr(ns.getID()))
			return "/elements/" + identifier;
		else{
			if (Util.voidStr(dstIdentifier) || Util.voidStr(tblIdentifier))
				return "/namespaces/" + ns.getID();
			else
				return "/datasets/" + dstIdentifier + "/tables/" + tblIdentifier;
		}
	}

	/*
	 * 
	 */
	public void setComparation(String sortString, int sortOrder) {
		
        this.sortString = sortString;
        this.sortOrder = sortOrder; 
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    public String toString(){
        return this.sortString;
    }

	/*
	 * 
	 */
    public int compareTo(Object o) {
        return this.sortOrder*this.sortString.compareTo(o.toString());
    }
    
    /*
     * 
     */
    public String getReferenceURL(){
    	
    	if (getIdentifier()==null)
    		return null;
    		
		StringBuffer buf = new StringBuffer();
		
		String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
		if (jspUrlPrefix!=null)
			buf.append(jspUrlPrefix);
		
		buf.append("data_element.jsp?mode=view&delem_idf=");
		buf.append(getIdentifier());
		
		if (getNamespace()!=null && getNamespace().getID()!=null){
			buf.append("&pns=");
			buf.append(getNamespace().getID());
		}
		
		return buf.toString();
    }

	/**
	 * 
	 * @return
	 */
    public String getUser() {
		return user;
	}

	/**
	 * 
	 * @param user
	 */
    public void setUser(String user) {
		this.user = user;
	}
}
