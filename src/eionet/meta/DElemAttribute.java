
package eionet.meta;

import java.util.Vector;
import java.util.Hashtable;

public class DElemAttribute{
    
    public static String TYPE_SIMPLE = "SIMPLE";
    public static String TYPE_COMPLEX = "COMPLEX";
    
    public static String FIELD_ID   = "fld-id";
    public static String FIELD_NAME = "fld-name";
    public static String FIELD_DEFN = "fld-defn";
    public static String FIELD_VALUE = "fld-value";
    
    private Hashtable typeWeights = new Hashtable();
    
    private String type = null;
    private String name = null;
    private String shortName = null;
    private String id = null;
    private String value = null;
    private String fixedValueID = null;
    private String definition = null;
    private String obligation = "M";
    private Namespace ns = null;
    
    private String displayType   = null;
    private int displayOrder  = 999;
    private int displayWhen      = -1;
    private String displayWidth  = "20";
    private String displayHeight = "1";
    
    private Vector fields = null;
    private Vector rows = null;
    
    private Vector fixedValues = null;
    
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
        return value;
    }
    
    public void setValue(String value){
        this.value = value;
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
                                  String displayHeight){
                                    
        this.displayType   = displayType;
        this.displayOrder  = displayOrder;
        this.displayWhen   = displayWhen;
        this.displayWidth  = displayWidth;
        this.displayHeight = displayHeight;
    }
    
    public String getDisplayType(){
        return displayType;
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
    
    public void addRow(Hashtable hash){
        
        if (rows == null) rows = new Vector();
        rows.add(hash);
    }
    
    public Vector getRows(){
        return rows;
    }
    
    public Vector getFields(){
        return fields;
    }
    
    public void addFixedValue(FixedValue fv){
        
        if (fixedValues == null) fixedValues = new Vector();
        rows.add(fv);
    }
    
    public Vector getFixedValues(){
        return fixedValues;
    }
    
    public void main(String[] args){
        DElemAttribute attr = new DElemAttribute(null, null, null, null, null);
        attr.setDisplayProps(null, 1, 2, null, null);
        System.out.println(attr.displayFor("AGG"));
    }
}