package eionet.meta.exports.schema;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.GetSchema;
import eionet.meta.Namespace;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/*
 *
 * @author jaanus
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Schema implements SchemaIF {

    protected DDSearchEngine searchEngine = null;
    private PrintWriter writer = null;

    private List<String> content = new ArrayList<String>();
    private List<String> namespaces = new ArrayList<String>();
    private List<String> imports = new ArrayList<String>();

    private String identitation = "";
    protected String appContext = "";

    protected String lineTerminator = "\n";

    protected String targetNsUrl = "";
    protected String referredNsPrefix = "";
    protected String referredNsID = "";

    protected Map<String, String> nonAnnotationAttributes = new HashMap<String, String>();

    private int containerness = NOT_IN_CONTAINER;
    private String containerNamespaceID = null;

    /*
     *
     */
    public Schema(DDSearchEngine searchEngine, PrintWriter writer) {

        this.searchEngine = searchEngine;
        this.writer = writer;
        this.lineTerminator = File.separator.equals("/") ? "\r\n" : "\n";

        this.namespaces.add("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");

        this.nonAnnotationAttributes.put("Datatype", "");
        this.nonAnnotationAttributes.put("MinSize", "");
        this.nonAnnotationAttributes.put("MaxSize", "");
        this.nonAnnotationAttributes.put("MinInclusiveValue", "");
        this.nonAnnotationAttributes.put("MaxInclusiveValue", "");
        this.nonAnnotationAttributes.put("MinExclusiveValue", "");
        this.nonAnnotationAttributes.put("MaxExclusiveValue", "");
        this.nonAnnotationAttributes.put("DecimalPrecision", "");
    }

    public void setIdentitation(String identitation) {
        if (identitation != null)
            this.identitation = identitation;
    }

    public void setAppContext(String appContext) {
        if (appContext != null) {
            if (!appContext.endsWith("/"))
                appContext = appContext + "/";
            this.appContext = appContext;
        }
    }

    protected void addString(String s) {
        if (content.size() == 0)
            content.add(identitation + s);
        else
            content.add(s);
    }

    protected void newLine() {
        content.add(lineTerminator + identitation);
    }

    protected void setTargetNsUrl(String nsID) {
        this.targetNsUrl = appContext + "namespace.jsp?ns_id=" + nsID;
    }

    protected void setRefferedNs(Namespace ns) {
        this.referredNsID = ns.getID();
        this.referredNsPrefix = getNamespacePrefix(ns);
    }

    protected String getNamespacePrefix(Namespace ns) {
        return ns == null ? "dd" : "dd" + ns.getID();
    }

    /**
     * Add the list of namespaces into Schema's namespaces.
     *
     * @param addNamespaces
     */
    protected void addNamespaces(List<String> addNamespaces) {
        if (addNamespaces != null) {
            for (String ns : addNamespaces) {
                if (!namespaces.contains(ns)) {
                    namespaces.add(ns);
                }
            }
        }

    }

    protected void addNamespace(Namespace ns) {

        StringBuffer nsDeclaration = new StringBuffer("xmlns:");
        nsDeclaration.append(getNamespacePrefix(ns));
        String url = appContext + "namespace.jsp?ns_id=" + ns.getID();
        nsDeclaration.append("=\"" + url + "\"");

        if (!namespaces.contains(nsDeclaration.toString()))
            namespaces.add(nsDeclaration.toString());
    }

    protected List<String> getNamespaces() {
        return namespaces;
    }

    protected void addNamespace(String prefix, String uri) {

        StringBuffer nsDeclaration = new StringBuffer("xmlns:").
                append(prefix).append("=\"").append(uri).append("\"");

        if (!namespaces.contains(nsDeclaration.toString()))
            namespaces.add(nsDeclaration.toString());
    }

    protected void addContainerImport(String tblID) {

        StringBuffer importClause = new StringBuffer("<xs:import namespace=\"");
        String url = appContext + "namespace.jsp?ns_id=" + this.referredNsID;
        importClause.append(url);
        importClause.append("\" schemaLocation=\"");

        importClause.append(appContext + "GetContainerSchema?id=" + GetSchema.TBL + tblID);

        importClause.append("\"/>");

        if (!imports.contains(importClause.toString()))
            imports.add(importClause.toString());
    }

    protected void addImport(String compID, String compType) {

        StringBuffer importClause = new StringBuffer("<xs:import namespace=\"");
        // String url = appContext + "namespace.jsp?ns_id=" + ns.getID();
        String url = appContext + "namespace.jsp?ns_id=" + this.referredNsID;
        importClause.append(url);
        importClause.append("\" schemaLocation=\"");

        importClause.append(appContext + "GetSchema?id=" + compType + compID);

        importClause.append("\"/>");

        if (!imports.contains(importClause.toString()))
            imports.add(importClause.toString());
    }

    /**
     * Write a schema for an object given by ID.
     */
    public abstract void write(String objID) throws Exception;

    /**
     * Flush the written content into the writer.
     */
    public void flush() throws Exception {

        // write schema header
        if (containerness == NOT_IN_CONTAINER ||
                containerness == FIRST_IN_CONTAINER ||
                containerness == FIRST_AND_LAST_IN_CONTAINER) {

            writeHeader();
        }

        // write imports
        writeImports();

        // write content
        for (int i = 0; i < content.size(); i++) {
            writer.print((String) content.get(i));
        }

        // write schema footer
        if (containerness == NOT_IN_CONTAINER ||
                containerness == LAST_IN_CONTAINER ||
                containerness == FIRST_AND_LAST_IN_CONTAINER) {

            writeFooter();
        }
    }

    protected void writeElemStart(String shortName) {

        addString("<xs:element name=\"");
        addString(shortName);
        addString("\">");
        newLine();
    }

    protected void writeElemEnd() {
        addString("</xs:element>");
    }

    protected void writeAnnotation(Vector simpleAttrs, Vector complexAttrs) throws Exception {

        addString("\t<xs:annotation>");
        newLine();
        addString("\t\t<xs:documentation xml:lang=\"en\">");
        newLine();

        // simple attributes first
        for (int i = 0; simpleAttrs != null && i < simpleAttrs.size(); i++) {

            // get attribute
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            Namespace ns = attr.getNamespace();
            String name = this.getNamespacePrefix(ns) + ":" + attr.getShortName();

            // put attributes value or values into a vector
            String dispMultiple = attr.getDisplayMultiple();
            if (Util.voidStr(dispMultiple))
                dispMultiple = "0";

            Vector values = null;
            if (dispMultiple.equals("1")) {
                values = attr.getValues();
            } else {
                String _value = attr.getValue();
                if (!Util.voidStr(_value)) {
                    values = new Vector();
                    values.add(_value);
                }
            }

            if (values == null || values.size() == 0)
                continue;

            // handle nonAnnotationAttributes
            if (nonAnnotationAttributes.containsKey(attr.getShortName())) {
                nonAnnotationAttributes.put(attr.getShortName(), (String) values.get(0));
                continue;
            }

            // add namespace
            addNamespace(ns);

            // add attr values to annotation
            for (int j = 0; j < values.size(); j++) {
                addString("\t\t\t");
                addString("<" + name + ">");
                addString(escapeCDATA((String) values.get(j)));
                addString("</" + name + ">");
                newLine();
            }
        }

        addString("\t\t</xs:documentation>");
        newLine();
        addString("\t</xs:annotation>");
        newLine();
    }

    protected void writeSequence(Vector children, String tab, String minOcc, String maxOcc)
                                                                            throws Exception {

        if (children == null || children.size() == 0)
            return;

        addString(tab);
        addString("<xs:sequence");

        if (minOcc != null)
            addString(" minOccurs=\"" + minOcc + "\"");
        if (maxOcc != null)
            addString(" maxOccurs=\"" + maxOcc + "\"");
        addString(">");
        newLine();

        for (int i = 0; children != null && i < children.size(); i++) {

            Object o = children.get(i);
            Class oClass = o.getClass();
            String oClassName = oClass.getName();

            DataElement elem = null;
            DsTable dsTable = null;

            if (oClassName.endsWith("DataElement"))
                elem = (DataElement) o;
            else if (oClassName.endsWith("DsTable"))
                dsTable = (DsTable) o;
            else
                continue;

            if (elem != null) {

                // JH120705 - instead of importing schemas of all elements we now import
                // a single schema where the parent table's corresponding namespace is the
                // target namespace and which includes declarations of all elements inside
                // that table. So its kind of like a single container of schemas of all elements.
                // see addImport() reference in TblSchema.java.
                // addImport(elem.getID(), GetSchema.ELM);

                // addNamespace(ns); - substituted with parent's namespace, i.e. referredNs
                // which is added already in parent's write() method

                addString(tab + "\t");
                addString("<xs:element ref=\"");
                addString(referredNsPrefix + ":" + elem.getIdentifier());

                String minOccs = elem.isMandatoryFlag() ? "1" : "0";
                String maxOccs = elem.getValueDelimiter() == null ? "1" : "unbounded";

                addString("\" minOccurs=\"");
                addString(minOccs);
                addString("\" maxOccurs=\"");
                addString(maxOccs);

                if (elem.getValueDelimiter() != null) {

                    String schemaUri = Props.getRequiredProperty(PropsIF.GENERAL_SCHEMA_URI);
                    String delimAttr = Props.getRequiredProperty(PropsIF.MULTIVAL_DELIM_ATTR);
                    String prefixedName = getNamespacePrefix(null) + ":" + delimAttr;
                    addNamespace(getNamespacePrefix(null), schemaUri);

                    addString("\" " + prefixedName + "=\"");
                    addString(elem.getValueDelimiter());
                }

                addString("\"/>");
                newLine();
            } else if (dsTable != null) {

                Namespace ns = null;
                String nsID = dsTable.getNamespace();
                if (!Util.voidStr(nsID))
                    ns = searchEngine.getNamespace(nsID);

                if (ns == null)
                    ns = searchEngine.getNamespace("1");

                addImport(dsTable.getID(), GetSchema.TBL);
                // addNamespace(ns); - substituted with parent's namespace, i.e. referredNs
                // which is added already in parent's write() method

                addString(tab + "\t");
                addString("<xs:element ref=\"");
                addString(referredNsPrefix + ":" + dsTable.getIdentifier());
                // addString(referredNsPrefix + ":" + dsTable.getShortName());

                addString("\" minOccurs=\"");
                addString("1");
                addString("\" maxOccurs=\"");
                addString("1");

                addString("\"/>");
                newLine();
            }
        }

        addString(tab);
        addString("</xs:sequence>");
        newLine();
    }

    private void writeHeader() {

        writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.print(lineTerminator);
        writer.print("<xs:schema targetNamespace=\"");
        writer.print(targetNsUrl);
        writer.print("\" ");

        Iterator<String> iter = namespaces.iterator();
        while (iter.hasNext()) {
            writer.print(iter.next());
            writer.print(" ");
        }

        writer.print("elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">");
        writer.print(lineTerminator);
    }

    private void writeImports() {

        Iterator<String> iter = imports.iterator();
        while (iter.hasNext()) {
            writer.print("\t");
            writer.print((String) iter.next());
            writer.print(lineTerminator);
        }
    }

    private void writeFooter() {
        writer.print(lineTerminator);
        writer.print("</xs:schema>");
    }

    protected PrintWriter getWriter() {
        return writer;
    }

    public void setContainerness(int i) {
        this.containerness = i;
    }

    public void setContainerNamespaceID(String nsID) {
        this.containerNamespaceID = nsID;
    }

    public String getContainerNamespaceID() {
        return containerNamespaceID;
    }

    protected String escape(String s) {

        if (s == null)
            return null;

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<')
                buf.append("&lt;");
            else if (c == '>')
                buf.append("&gt;");
            else if (c == '&')
                buf.append("&amp;");
            else
                buf.append(c);
        }

        return buf.toString();
    }

    protected String escapeCDATA(String s) {

        if (s == null)
            return null;

        StringBuffer buf = new StringBuffer("<![CDATA[");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isISOControl(c)) {
                if (Character.isWhitespace(c))
                    buf.append(c);
            } else
                buf.append(c);
        }

        buf.append("]]>");
        return buf.toString();
    }
}
