
package eionet.meta;

import java.util.Vector;

import eionet.util.Util;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class FixedValue {

    private String id = null;
    private String elem_id = null;
    private String value = null;
    private String position = "0";
    private Vector attributes = new Vector();
    private Vector items = new Vector();

    private boolean isDefault = false;
    private String parent_type = "elem";

    private String csID = null;
    private String type = null;
    private int level = 0;

    private String definition = null;
    private String shortDesc = null;

    public FixedValue() {
    }

    public FixedValue(String id) {
        this.id = id;
    }

    public FixedValue(String id, String elem_id, String value) {
        this.id = id;
        this.elem_id = elem_id;
        this.value = value;
    }
    public FixedValue(String id, String elem_id, String value, String position) {
        this (id, elem_id, value);
        this.position = position;
    }

    /**
     * Overrides equals() in class Object.
     */
    public boolean equals(Object o) {

        if (!(o instanceof FixedValue))
            return false;

        // comapre the two attr values
        return value.equals(((FixedValue)o).getValue());
    }

    public String getID() {
        return id;
    }

    public String getValue() {
        return value;
    }
    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public void setCsID(String csID) {
        this.csID = csID;
    }

    public String getCsID() {
        return this.csID;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void addAttribute(Object attr) {
        attributes.add(attr);
    }

    public Vector getAttributes() {
        return attributes;
    }

    public DElemAttribute getAttributeByShortName(String name) {

        for (int i=0; i<attributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr;
        }

        return null;
    }

    public DElemAttribute getAttributeByName(String name) {

        for (int i=0; i<attributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (attr.getName().equalsIgnoreCase(name))
                return attr;
        }

        return null;
    }

    public DElemAttribute getAttributeById(String id) {

        for (int i=0; i<attributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (attr.getID().equalsIgnoreCase(id))
                return attr;
        }

        return null;
    }

    public String getAttributeValueByShortName(String name) {

        DElemAttribute attr = null;
        for (int i=0; i<attributes.size(); i++) {
            attr = (DElemAttribute)attributes.get(i);
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr.getValue();
        }

        return null;
    }

    public String getAttributeValueByID(String id) {

        DElemAttribute attr = null;
        for (int i=0; i<attributes.size(); i++) {
            attr = (DElemAttribute)attributes.get(i);
            if (attr.getID().equalsIgnoreCase(id))
                return attr.getValue();
        }

        return null;
    }

    public String getAttributeValueByName(String name) {

        DElemAttribute attr = null;
        for (int i=0; i<attributes.size(); i++) {
            attr = (DElemAttribute)attributes.get(i);
            if (attr.getName().equalsIgnoreCase(name))
                return attr.getValue();
        }

        return null;
    }

    public void setDefault() {
        this.isDefault = true;
    }

    public boolean getDefault() {
        return this.isDefault;
    }

    public void setParentType(String type) {
        if (!Util.voidStr(type))
            parent_type = type;
    }

    public String getParentType() {
        return parent_type;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    public int getLevel() {
        return level;
    }

    public void addItem(Object item) {
        items.add(item);
    }

    public Vector getItems() {
        return items;
    }

    public void setDefinition(String definition) {
        this.definition= definition;
    }

    public String getDefinition() {
        return definition;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return value==null || value.length()==0;
    }

    public String toString() {

        StringBuffer buf = new StringBuffer();

        buf.append("id=");
        buf.append(id);
        buf.append("\n");

        buf.append("elem_id=");
        buf.append(elem_id);
        buf.append("\n");

        buf.append("value=");
        buf.append(value);
        buf.append("\n");


        buf.append("\nAttributes:\n");
        for (int i=0; attributes!=null && i<attributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            buf.append(attr.getShortName());
            buf.append("=");
            buf.append(attr.getValue());
            buf.append("\n");
        }

        return buf.toString();
    }
}
