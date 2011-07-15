/*
 * Created on 14.02.2007
 */
package eionet.meta.exports.codelist;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;

import eionet.meta.DDRuntimeException;
import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.FixedValue;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 *
 * @author jaanus
 */
public class CodelistXML extends Codelist {

    /** */
    public static final String DD_NAMESPACE = "dd";
    public static final String TAG_VALUE_LISTS = "value-lists";
    public static final String TAG_VALUE_LIST = "value-list";
    public static final String TAG_VALUE = "value";
    public static final String TAG_DEFINITION = "definition";
    public static final String TAG_SHORT_DESC = "shortDescription";

    /** */
    public static final String ATTR_ELEMENT = "element";
    public static final String ATTR_TABLE = "table";
    public static final String ATTR_DATASET = "dataset";
    public static final String ATTR_FIXED = "fixed";
    public static final String ATTR_VALUE = "value";

    /** */
    private static final String KEY_NS_ID = "ns-id";
    private static final String KEY_NS_URL = "ns-url";

    /** */
    private Vector namespaces = null;

    /**
     *
     * @param conn
     * @param writer
     * @param delim
     */
    public CodelistXML(Connection conn, PrintWriter writer) {

        this.writer = writer;
        if (conn != null) {
            searchEngine = new DDSearchEngine(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.exports.codelist.Codelist#write(java.lang.String, java.lang.String)
     */
    public void write(String objID, String objType) throws Exception {

        Vector elms = new Vector();
        if (objType.equalsIgnoreCase(ELM)) {
            DataElement elm = searchEngine.getDataElement(objID);
            if (elm != null) {
                elms.add(elm);
            }
        } else if (objType.equalsIgnoreCase(TBL)) {
            elms = searchEngine.getDataElements(null, null, null, null, objID);
        } else if (objType.equalsIgnoreCase(DST)) {
            elms = searchEngine.getAllDatasetElements(objID);
        } else {
            throw new IllegalArgumentException("Unknown object type: " + objType);
        }

        initNamespaces();
        writeHeader();
        write(elms, objType);
        writeFooter();
    }

    /**
     *
     *
     */
    private void writeHeader() {
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        StringBuffer line = new StringBuffer("<");
        line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LISTS);

        for (int i = 0; namespaces != null && i < namespaces.size(); i++) {
            Hashtable ns = (Hashtable) namespaces.get(i);
            String nsID = (String) ns.get(KEY_NS_ID);
            String nsURL = (String) ns.get(KEY_NS_URL);
            line.append(" xmlns:").append(nsID).append("=\"").append(nsURL).append("\"");
        }
        line.append(">");
        lines.add(line);
    }

    /**
     *
     *
     */
    private void writeFooter() {
        StringBuffer line = new StringBuffer("</");
        line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LISTS).append(">");
        lines.add(line);
    }

    /**
     *
     * @param elms
     * @throws Exception
     */
    private void write(Vector elms, String objType) throws Exception {

        if (elms == null || elms.isEmpty()) {
            return;
        }

        boolean elmObjType = objType.equalsIgnoreCase(ELM);

        for (int i = 0; elms != null && i < elms.size(); i++) {

            DataElement elm = (DataElement) elms.get(i);
            String elmIdf = elm.getIdentifier();
            if (elmIdf == null || elmIdf.trim().length() == 0) {
                throw new DDRuntimeException("Failed to get the element's identifier");
            }

            StringBuffer line = new StringBuffer();
            line.append("\t<").append(DD_NAMESPACE).append(":").append(TAG_VALUE_LIST);
            line.append(" ");
            line.append(ATTR_ELEMENT).append("=\"").append(elmIdf).append("\"");

            if (!elmObjType || (elmObjType && !elm.isCommon())) {

                String tblIdf = elm.getTblIdentifier();
                if (tblIdf == null || tblIdf.trim().length() == 0) {
                    throw new DDRuntimeException("Failed to get the table's identifier");
                }

                String dstIdf = elm.getDstIdentifier();
                if (dstIdf == null || dstIdf.trim().length() == 0) {
                    throw new DDRuntimeException("Failed to get the dataset's identifier");
                }

                line.append(" ");
                line.append(ATTR_TABLE).append("=\"").append(tblIdf).append("\" ").append(ATTR_DATASET).append("=\"")
                        .append(dstIdf).append("\"");
            }

            if (elm.getType() != null && elm.getType().equals("CH1")) {
                line.append(" ");
                line.append(ATTR_FIXED).append("=\"").append(Boolean.TRUE).append("\"");
            }

            line.append(">");

            Vector elmLines = new Vector();
            elmLines.add(line);

            int valuesAdded = 0;
            Vector fxvs = searchEngine.getFixedValues(elm.getID());

            for (int j = 0; fxvs != null && j < fxvs.size(); j++) {

                FixedValue fxv = (FixedValue) fxvs.get(j);
                String value = fxv.getValue();

                if (value != null && value.trim().length() > 0) {

                    line = new StringBuffer("\t\t<");
                    line.append(DD_NAMESPACE).append(":").append(TAG_VALUE);
                    line.append(" ").append(ATTR_VALUE).append("=\"");
                    line.append(StringEscapeUtils.escapeXml(value));
                    line.append("\">");
                    elmLines.add(line);

                    String definition = fxv.getDefinition();
                    if (definition != null && definition.trim().length() > 0) {

                        line = new StringBuffer("\t\t\t<");
                        line.append(DD_NAMESPACE).append(":").append(TAG_DEFINITION).append(">");
                        elmLines.add(line);

                        line = new StringBuffer("\t\t\t\t").append(StringEscapeUtils.escapeXml(definition));
                        elmLines.add(line);

                        line = new StringBuffer("\t\t\t</");
                        line.append(DD_NAMESPACE).append(":").append(TAG_DEFINITION).append(">");
                        elmLines.add(line);
                    }

                    String shortDesc = fxv.getShortDesc();
                    if (shortDesc != null && shortDesc.trim().length() > 0) {

                        line = new StringBuffer("\t\t\t<");
                        line.append(DD_NAMESPACE).append(":").append(TAG_SHORT_DESC).append(">");
                        elmLines.add(line);

                        line = new StringBuffer("\t\t\t\t").append(StringEscapeUtils.escapeXml(shortDesc));
                        elmLines.add(line);

                        line = new StringBuffer("\t\t\t</");
                        line.append(DD_NAMESPACE).append(":").append(TAG_SHORT_DESC).append(">");
                        elmLines.add(line);
                    }

                    line = new StringBuffer("\t\t</");
                    line.append(DD_NAMESPACE).append(":").append(TAG_VALUE).append(">");
                    elmLines.add(line);

                    valuesAdded++;
                }
            }

            if (valuesAdded > 0) {

                // end value-list tag
                line = new StringBuffer("\t</");
                line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LIST).append(">");
                elmLines.add(line);

                lines.addAll(elmLines);
            }
        }
    }

    /**
     *
     *
     */
    private void initNamespaces() throws Exception {
        namespaces = new Vector();
        Hashtable ns = new Hashtable();
        ns.put(KEY_NS_ID, "xsi");
        ns.put(KEY_NS_URL, "http://www.w3.org/2001/XMLSchema-instance");
        namespaces.add(ns);

        String jspURLPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
        if (jspURLPrefix == null || jspURLPrefix.length() == 0)
            throw new Exception("Missing " + PropsIF.JSP_URL_PREFIX + " property!");
        if (jspURLPrefix.endsWith("/"))
            jspURLPrefix = jspURLPrefix.substring(0, jspURLPrefix.length() - 1);

        ns = new Hashtable();
        ns.put(KEY_NS_ID, "dd");
        ns.put(KEY_NS_URL, jspURLPrefix);
        namespaces.add(ns);
    }
}
