
package eionet.meta;

import eionet.util.Util;
import java.util.*;

public class DDSearchParameter {
    
    private String attrID = null;
    private Vector attrValues = null;
    private String valueOper = "=";
    private String idOper = "=";
    
    public DDSearchParameter(String attrID){
        this.attrID = attrID;
    }
    
    public DDSearchParameter(String attrID, Vector attrValues){
        this(attrID);
        this.attrValues = attrValues;
        legalizeValues();
    }
    
    public DDSearchParameter(String attrID, Vector attrValues, String valueOper, String idOper){
        this(attrID, attrValues);
        this.valueOper = valueOper;
        this.idOper = idOper;
    }
    
    public String getAttrID(){
        return attrID;
    }
    
    public Vector getAttrValues(){
        return attrValues;
    }
    
    public String getValueOper(){
        return valueOper;
    }
    
    public String getIdOper(){
        return idOper;
    }
    
    public void addValue(String value){
        if (attrValues == null) attrValues = new Vector();
        attrValues.add(legalize(value));
    }
    
    public void apostrophizeValues(){
        for (int i=0; attrValues!=null && i<attrValues.size(); i++){
            String value = (String)attrValues.get(i);
            attrValues.remove(i);
            attrValues.add(i, apostrophize(value));
        }
    }
    
    private void legalizeValues(){
        for (int i=0; attrValues!=null && i<attrValues.size(); i++){
            String value = (String)attrValues.get(i);
            attrValues.remove(i);
            attrValues.add(i, legalize(value));
        }
    }
    
    private String apostrophize(String in){
        return "'" + in + "'";
    }
    
    private String legalize(String in){
        
        in = (in != null ? in : "");
        StringBuffer ret = new StringBuffer(); 
      
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'' && i!=0 && i!=in.length()-1){
                ret.append("''");                
            }
            else
                ret.append(c);
        }
      
        return ret.toString();
    }
    
    public static void main(String[] args){
        Vector v = new Vector();
        v.add("kal'a");
        v.add("maja");
        DDSearchParameter par = new DDSearchParameter("9", v);
        par.apostrophizeValues();
        
        Vector vv = par.getAttrValues();
        for (int i=0; i<vv.size(); i++){
            System.out.println(vv.get(i));
        }
    }
}