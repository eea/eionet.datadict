
package eionet.meta;
import com.tee.util.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.HashSet;

public class DElemAttribute{

    public static String TYPE_SIMPLE = "SIMPLE";
    public static String TYPE_COMPLEX = "COMPLEX";

    public static String FIELD_ID   = "fld-id";
    public static String FIELD_NAME = "fld-name";
    public static String FIELD_DEFN = "fld-defn";
    public static String FIELD_VALUE = "fld-value";

    public static String FIELD_PRIORITY_HIGH = "0";
    public static String FIELD_PRIORITY_LOW = "1";

    private Hashtable typeWeights = new Hashtable();

    private String type = null;
    private String name = null;
    private String shortName = null;
    private String id = null;
    private String value = null;
    private Vector values = null;
    private Vector originalValues = null;
    private Vector inheritedValues = null;
    private String inheritedValue = null; //for attributes, which can hae only 1 value
    private String originalValue = null;
    private String inheritedLevel = null; //possible values: null, T, DS
    private String fixedValueID = null;
    private String definition = null;
    private String obligation = "M";
    private Namespace ns = null;

    private String displayType   = null;
    private int displayOrder  = 999;
    private int displayWhen      = -1;
    private String displayWidth  = "20";
    private String displayHeight = "1";
    private String displayMultiple   = "0";
    private String inheritable   = "0";    
	private String harvesterID   = null;
	private String harvAttrID   = null;

    private Vector fields = null;
    private Vector rows = null;

    private Vector fixedValues = null;

    private HashSet verAttrs = null;

    public DElemAttribute(String id, String name, String shortName, String type, String value){
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.value = value;

        typeWeights.put("TBL", new Integer(64));
        typeWeights.put("FXV", new Integer(32));
        typeWeights.put("DCL", new Integer(16));
        typeWeights.put("DST", new Integer(8));
        typeWeights.put("AGG", new Integer(4));
        typeWeights.put("CH1", new Integer(2));
        typeWeights.put("CH2", new Integer(1));
    }

    public DElemAttribute(String id, String name, String shortName, String type, String value, String definition){
        this(id, name, shortName, type, value);
        this.definition = definition;
    }

    public DElemAttribute(String id, String name, String shortName, String type, String value, String definition, String obligation){
        this(id, name, shortName, type, value, definition);
        this.obligation = obligation;
    }
    public DElemAttribute(String id, String name, String shortName, String type, String value, String definition, String obligation, String multiple){
        this(id, name, shortName, type, value, definition, obligation);
        this.displayMultiple = multiple;
        if (this.displayMultiple.equals("1")){
          if (!Util.nullString(value))
              addValue(value);
        }
    }

    public String getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public String getShortName(){
        return shortName;
    }

    public String getID(){
        return id;
    }

    public String getValue(){


        if (values==null && inheritedValues!=null)
            values=inheritedValues;
        if (value==null && inheritedValue!=null)
            value=inheritedValue;

        //if (displayMultiple!=null && displayMultiple.equals("1")){
            if (values!=null) {
              if (values.size()>0){
                StringBuffer buf = new StringBuffer();
                for (int i=0; i<values.size(); i++){
                    buf.append(values.get(i));
                    if (i!=values.size()-1)
                        buf.append(", ");
                }

                return buf.toString();
              }
            }
        //}

        return value;
    }

    public void setValue(String value){
        if (this.displayMultiple.equals("1") || this.inheritable.equals("1")){
          if (!Util.nullString(value))
              addValue(value);
        }
        this.value = value;
    }
    public Vector getValues(){
        if (values==null && inheritedValues!=null)
            values=inheritedValues;
        return values;
    }

    public void addValue(String value){
        if (values==null) values = new Vector();

        if (!values.contains(value))
            values.add(value);
    }

    public String getDefinition(){
        return definition;
    }

    public String getObligation(){
        return obligation;
    }

    public void setFixedValueID(String fixedValueID){
        this.fixedValueID = fixedValueID;
    }

    public String getFixedValueID(){
        return fixedValueID;
    }

    public void setNamespace(Namespace ns){
        this.ns = ns;
    }

    public Namespace getNamespace(){
        return ns;
    }
    
    public void setDisplayProps(String displayType,
                                  int displayOrder,
                                  int displayWhen,
                                  String displayWidth,
                                  String displayHeight,
                                  String displayMultiple){

        this.displayType   = displayType;
        this.displayOrder  = displayOrder;
        this.displayWhen   = displayWhen;
        this.displayWidth  = displayWidth;
        this.displayHeight = displayHeight;
        this.displayMultiple = displayMultiple;
    }

    public void setInheritable(String value){
        this.inheritable = value;
    }
    
    public void setHarvesterID(String harvesterID){
    	this.harvesterID = harvesterID; 
    }
    
	public String getHarvesterID(){
		return this.harvesterID; 
	}

	public void setDisplayType(String displayType){
		this.displayType = displayType;
	}
	
    public String getDisplayType(){
        return displayType;
    }

    public String getDisplayMultiple(){
        return displayMultiple;
    }

    public String getInheritable(){
        return inheritable;
    }

    public int getDisplayOrder(){
        return displayOrder;
    }

    public String getDisplayWidth(){
        return displayWidth;
    }

    public String getDisplayHeight(){
        return displayHeight;
    }

    public boolean displayFor(String type){

        if (this.type.equals(TYPE_COMPLEX)){
            if (type.equals("FXV") || type.equals("TBL"))
                return false;
            else
                return true;
        }

        // if displayWhen==0, no flag can possible be set
        if (displayWhen == 0)
            return false;

        // for an unrecognized type we return false
        if (type == null)
            return false;

        Integer weight = (Integer)typeWeights.get(type);
        if (weight == null)
            return false;
        
        // we divide displayWhen with the type's weight
        // and if the result is an odd number, we return true
        // if not, we return false
        int div = displayWhen/weight.intValue();
        
        if (div % 2 != 0)
            return true;
        else
            return false;
    }
    
    public void addField(String id, String name, String value){
        
        Hashtable hash = new Hashtable();
        hash.put(FIELD_ID, id);
        hash.put(FIELD_NAME, name);
        hash.put(FIELD_VALUE, value);
        
        if (fields == null) fields = new Vector();
        fields.add(hash);
    }
    
    public void setFields(Vector v){
        this.fields = v;
    }
    
    public void addRow(Hashtable hash){
        
        if (rows == null) rows = new Vector();
        rows.add(hash);
    }
    
    public Vector getRows(){
        if (rows==null || rows.size()==0 && inheritedValues!=null)
            rows=inheritedValues;
        return rows;
    }
    
    public Vector getFields(){
        return fields;
    }

    /**
    * A function for getting value of the specified field.
    * Meant for complex attributes only and return the field                   
    * value as soon as it finds it in one of the rows.
    */

    public String getFieldValueByID(String fldID){
        
        if (fldID==null)
            return null;

        Vector _rows = getRows();
        for (int i=0; _rows!=null && i<_rows.size(); i++){
            Hashtable rowHash = (Hashtable)_rows.get(i);
			String value = (String)rowHash.get(fldID);
			if (value==null)
				continue;
			else
				return value;
        }
        
        return null;
    }
    
    /**
    * A function for getting value of the specified field.
    * Meant for complex attributes only and return the field
    * value as soon as it finds it in one of the rows.
    */

    public String getFieldValueByName(String fldName){
        return getFieldValueByID(getFieldIdByName(fldName));
    }
    
    public String getFieldIdByName(String fldName){
        
        if (fldName==null || fields==null)
            return null;
            
        for (int i=0; i<fields.size(); i++){
            Hashtable fldHash = (Hashtable)fields.get(i);
            if (fldName.equals(fldHash.get("name")))
                return (String)fldHash.get("id");
        }
        
        return null;
    }
    
    public void addFixedValue(FixedValue fv){
        
        if (fixedValues == null) fixedValues = new Vector();
        rows.add(fv);
    }
    
    public Vector getFixedValues(){
        return fixedValues;
    }
    
    /**
    * Overrides equals() in class Object.
    */
    public boolean equals(Object o) {
        
        if (!(o instanceof DElemAttribute))
	        return false;
        
        DElemAttribute oAttr = (DElemAttribute)o;
        
        // for now, we don't support complex attributes here
        String oType = oAttr.getType();
        if ((type!=null && type.equals(TYPE_COMPLEX)) ||
            (oType!=null && oType.equals(TYPE_COMPLEX)))
            return false;
        
        // compare the two attr names
        boolean diff = shortName.equals(oAttr.getShortName());
        if (!diff)
            return diff;
        
        // comapre the two attr values
        if (value== null || oAttr.getValue()==null)
            return false;
        else if (value== null && oAttr.getValue()==null)
            return true;
            
        return value.equals(oAttr.getValue());
    }
    
    /**
    *
    */
    public boolean effectsVersion(){

        if (verAttrs==null){
            verAttrs = new HashSet();
            verAttrs.add("ShortDescription");
            verAttrs.add("Name");
            verAttrs.add("Definition");
            verAttrs.add("Descriptipon of Use");
            verAttrs.add("Methodology");
            verAttrs.add("Datatype");
            verAttrs.add("MinSize");
            verAttrs.add("MaxSize");
            verAttrs.add("Decimal precision");
            verAttrs.add("Unit");
            verAttrs.add("MinValue");
            verAttrs.add("MaxValue");
            verAttrs.add("Planned Upd Frequency");
        }

        return verAttrs.contains(shortName);
    }
    public void setInheritedLevel(String value){
        this.inheritedLevel = value;
    }
    public String getInheritedLevel(){
        return inheritedLevel;
    }
    public void setInheritedValue(String value){
        if (this.displayMultiple.equals("1") || this.inheritable.equals("1")){
          if (!Util.nullString(value))
              addInheritedValue(value);
        }
        this.inheritedValue = value;
    }
    public String getInheritedValue(){
        //if (displayMultiple!=null && displayMultiple.equals("1")){
            if (inheritedValues!=null){
                if (inheritedValues.size()>0){
                  StringBuffer buf = new StringBuffer();
                  for (int i=0; i<inheritedValues.size(); i++){
                      buf.append(inheritedValues.get(i));
                      if (i!=inheritedValues.size()-1)
                          buf.append(", ");
                  }
                  return buf.toString();
                }
            }
        //}
        return inheritedValue;
    }
    public void setOriginalValue(String value){
        if (this.displayMultiple.equals("1")){
          if (!Util.nullString(value))
              addOriginalValue(value);
        }
        this.originalValue = value;
    }
    public String getOriginalValue(){
        if (displayMultiple!=null && displayMultiple.equals("1")){
            if (originalValues!=null && originalValues.size()>0){
                StringBuffer buf = new StringBuffer();
                for (int i=0; i<originalValues.size(); i++){
                    buf.append(originalValues.get(i));
                    if (i!=originalValues.size()-1)
                        buf.append(", ");
                }

                return buf.toString();
            }
        }
        return originalValue;
    }
    public Vector getOriginalValues(){
        return originalValues;
    }
    public void addOriginalValue(Object value){
        if (originalValues==null) originalValues = new Vector();

        if (!originalValues.contains(value))
          originalValues.add(value);
    }
    public Vector getInheritedValues(){
        return inheritedValues;
    }
    public void addInheritedValue(Object value){
        if (inheritedValues==null) inheritedValues = new Vector();

        if (!inheritedValues.contains(value))
          inheritedValues.add(value);
    }
    public void clearInherited(){
        //if (inheritedValues!=null)
        inheritedValues=null;
        inheritedValue=null;
    }

	public static Vector orderAttrs(Vector attrs, Vector order){
		
		if (order==null || order.size()==0 || attrs==null || attrs.size()==0) return attrs;
		
		for (int i=0; i<attrs.size(); i++){
			DElemAttribute attr = (DElemAttribute)attrs.get(i);
			String shortName = attr.getShortName();
			int pos = order.indexOf(shortName);
			if (pos == -1)
				order.add(attr);
			else{
				order.remove(pos);
				order.insertElementAt(attr, pos);
			}
		}
		
		for (int i=0; i<order.size(); i++)
			if (!order.get(i).getClass().getName().endsWith("DElemAttribute")) order.remove(i--);
		
		return order;
	}

    /**
    *
    */
    public void main(String[] args){

        Object o = null;
        boolean g = o instanceof DElemAttribute;
        //public DElemAttribute(
        //String id, String name, String shortName, String type, String value){
        DElemAttribute attr1 = new DElemAttribute(null, null, "name2", null, "kala");
        DElemAttribute attr2 = new DElemAttribute(null, null, "name2", null, "lkala");
        
        System.out.println(attr1.equals(attr2));
    }
}
