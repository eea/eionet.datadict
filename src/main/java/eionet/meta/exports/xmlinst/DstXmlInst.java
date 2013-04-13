package eionet.meta.exports.xmlinst;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.meta.Namespace;
import eionet.util.Util;


public class DstXmlInst extends XmlInst {

    private static final String DATASETS_NS_ID = "1";

    public DstXmlInst(DDSearchEngine searchEngine, PrintWriter writer) {
        super(searchEngine, writer);
    }

    public void write(String dstID) throws Exception {

        if (Util.isEmpty(dstID)) throw new Exception("Dataset ID not specified!");

        Dataset dst = searchEngine.getDataset(dstID);
        if (dst == null) throw new Exception("Dataset not found!");

        // get the tables
        dst.setTables(searchEngine.getDatasetTables(dstID, true));

        write(dst);
    }

    private void write(Dataset dst) throws Exception {

        // set and add the dataset namespace
        String nsID = dst.getNamespaceID();
        if (!Util.isEmpty(nsID)) {
            Namespace ns = searchEngine.getNamespace(nsID);
            if (ns != null) {
                addNamespace(ns);
                dstNsPrefix = getNamespacePrefix(ns);
                setSchemaLocation(getSchemaLocation(nsID, dst.getID()));
            }
        }

        // set the doc element and add the datasets namespace
        Namespace ns = searchEngine.getNamespace(DATASETS_NS_ID);
        if (ns == null) ns = new Namespace("1", null, null, null, null);
        setDocElement(getNamespacePrefix(ns) + ":" + dst.getIdentifier());
        addNamespace(ns);

        // write tables
        writeTables(dst);
    }

    private void writeTables(Dataset dst) throws SQLException {

        Vector tbls = dst.getTables();
        for (int i = 0; tbls != null && i<tbls.size(); i++) {
            writeTable((DsTable)tbls.get(i));
        }
    }

    private void writeTable(DsTable tbl) throws SQLException {

        // set and add the table's namespace
        String nsID = tbl.getNamespace();
        if (!Util.isEmpty(nsID)) {
            Namespace ns = searchEngine.getNamespace(nsID);
            if (ns != null) {
                addNamespace(ns);
                tblNsPrefix = getNamespacePrefix(ns);
            }
        }

        // write table start tag
        addString(getLead("tbl") + "<" + dstNsPrefix + ":" + tbl.getIdentifier() + ">");
        newLine();

        // get and write the table elements
        writeRows(searchEngine.getDataElements(null, null, null, null, tbl.getID()));

        // write table end tag
        addString(getLead("tbl") + "</" + dstNsPrefix + ":" + tbl.getIdentifier() + ">");
        newLine();
    }

    protected String getSchemaLocation(String nsID, String id) {
        StringBuffer buf = new StringBuffer().
        append(appContext).append("namespace.jsp?ns_id=").append(nsID).append(" ").
        append(appContext).append("GetSchema?id=DST").append(id);

        return buf.toString();
    }

    protected void setLeads() {
        leads = new Hashtable();
        leads.put("tbl", "\t");
        leads.put("row", "\t\t");
        leads.put("elm", "\t\t\t");
    }
}
