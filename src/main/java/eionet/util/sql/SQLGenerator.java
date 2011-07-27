package eionet.util.sql;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class SQLGenerator implements Cloneable {

    private LinkedHashMap fields;
    private String tableName;
    private String pkField;
    private int    state;

    /**
     * Sets table name.
     */
    public void setTable(String tableName) {
        this.tableName = tableName;
    }
    /**
     * Gets table name.
     */
    public String getTableName() {
        return tableName;
    }
    /**
     * Set primary key field.
     */
    public void setPKField(String pkField) {
        this.pkField = pkField;
    }
    /**
     * Gets primary key field.
     */
    public String getPKField() {
        return pkField;
    }
    /**
     * Sets field value (it will be put between quotes).
     */
    public void setField(String fldName, String fldValue) {
        int len = fldValue.length();

        if (len > 1 && fldValue.charAt(0) == '\'' && fldValue.charAt(len-1) == '\'')
            // fldValue is already escaped
            fields.put(fldName, fldValue);
        else
            fields.put(fldName, SQL.toLiteral(fldValue));
    }
    /**
     * Sets unquoted field expression.
     */
    public void setFieldExpr(String fldName, String fldExpr) {
        fields.put(fldName, fldExpr);
    }
    /**
     * Removes field from the collection.
     */
    public void removeField(String fldName) {
        fields.remove(fldName);
    }
    /**
     * Gets field value/expression for given field name.
     */
    public String getFieldValue(String fldName) {
        String value = (String)fields.get(fldName);
        if (value == null)
            return null;

        int len = value.length();
        if (len > 1 && value.charAt(0) == '\'' && value.charAt(len-1) == '\'')
            // strip the enclosing apostrophes
            return value.substring(1, len-1);
        else
            return value;
    }
    /**
     * Returns generator state - either MODIFY_RECORD, INSERT_RECORD or DELETE_RECORD.
     */
    public int getState() {
        return state;
    }
    /**
     *
     */
    public void setState(int state) {
        this.state = state;
    }
    /**
     * Returns string of comma-separated field values of this SQLGenerator object.
     */
    public String getValues() {
        StringBuffer buf = new StringBuffer();
        int numElems = fields.size();

        int i = 0;
        Collection values = fields.values();
        for (Iterator iter = values.iterator(); iter.hasNext();) {

            String value = (String)iter.next();
            buf.append(value);
            if (i++ != numElems-1)
                buf.append(", ");
        }

        return buf.toString();
    }
    /**
     * Generates UPDATE statement (without constraint part).
     */
    public String updateStatement() {
        StringBuffer buf = new StringBuffer();
        int numElems = fields.size();

        buf.append("UPDATE ");
        buf.append(tableName);
        buf.append(" SET ");

        int i = 0;
        Set names = fields.keySet();
        Collection values = fields.values();

        Set entries = fields.entrySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {

            Map.Entry entry = (Map.Entry)iter.next();
            String name = (String)entry.getKey();
            String value = (String)entry.getValue();

            // skip primary key field from update statement
            if (pkField != null && pkField.equals(name)) {
                continue;
            }

            // not first field
            if (i++ != 0) {
                buf.append(',');
            }

            buf.append(name);
            buf.append('=');
            buf.append(value);
        }
        buf.append(' ');

        return buf.toString();
    }
    /**
     * Generates INSERT statment.
     */
    public String insertStatement() {
        StringBuffer buf = new StringBuffer();
        int numElems = fields.size();

        buf.append("INSERT INTO ");
        buf.append(tableName);
        buf.append(" (");

        int i = 0;

        Set names = fields.keySet();
        for (Iterator iter = names.iterator(); iter.hasNext();) {

            String name = (String)iter.next();
            buf.append(name);
            if (i++ != numElems-1)
                buf.append(',');
        }
        buf.append(") VALUES(");
        buf.append(this.getValues());
        buf.append(')');

        return buf.toString();
    }

    /**
     * Generates DELETE statement (withoud constraint part)
     */
    public String deleteStatement() {
        return "DELETE FROM " + tableName + " ";
    }

    /**
     * Clears SQLGenerator internal state.
     */
    public void clear() {
        fields.clear();
        tableName = "";
        pkField = null;
        state = -1;
    }
    /**
     * Default constructor.
     */
    public SQLGenerator() {
        fields = new LinkedHashMap();
        tableName = "";
        pkField = null;
        state = -1;
    }
    /**
     * Overrides Object.clone() method.
     */
    public Object clone() {

        SQLGenerator theNew = null;
        try {
            theNew = (SQLGenerator)super.clone();
            theNew.fields = new LinkedHashMap(fields);
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.toString(), e);
        }

        return theNew;
    }
}
