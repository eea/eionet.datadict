
package eionet.meta;

import java.util.*;

public class DataClass {
    
    private String id = null;
    private String shortName = null;
    private Vector attributes = new Vector();

    private Namespace ns = null;

    public DataClass(){
    }

    public DataClass(String id, String shortName){
        this.id = id;
        this.shortName = shortName;
    }

    public String getID(){
        return id;
    }

    public String getShortName(){
        return shortName;
    }

    public void addAttribute(Object attr){
        attributes.add(attr);
    }

    public Vector getAttributes(){
        return attributes;
    }

    public DElemAttribute getAttributeByShortName(String name){

        for (int i=0; i<attributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr;
        }

        return null;
    }

    public DElemAttribute getAttributeByName(String name){

        for (int i=0; i<attributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (attr.getName().equalsIgnoreCase(name))
                return attr;
        }

        return null;
    }

    public DElemAttribute getAttributeById(String id){

        for (int i=0; i<attributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (attr.getID().equalsIgnoreCase(id))
                return attr;
        }

        return null;
    }

    public String getAttributeValueByShortName(String name){

        DElemAttribute attr = null;
        for (int i=0; i<attributes.size(); i++){
            attr = (DElemAttribute)attributes.get(i);
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr.getValue();
        }

        return null;
    }

    public String getAttributeValueByName(String name){

        DElemAttribute attr = null;
        for (int i=0; i<attributes.size(); i++){
            attr = (DElemAttribute)attributes.get(i);
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

    public String toString(){

        StringBuffer buf = new StringBuffer();

        buf.append("id=");
        buf.append(id);
        buf.append("\n");

        buf.append("shortName=");
        buf.append(shortName);
        buf.append("\n");

        if (ns != null){
            buf.append("xmlns:");
            buf.append(ns.getShortName());
            buf.append("=\"");
            buf.append(ns.getUrl());
            buf.append("\"\n");
        }

        buf.append("\nAttributes:\n");
        for (int i=0; attributes!=null && i<attributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            buf.append(attr.getShortName());
            buf.append("=");
            buf.append(attr.getValue());
            buf.append("\n");
        }

        return buf.toString();
    }
}