package eionet.meta.exports.xforms;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.Namespace;
import eionet.util.Util;

public class TblXForm extends XForm {

    private static final String REPEAT_ID = "i1";

    private Vector elements = null;
    private Hashtable tblBind = new Hashtable();
    private String dstNs = "";
    private String tblNs = "";

    public TblXForm(DDSearchEngine searchEngine, PrintWriter writer) {
        super(searchEngine, writer);
    }

    @Override
    public void write(String tblID) throws Exception {

        if (Util.isEmpty(tblID))
            throw new Exception("Table ID not specified!");

        // Get the table object.
        DsTable tbl = searchEngine.getDatasetTable(tblID);
        if (tbl == null)
            throw new Exception("Table not found!");

        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T", null, tbl.getDatasetID());
        tbl.setSimpleAttributes(v);

        // get data elements (this will set all the simple attributes of elements)
        elements = searchEngine.getDataElements(null, null, null, null, tblID);

        // set namespaces
        Namespace ns = null;
        String nsID = tbl.getParentNs();
        if (!Util.isEmpty(nsID)) {
            ns = searchEngine.getNamespace(nsID);
            if (ns != null)
                dstNs = ns.getPrefix() + ":";
        }
        nsID = tbl.getNamespace();
        if (!Util.isEmpty(nsID)) {
            ns = searchEngine.getNamespace(nsID);
            if (ns != null)
                tblNs = ns.getPrefix() + ":";
        }

        //
        write(tbl);
    }

    /**
     * Write a schema for a given object.
     */
    private void write(DsTable tbl) throws Exception {

        // set instance
        setInstance(tbl.getID());

        // set controls label
        setControlsLabel(tbl.getDatasetName() + " dataset, " + tbl.getIdentifier() + " table");
        // tbl.getDatasetName() + " dataset, " + tbl.getShortName() + " table");

        // set binds
        setBinds(tbl);

        // set controls
        setControls(tbl);
    }

    private void setBinds(DsTable tbl) {

        // set the table bind
        String bindID = tbl.getIdentifier();
        // String nodeset = "/" + dstNs + tbl.getShortName() + "/" + dstNs + "Row";
        String nodeset = "/" + dstNs + tbl.getIdentifier() + "/" + dstNs + "Row";
        tblBind.put(ATTR_ID, bindID);
        tblBind.put(ATTR_NODESET, nodeset);

        // set element binds
        for (int i = 0; elements != null && i < elements.size(); i++) {

            DataElement elm = (DataElement) elements.get(i);
            bindID = elm.getIdentifier();
            String bindType = elm.getAttributeValueByShortName("Datatype");
            if (bindType == null)
                bindType = DEFAULT_DATATYPE;

            if (elm.isExternalSchema()) {
                nodeset = elm.getIdentifier();
            } else {
                nodeset = tblNs + elm.getIdentifier();
            }
            // nodeset = tblNs + elm.getShortName();

            Hashtable elmBind = new Hashtable();
            elmBind.put(ATTR_ID, bindID);
            elmBind.put(ATTR_TYPE, bindType);
            elmBind.put(ATTR_NODESET, nodeset);

            if (!elm.getType().equalsIgnoreCase("CH1")) {

                String minSize = elm.getAttributeValueByShortName("MinSize");
                String maxSize = elm.getAttributeValueByShortName("MaxSize");
                String minInclusiveValue = elm.getAttributeValueByShortName("MinInclusiveValue");
                String maxInclusiveValue = elm.getAttributeValueByShortName("MaxInclusiveValue");
                String minExclusiveValue = elm.getAttributeValueByShortName("MinExclusiveValue");
                String maxExclusiveValue = elm.getAttributeValueByShortName("MaxExclusiveValue");

                if (minSize != null && minSize.trim().length() > 0) {
                    elmBind.put(ATTR_MINSIZE, minSize);
                }
                if (maxSize != null && maxSize.trim().length() > 0) {
                    elmBind.put(ATTR_MAXSIZE, maxSize);
                }

                if (bindType.equalsIgnoreCase("float") || bindType.equalsIgnoreCase("double")
                        || bindType.equalsIgnoreCase("integer") || bindType.equalsIgnoreCase("decimal")) {

                    if (minInclusiveValue != null && minInclusiveValue.trim().length() > 0) {
                        elmBind.put(ATTR_MIN_INCL_VALUE, minInclusiveValue);
                    }
                    if (maxInclusiveValue != null && maxInclusiveValue.trim().length() > 0) {
                        elmBind.put(ATTR_MAX_INCL_VALUE, maxInclusiveValue);
                    }

                    if (minExclusiveValue != null && minExclusiveValue.trim().length() > 0) {
                        elmBind.put(ATTR_MIN_EXCL_VALUE, minExclusiveValue);
                    }
                    if (maxExclusiveValue != null && maxExclusiveValue.trim().length() > 0) {
                        elmBind.put(ATTR_MAX_EXCL_VALUE, maxExclusiveValue);
                    }
                }
            }

            addBind(elmBind);
        }
    }

    /**
     *
     * @param tbl
     * @throws Exception
     */
    private void setControls(DsTable tbl) throws Exception {

        for (int i = 0; elements != null && i < elements.size(); i++) {

            DataElement elm = (DataElement) elements.get(i);
            String ctrlID = "ctrl_" + elm.getID();
            String ctrlLabel = elm.getAttributeValueByShortName("Name");
            if (ctrlLabel == null)
                ctrlLabel = elm.getShortName(); // Short name is OK to use for label!
            String bind = elm.getIdentifier();
            String ctrlType = DEFAULT_CTRLTYPE;
            String ctrlHint = elm.getAttributeValueByShortName("Definition");
            String ctrlAlert = extractControlAlert(elm);

            Vector fxvs = null;
            String elmType = elm.getType();
            if (elmType != null && elmType.equals("CH1")) {
                fxvs = searchEngine.getFixedValues(elm.getID());
                if (fxvs != null && fxvs.size() > 0)
                    ctrlType = "select1";
            }

            Hashtable control = new Hashtable();
            control.put(ATTR_ID, ctrlID);
            control.put(ATTR_BIND, bind);
            control.put(CTRL_LABEL, ctrlLabel);
            control.put(CTRL_TYPE, ctrlType);
            if (ctrlAlert != null)
                control.put(CTRL_ALERT, ctrlAlert);
            if (ctrlHint != null)
                control.put(CTRL_HINT, ctrlHint);
            if (fxvs != null)
                control.put(CTRL_FXVS, fxvs);
            addControl(control);
        }
    }

    @Override
    protected void writeBinds(String lead) throws Exception {

        // element binds will be written into the table bind

        // start table bind
        String id = (String) tblBind.get(ATTR_ID);
        String nodeset = (String) tblBind.get(ATTR_NODESET);
        StringBuffer buf = new StringBuffer("<f:bind");
        if (id != null)
            buf.append(" id=\"").append(id).append("\"");
        if (nodeset != null)
            buf.append(" nodeset=\"").append(nodeset).append("\"");
        buf.append(">");

        writer.println(lead + buf.toString());

        // write element binds
        writeRegularBinds(lead + "\t");

        // end table bind
        writer.println(lead + "</f:bind>");
    }

    @Override
    protected void writeRepeat(String line) throws Exception {
        line = setAttr(line, "id", REPEAT_ID);
        String tblBindID = (String) tblBind.get(ATTR_ID);
        if (tblBindID != null)
            line = setAttr(line, "bind", tblBindID);

        writer.println(line);
    }

    @Override
    protected void writeInsert(String line) throws Exception {

        String tblBindNodeset = (String) tblBind.get(ATTR_NODESET);
        if (tblBindNodeset != null) {
            line = setAttr(line, "at", "count(" + tblBindNodeset + ")");
            line = setAttr(line, "nodeset", tblBindNodeset);
        } else
            line = setAttr(line, "at", "index('" + REPEAT_ID + "')");

        writer.println(line);
        writeInsertValues(tblBindNodeset, extractLead(line));
    }

    @Override
    protected void writeDelete(String line) throws Exception {

        line = setAttr(line, "at", "index('" + REPEAT_ID + "')");
        String tblBindNodeset = (String) tblBind.get(ATTR_NODESET);
        if (tblBindNodeset != null)
            line = setAttr(line, "nodeset", tblBindNodeset);

        writer.println(line);
    }

    protected void writeInsertValues(String tblBindNodeset, String lead) throws Exception {

        if (tblBindNodeset == null || elements == null)
            return;
        if (lead == null)
            lead = "";

        for (int i = 0; i < elements.size(); i++) {
            DataElement elm = (DataElement) elements.get(i);
            String elmIdf = elm.getIdentifier();
            if (elmIdf != null) {

                StringBuffer buf = new StringBuffer(lead).append("<f:setvalue f:ref=\"").append(tblBindNodeset).append("[index('")
                        .append(REPEAT_ID).append("')]/").append(elm.isExternalSchema() ? "" : this.tblNs).append(elmIdf).append("\"/>");

                writer.println(buf);
            }
        }
    }

    protected String extractControlAlert(DataElement elm) {

        if (elm == null || !elm.getType().equalsIgnoreCase("CH2"))
            return null;

        StringBuffer buf = new StringBuffer("Datatype=");
        String datatype = elm.getAttributeValueByShortName("Datatype");
        if (datatype == null)
            datatype = DEFAULT_DATATYPE;
        buf.append(datatype);

        String[] attrs = { "MinSize", "MaxSize", "MinInclusiveValue", "MaxInclusiveValue", "MinExclusiveValue", "MaxExclusiveValue" };
        for (int i = 0; i < attrs.length; i++) {

            // allow no MinInclusiveValue, MaxInclusiveValue, MinExclusiveValue, MaxExclusiveValue
            // for non-numeric types, even if the user has specified
            if (!datatype.equalsIgnoreCase("float") && !datatype.equalsIgnoreCase("double")
                    && !datatype.equalsIgnoreCase("integer") && !datatype.equalsIgnoreCase("decimal")
                    && attrs[i].endsWith("clusiveValue")) {
                continue;
            }

            String value = elm.getAttributeValueByShortName(attrs[i]);
            if (value != null && value.trim().length() > 0) {
                if (buf.length() > 0)
                    buf.append(";");
                buf.append(attrs[i]).append("=").append(value);
            }
        }

        return buf.toString();
    }
}
