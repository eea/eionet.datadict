
package eionet.meta;

import java.util.*;

public class DataElement {
    
    private String id = null;
    private String shortName = null;
    private String type = null;
    private String version = null;
    private String status = null;
    private String defUrl = null;
    
    private Vector simpleAttrs = new Vector();
    private Vector complexAttrs = new Vector();
    
    private String parentID = null;
    private String position = null;
    private String minOccurs = null;
    private String maxOccurs = null;
    
    private String inSequenceID = null;
    private String inChoiceID = null;
    
    private Namespace ns = null;
    private String topNS = null;
    
    private String tableID = null;
    private String datasetID = null;
    
    private String dataClassID = null;
    
    private Vector subElements = null;
    private Vector fixedValues = null;
    
    private String contentDefType = "xsd";
    
    private String sequenceID = null;
    private String choiceID = null;
    
    private String extendsID = null;
    
    private String workingUser = null;
    private String workingCopy = null;
    
	private Vector fks = new Vector();
    
    public DataElement(){
    }
        
    public DataElement(String id, String shortName, String type){
        this.id = id;
        this.shortName = shortName;
        this.type = type;
    }
    
    public DataElement(String id, String shortName, String type, String defUrl){
        this(id, shortName, type);
        this.defUrl = defUrl;
    }
    
    public String getTableID(){
        return tableID;
    }

    public void setTableID(String tableID){
        this.tableID = tableID;
    }
    public String getDatasetID(){
        return datasetID;
    }

    public void setDatasetID(String datasetID){
        this.datasetID = datasetID;
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
    
    public String getDefinitionUrl(){
        return defUrl;
    }
    
    public void setDefinitionUrl(String url){
        this.defUrl = url;
    }
    
    public void setExtension(String id){
        this.extendsID = id;
    }
    
    public String getExtension(){
        return this.extendsID;
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
    
    public void setSubElements(Vector subElements){
        this.subElements = subElements;
    }
    
    public Vector getSubElements(){
        return subElements;
    }
    
    public void setFixedValues(Vector fixedValues){
        this.fixedValues = fixedValues;
    }
    
    public Vector getFixedValues(){
        return fixedValues;
    }
    
    public void setContentDefType(String type){
        if (type != null && type.length()!=0)
            contentDefType = type;
    }
    
    public String getContentDefType(){
        return contentDefType;
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
    
    public void setRelation(String parentID, String position, String minOccurs, String maxOccurs){
        this.parentID = parentID;
        this.position = position;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }
    
    public void setInSequence(String sequenceID, String position, String minOccurs, String maxOccurs){
        this.inSequenceID = sequenceID;
        this.position = position;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
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
    
    public String getParentID(){
        return parentID;
    }
    
    public String getPosition(){
        return position;
    }
    
    public String getMinOccurs(){
        return minOccurs;
    }
    
    public String getMaxOccurs(){
        return maxOccurs;
    }
    
    public void setSequence(String sequenceID){
        this.sequenceID = sequenceID;
    }

    public String getSequence(){
        return sequenceID;
    }

    public void setChoice(String choiceID){
        this.choiceID = choiceID;
    }

    public String getChoice(){
        return choiceID;
    }

    public void setDataClass(String dataClassID){
        this.dataClassID = dataClassID;
    }

    public String getDataClass(){
        return dataClassID;
    }

    public void setPosition(String pos){
        this.position = pos;
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
    
    public void setStatus(String status){
        this.status = status;
    }
    
    public String getStatus(){
        return this.status;
    }
    
    public void setFKRelations(Vector fks){
    	this.fks = fks;
    }
    
	public Vector getFKRelations(){
		return this.fks;
	}
    
    public String toString(){

        StringBuffer buf = new StringBuffer();

        buf.append("id=");
        buf.append(id);
        buf.append("\n");

        buf.append("type=");
        buf.append(type);
        buf.append("\n");

        buf.append("shortName=");
        buf.append(shortName);
        buf.append("\n");

        buf.append("defUrl=");
        buf.append(defUrl);
        buf.append("\n");

        if (ns != null){
            buf.append("xmlns:");
            buf.append(ns.getShortName());
            buf.append("=\"");
            buf.append(ns.getUrl());
            buf.append("\"\n");
        }
        
        buf.append("\nAttributes:\n");
        for (int i=0; simpleAttrs!=null && i<simpleAttrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
            buf.append(attr.getShortName());
            buf.append("=");
            buf.append(attr.getValue());
            buf.append("\n");
        }
        
        buf.append("\nFixedValues:\n");
        for (int i=0; fixedValues!=null && i<fixedValues.size(); i++){
            String value = (String)fixedValues.get(i);
            buf.append(value);
            buf.append("\n");
        }
        
        buf.append("\nSubElements:\n");
        for (int i=0; subElements!=null && i<subElements.size(); i++){
            DataElement subElem = (DataElement)subElements.get(i);
            Namespace namespace = subElem.getNamespace();
            String nsName = namespace==null ? "null" : namespace.getShortName();
            buf.append(nsName);
            buf.append(":");
            buf.append(subElem.getShortName());
            buf.append(", minOcc=");
            buf.append(subElem.getMinOccurs());
            buf.append(", maxOcc=");
            buf.append(subElem.getMaxOccurs());
            buf.append(", defUrl=");
            buf.append(subElem.getDefinitionUrl());
            buf.append("\n");
        }
        
        return buf.toString();
    }
}