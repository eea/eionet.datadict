/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Web Dashboards Service
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency (EEA).  Portions created by European Dynamics (ED) company are
 * Copyright (C) by European Environment Agency.  All Rights Reserved.
 * 
 * Contributors(s):
 *    Original code: Dusko Kolundzija (ED)
 *                   Istvan Alfeldi (ED)
 */

package eionet.meta.exports.xmlmeta;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.Namespace;
import eionet.util.Props;
import eionet.util.PropsIF;

public abstract class XmlMeta implements XmlMetaIF {

    protected static final int ROW_COUNT = 1;

    protected DDSearchEngine searchEngine = null;

    private PrintWriter writer = null;

    protected String appContext = "";

    protected String lineTerminator = "\n";

    private String docElement = null;

    protected Hashtable leads = null;

    private String curLead = "";

    private Vector content = new Vector();

    private Vector namespaces = new Vector();

    private String schemaLocation = "";

    public XmlMeta(DDSearchEngine searchEngine, PrintWriter writer) {
        this.searchEngine = searchEngine;
        this.writer = writer;
    }

    public void setAppContext(String appContext) {
        if (appContext != null) {
            if (!appContext.endsWith("/"))
                appContext = appContext + "/";
            this.appContext = appContext;
        }
    }

    protected void addString(String s) {
        content.add(s);
    }

    protected void newLine() {
        content.add(lineTerminator);
    }

    protected String getNamespacePrefix(Namespace ns) {
        return ns.getPrefix();
    }

    protected void addNamespace(Namespace ns) {
        addNamespace(getNamespacePrefix(ns), appContext + "namespace.jsp?ns_id=" + ns.getID());
    }

    protected void addNamespace(String prefix, String url) {

        StringBuffer buf = new StringBuffer("xmlns:").append(prefix).append("=\"").append(url).append("\"");

        if (!namespaces.contains(buf.toString()))
            namespaces.add(buf.toString());
    }

    protected void setDocElement(String docElement) {
        this.docElement = docElement;
    }

    private void startDocElement() {

        writer.print("<" + docElement);
        writeNamespaces();
        writeSchemaLocation();
        writer.print(">");
        writer.print(lineTerminator);
    }

    private void endDocElement() {
        writer.print("</" + docElement + ">");
    }

    private void writeNamespaces() {

        Iterator iter = namespaces.iterator();
        while (iter.hasNext()) {
            writer.print(" ");
            writer.print(iter.next());
        }
    }

    private void writeSchemaLocation() {
        writer.print(" xsi:noNamespaceSchemaLocation=\"" + schemaLocation + "\"");
    }

    private void writeHeader() {
        writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.print(lineTerminator);
    }

    /**
     * Flush the written content into the writer.
     */
    public void flush() throws Exception {

        writeHeader();

        for (int i = 0; i < content.size(); i++) {
            writer.print((String) content.get(i));
        }

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

    protected String getLead(String leadName) {

        if (leads == null || leads.size() == 0) {
            setLeads();
        }

        String lead = (String) leads.get(leadName);
        if (lead == null)
            lead = curLead;
        else
            curLead = lead;

        return lead;
    }

    private void addFixedNamespaces() {

    }

    protected void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    protected void writeTable(DsTable tbl) {
        addString(startTable());
        newLine();

        String id = tbl.getID();
        String name = tbl.getIdentifier();
        String correspondingNS = tbl.getNamespace();
        String parentNS = tbl.getParentNs();

        String fApp = getLead("appContext") + "<appContext>" + appContext + "</appContext>";
        addString(fApp);
        newLine();

        String fId = getLead("tableid") + "<tableid>" + id + "</tableid>";
        addString(fId);
        newLine();

        String fName = getLead("identifier") + "<identifier>" + name + "</identifier>";
        addString(fName);
        newLine();

        String fCorrres = getLead("correspondingNS") + "<correspondingNS>" + correspondingNS + "</correspondingNS>";
        addString(fCorrres);
        newLine();

        String fParentNS = getLead("parentNS") + "<parentNS>" + parentNS + "</parentNS>";
        addString(fParentNS);
        newLine();

        writeElements(tbl.getElements());

        addString(endTable());
        newLine();
    }

    protected void writeElements(Vector elms) {
        addString(startElements());
        newLine();

        for (int i = 0; elms != null && i < elms.size(); i++) {
            addString(startElement());
            newLine();
            DataElement elm = (DataElement) elms.get(i);
            writeElement(elm);
            addString(endElement());
            newLine();
        }

        addString(endElements());
        newLine();
    }

    protected String startTable() {
        return getLead("table") + "<table>";
    }

    private String endTable() {
        return getLead("table") + "</table>";
    }

    protected String startElements() {
        return getLead("elements") + "<elements>";
    }

    private String endElements() {
        return getLead("elements") + "</elements>";
    }

    protected String startElement() {
        return getLead("element") + "<element>";
    }

    private String endElement() {
        return getLead("element") + "</element>";
    }

    /**
     * 
     * @param elem
     */
    private void writeElement(DataElement elem) {

        String id = elem.getID();
        String name = elem.getIdentifier();
        String parentNS = ((Namespace) elem.getNamespace()).getID();

        DElemAttribute datatypeAttr = elem.getAttributeByShortName("Datatype");
        String elementDataType = datatypeAttr.getValue();

        DElemAttribute maxSizeAttr = elem.getAttributeByShortName("MaxSize");
        String elementValueMaxSize = "";
        if (maxSizeAttr == null) {
            elementDataType = "";
            elementValueMaxSize = "";
        } else {
            elementValueMaxSize = maxSizeAttr.getValue();
        }

        DElemAttribute decimalPrecisionAttr = elem.getAttributeByShortName("DecimalPrecision");
        String elementValueDecimalPrecision = decimalPrecisionAttr == null ? "" : decimalPrecisionAttr.getValue();

        String fId = getLead("elementid") + "<elementid>" + id + "</elementid>";
        addString(fId);
        newLine();

        String fName = getLead("identifier") + "<identifier>" + name + "</identifier>";
        addString(fName);
        newLine();

        String fParentNS = getLead("parentNS") + "<parentNS>" + parentNS + "</parentNS>";
        addString(fParentNS);
        newLine();

        String fType = getLead("type") + "<type>" + elementDataType + "</type>";
        addString(fType);
        newLine();

        String fLength = getLead("length") + "<length>" + elementValueMaxSize + "</length>";
        addString(fLength);
        newLine();

        String fPrec = getLead("precision") + "<precision>" + elementValueDecimalPrecision + "</precision>";
        addString(fPrec);
        newLine();

        String multiValueDelim = elem.getValueDelimiter();
        if (multiValueDelim != null && multiValueDelim.length() > 0) {
            String delimAttrName = Props.getRequiredProperty(PropsIF.MULTIVAL_DELIM_ATTR);
            addString(getLead(delimAttrName) + "<" + delimAttrName + ">" + multiValueDelim + "</" + delimAttrName + ">");
            newLine();
        }
    }

    protected abstract String getSchemaLocation(String nsID, String id);

    protected abstract void setLeads();

    public abstract void write(String objID) throws Exception;
}
