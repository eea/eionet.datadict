
package eionet.meta;

import java.util.Vector;

public class DDSearchParameter {

    private String attrID = null;
    private Vector attrValues = null;
    private String valueOper = "=";
    private String idOper = "=";
    private String attrShortName = null;

    public DDSearchParameter(String attrID) {
        this.attrID = attrID;
    }

    public DDSearchParameter(String attrID, Vector attrValues) {
        this(attrID);
        this.attrValues = attrValues;
        legalizeValues();
    }

    public DDSearchParameter(String attrID, Vector attrValues, String valueOper, String idOper) {
        this(attrID, attrValues);
        this.valueOper = valueOper;
        this.idOper = idOper;
    }

    public String getAttrID() {
        return attrID;
    }

    public Vector getAttrValues() {
        return attrValues;
    }

    public String getValueOper() {
        return valueOper;
    }

    public String getIdOper() {
        return idOper;
    }

    public void setAttrShortName(String attrShortName) {
        this.attrShortName = attrShortName;
    }

    public String getAttrShortName() {
        return this.attrShortName;
    }

    public void addValue(String value) {
        if (attrValues == null) attrValues = new Vector();
        attrValues.add(legalize(value));
    }

    public void apostrophizeValues() {
        for (int i = 0; attrValues != null && i < attrValues.size(); i++) {
            String value = (String) attrValues.get(i);
            attrValues.remove(i);
            attrValues.add(i, apostrophize(value));
        }
    }

    private void legalizeValues() {
        for (int i = 0; attrValues != null && i < attrValues.size(); i++) {
            String value = (String) attrValues.get(i);
            attrValues.remove(i);
            attrValues.add(i, legalize(value));
        }
    }

    private String apostrophize(String in) {
        return "'" + in + "'";
    }

    /**
     * Scans the string for single quotes and adds a quote. I.e. make it safe for SQL.
     * Since there is no SQL operation here, why is the data made SQL-safe?
     * @param in - input string
     * @return - the quoted string.
     */
    private String legalize(String in) {
        in = (in != null ? in : "");
        StringBuffer ret = new StringBuffer();

        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'' && i != 0 && i != in.length()-1) {
                ret.append("''");
            } else {
                ret.append(c);
            }
        }
        return ret.toString();
    }

}
