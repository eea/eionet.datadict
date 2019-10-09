package eionet.util.sql;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods for building SQL INSERT, UPDATE and DELETE statements.
 *
 * @author Jaanus Heinlaid
 *
 */
public class SQLGenerator implements Cloneable {

    /** Key value pairs of table fields and values. */
    private LinkedHashMap<String, String> fields;
    /** Database table name. */
    private String tableName;
    /** Primary key field in the table. */
    private String pkField;

    /**
     * Sets table name that will be used in SQL statements.
     *
     * @param tableName Database table name.
     */
    public void setTable(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Gets table name.
     *
     * @return table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Set primary key field.
     *
     * @param pkField Primary key field name.
     */
    public void setPKField(String pkField) {
        this.pkField = pkField;
    }

    /**
     * Gets primary key field.
     *
     * @return Primary key field name.
     */
    public String getPKField() {
        return pkField;
    }

    /**
     * Sets field value (it will be put between apostrophes). Apostrophes inside the value will be escaped. If fldValue is already
     * surrounded with apostrophes, then additional apostrophes and escaping will not applied.
     *
     * @param fldName Table field name.
     * @param fldValue Field value to set.
     */
    public void setField(String fldName, String fldValue) {
        int len = fldValue.length();

        if (len > 1 && fldValue.charAt(0) == '\'' && fldValue.charAt(len - 1) == '\'') {
            // fldValue is already escaped
            fields.put(fldName, fldValue);
        } else {
            fields.put(fldName, SQL.toLiteral(fldValue));
        }
    }

    /**
     * Sets unquoted field expression without escaping. Use this method to set numeric or NULL values.
     *
     * @param fldName Table field name.
     * @param fldExpr Field expression to set.
     */
    public void setFieldExpr(String fldName, String fldExpr) {
        fields.put(fldName, fldExpr);
    }

    /**
     * Removes field from the collection.
     *
     * @param fldName Table field name.
     */
    public void removeField(String fldName) {
        fields.remove(fldName);
    }

    /**
     * Gets field value/expression for given field name.
     *
     * @param fldName Table field name.
     * @return Field value.
     */
    public String getFieldValue(String fldName) {
        String value = fields.get(fldName);
        if (value == null) {
            return null;
        }

        int len = value.length();
        if (len > 1 && value.charAt(0) == '\'' && value.charAt(len - 1) == '\'') {
            // strip the enclosing apostrophes
            return value.substring(1, len - 1);
        } else {
            return value;
        }
    }

    /**
     * Returns string of comma-separated field values of this SQLGenerator object.
     *
     * @return List of comma separated field values.
     */
    public String getValues() {
        StringBuffer buf = new StringBuffer();
        int numElems = fields.size();

        int i = 0;
        for (String value : fields.values()) {

            buf.append(value);
            if (i++ != numElems - 1) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    /**
     * Generates UPDATE statement (without constraint part) from the fields and values added by setter methods.
     *
     * @return SQL UPDATE statement without constraint part.
     */
    public String updateStatement() {
        StringBuilder buf = new StringBuilder();

        buf.append("UPDATE ");
        buf.append(tableName);
        buf.append(" SET ");

        int i = 0;

        for (Map.Entry<String, String> entry : fields.entrySet()) {

            String name = entry.getKey();
            String value = entry.getValue();

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
     * Generates INSERT statement from the fields and values added by setter methods.
     *
     * @return SQL INSERT statement.
     */
    public String insertStatement() {
        StringBuilder buf = new StringBuilder();
        int numElems = fields.size();

        buf.append("INSERT INTO ");
        buf.append(tableName);
        buf.append(" (");

        int i = 0;

        for (String name : fields.keySet()) {
            buf.append(name);
            if (i++ != numElems - 1) {
                buf.append(',');
            }
        }
        buf.append(") VALUES(");
        buf.append(this.getValues());
        buf.append(')');

        return buf.toString();
    }

    /**
     * Generates DELETE statement (without constraint part).
     *
     * @return SQL DELETE statement without constraint part.
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
    }

    /**
     * Default constructor.
     */
    public SQLGenerator() {
        fields = new LinkedHashMap<String, String>();
        tableName = "";
        pkField = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {

        SQLGenerator theNew = null;
        try {
            theNew = (SQLGenerator) super.clone();
            theNew.fields = new LinkedHashMap<String, String>(fields);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.toString(), e);
        }

        return theNew;
    }
}
