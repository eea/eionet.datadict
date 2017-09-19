/*
 * Created on Feb 1, 2006
 */
package eionet.meta.exports.mdb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import eionet.meta.DDRuntimeException;
import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaanus
 */
public class MdbFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(MdbFile.class);

    /** */
    public static final String PROP_TMP_FILE_PATH = "mdb.tmp-file-path";
    public static final String PROP_LOG_FILE = "mdb.log-file";
    public static final String PROP_SCHEMA_URL = "mdb.vmd-schema-url";
    public static final String NAMESPACE_PREFIX = "dd";
    public static final String DATASETS_NSID = "1";

    public static final String VMD_TABLENAME = "VALIDATION_METADATA_DO_NOT_MODIFY";
    public static final String[] VMD_COLUMNS = {"TblIdf", "ElmIdf", "TblNr", "TblNsID", "TblNsURL", "TblSchemaURL", "DstIdf",
            "DstNr", "DstNsID", "DstNsURL", "DstSchemaURL", "DstSchemaLocation", "DstsNsID", "DstsNsURL"};

    /** */
    private Connection conn = null;

    private String dstID = null;
    private String dstIdf = null;
    private String dstNsID = null;

    private Dataset dst = null;

    private String fullPath = null;
    private DDSearchEngine searchEngine = null;

    private boolean vmdOnly = false;

    private static String namespaceURLPrefix = null;
    private static String dstSchemaLocationPrefix = null;

    /*
     *
     */
    private MdbFile(Connection conn, String dstID, String fullPath) throws MdbException {

        if (conn == null) {
            throw new MdbException("SQL connection not given");
        }
        if (dstID == null) {
            throw new MdbException("Dataset ID not given");
        }
        if (fullPath == null) {
            throw new MdbException("File path not given");
        }

        this.conn = conn;
        this.dstID = dstID;
        this.fullPath = fullPath;
        searchEngine = new DDSearchEngine(this.conn);
    }

    /*
     *
     */
    public void setVmdOnly(boolean vmdOnly) {
        this.vmdOnly = vmdOnly;
    }

    /*
     *
     */
    public boolean getVmdOnly() {
        return vmdOnly;
    }

    /*
     *
     */
    private File create() throws Exception {

        // if only creating metadata for automatic validation
        if (vmdOnly) {
            return createVmdOnly();
        }

        Dataset dst = searchEngine.getDataset(dstID);
        if (dst == null) {
            throw new MdbException("Dataset not found, id=" + dstID);
        }

        File file = new File(fullPath);
        createDatabase(dst, file);
        return file;
    }

    /**
     *
     * @param dst
     * @param file
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private Database createDatabase(Dataset dst, File file) throws Exception {

        Database db = null;
        try {
            db = Database.create(file);

            Vector tables = searchEngine.getDatasetTables(dstID, true);
            for (int i = 0; tables != null && i < tables.size(); i++) {
                createTable((DsTable) tables.get(i), db);
            }
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Throwable t) {
                }
            }
        }

        return db;
    }

    /**
     * @param tbl
     * @param db
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void createTable(DsTable tbl, Database db) throws Exception {

        if (tbl == null) {
            return;
        }
        if (db == null) {
            return;
        }

        Vector columns = new Vector();
        Vector elems = searchEngine.getDataElements(null, null, null, null, tbl.getID());
        if (elems != null && elems.size() > 0) {
            columns = createColumns(elems);
        }

        boolean tableCreated = false;
        String tableName = tbl.getIdentifier();
        if (columns != null && columns.size() > 0) {
            db.createTable(tableName, columns);
            tableCreated = true;
        }

        if (!tableCreated) {
            db.createTable(tableName, new Vector());
        }
    }

    /**
     *
     * @param elems
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Vector createColumns(Vector elems) throws Exception {

        Vector result = new Vector();

        if (elems == null || elems.size() == 0) {
            return result;
        }

        for (int i = 0; i < elems.size(); i++) {

            DataElement elm = (DataElement) elems.get(i);
            Column col = createColumn(elm);
            if (col != null) {
                result.add(col);
            }
        }

        return result;
    }

    /**
     *
     * @param elm
     * @return
     * @throws SQLException
     */
    private Column createColumn(DataElement elm) throws SQLException {

        if (elm == null) {
            return null;
        }
        String colName = elm.getIdentifier();
        String elmDataType = elm.getAttributeValueByShortName("Datatype");
        if (colName == null || elmDataType == null) {
            return null;
        }

        int colType = Mdb.getMdbType(elmDataType);

        Column col = new Column();
        col.setName(colName);
        col.setSQLType(colType);

        return col;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private File createVmdOnly() throws Exception {

        File file = new File(fullPath);
        Database db = null;
        try {
            db = Database.create(file);

            List cols = getVmdColumns();
            if (cols == null || cols.size() == 0) {
                throw new MdbException("No columns were added for validation metadata");
            }

            db.createTable(VMD_TABLENAME, cols);
            Table vmdTable = db.getTable(VMD_TABLENAME);
            if (vmdTable == null) {
                throw new NullPointerException();
            }

            List rows = createVmdRows();
            if (rows == null || rows.size() == 0) {
                throw new MdbException("No rows were added for validation metadata");
            }

            vmdTable.addRows(rows);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Throwable t) {
                }
            }
        }

        return file;
    }

    /**
     *
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws MdbException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List createVmdRows() throws SQLException, IOException, MdbException {

        // "TblIdf", "ElmIdf", "TblNr", "TblNsID", "TblNsURL", "TblSchemaURL", "DstIdf", "DstNr", "DstNsID", "DstNsURL",
        // "DstSchemaURL", "DstSchemaLocation", "DstsNsID", "DstsNsURL"

        Vector ddTables = searchEngine.getDatasetTables(dstID, true);
        if (ddTables == null || ddTables.size() == 0) {
            return null;
        }

        Vector rows = new Vector();
        for (int i = 0; ddTables != null && i < ddTables.size(); i++) {

            DsTable tbl = (DsTable) ddTables.get(i);

            Vector ddElms = searchEngine.getDataElements(null, null, null, null, tbl.getID());
            for (int j = 0; ddElms != null && j < ddElms.size(); j++) {

                DataElement elm = (DataElement) ddElms.get(j);
                Object[] row = constructVmdRow(getDst(), tbl, elm);
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     *
     * @param dst
     * @param tbl
     * @param elm
     * @return
     */
    public static Object[] constructVmdRow(Dataset dst, DsTable tbl, DataElement elm) {
       
        String datadictUrlBase = Props.getProperty(PropsIF.DD_URL);
        Object[] row = new Object[VMD_COLUMNS.length];

        row[0] = tbl.getIdentifier(); // TblIdf
        row[1] = elm.getIdentifier(); // ElmIdf
        row[2] = tbl.getID(); // TblNr
        row[3] = NAMESPACE_PREFIX + tbl.getNamespace(); // TblNsID
        row[4] = getNamespaceURLPrefix() + tbl.getNamespace(); // TblNsURL
        row[5] = datadictUrlBase +"/v2/dataset/"+dst.getID() +"/schema-tbl-"+tbl.getID()+".xsd";
        row[6] = dst.getIdentifier(); // DstIdf
        row[7] = dst.getID(); // DstNr
        row[8] = NAMESPACE_PREFIX + dst.getNamespaceID(); // DstNsID
        row[9] = getNamespaceURLPrefix() + dst.getNamespaceID(); // DstNsURL
        row[10] = datadictUrlBase +"/v2/dataset/"+dst.getID() +"/schema-dst-"+dst.getID()+".xsd";
        row[11] = getDstSchemaLocationPrefix() + row[8] + " " + row[10];// DstSchemaLocation
        row[12] = NAMESPACE_PREFIX + DATASETS_NSID; // DstsNsID
        row[13] = getNamespaceURLPrefix() + DATASETS_NSID; // DstsNsURL

        return row;
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List getVmdColumns() throws SQLException {
        Vector cols = new Vector();
        for (int i = 0; i < VMD_COLUMNS.length; i++) {
            Column col = new Column();
            String colName = VMD_COLUMNS[i];
            int colType = Mdb.getVmdColumnType(colName);
            col.setName(colName);
            col.setSQLType(colType);
            cols.add(col);
        }
        return cols;
    }

    // /////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////

    /*
     *
     */
    public static File create(Connection conn, String dstID, String fullPath) throws Exception {

        return MdbFile.create(conn, dstID, fullPath, false);
    }

    /*
     *
     */
    public static File create(Connection conn, String dstID, String fullPath, boolean vmdOnly) throws Exception {
        MdbFile mdbFile = new MdbFile(conn, dstID, fullPath);
        mdbFile.setVmdOnly(vmdOnly);
        return mdbFile.create();
    }

    /**
     * This implementation of the main method is not only for testing purposes.
     * It is sometimes indeed invoked in the production environment. So please do not delete it.
     *
     * @param args
     */
    public static void main(String args[]) {

        String dstID = (args != null && args.length > 0) ? args[0] : null;
        String fileFullPath = (args != null && args.length > 1) ? args[1] : null;
        String vmdOnly = (args != null && args.length > 2) ? args[2] : null;

        System.out.println("entered " + MdbFile.class.getName() + ".main() with " + args);

        Connection conn = null;
        try {
            if (dstID == null) {
                throw new MdbException("Missing command line argument for dataset id");
            }
            if (fileFullPath == null) {
                throw new MdbException("Missing command line argument for file full path");
            }

            MdbFile.getProperties();
            conn = ConnectionUtil.getConnection();
            if (vmdOnly == null) {
                MdbFile.create(conn, dstID, fileFullPath);
            } else {
                MdbFile.create(conn, dstID, fileFullPath, Boolean.valueOf(vmdOnly).booleanValue());
            }
        } catch (Throwable t) {
            try {
                SQL.close(conn);
                LOGGER.error(t.getMessage(), t);
            } finally {
                System.exit(1);
            }
        }

        System.exit(0);
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Properties getProperties() throws Throwable {

        Vector v = new Vector();
        v.add(PropsIF.DBDRV);
        v.add(PropsIF.DBURL);
        v.add(PropsIF.DBUSR);
        v.add(PropsIF.DBPSW);

        Properties props = new Properties();

        for (int i = 0; i < v.size(); i++) {
            String propName = (String) v.get(i);
            String propValue = Props.getProperty(propName);
            if (propValue == null || propValue.length() == 0) {
                throw new MdbException("Could not find property: " + propName);
            } else {
                props.setProperty(propName, propValue);
            }
        }

        return props;
    }

    /*
     * FIXME: Move to unit test.
     */
    @SuppressWarnings({"unused", "rawtypes", "unchecked"})
    private static void createTest() throws Exception {

        File file = null;
        if (File.separator.equals("/")) {
            file = new File("/home/jaanus/test.mdb");
        } else {
            file = new File("D:\\projects\\datadict\\doc\\test.mdb");
        }

        Database db = Database.create(file);

        Column a = new Column();
        a.setName("a");
        a.setSQLType(Types.INTEGER);
        Column b = new Column();
        b.setName("b");
        b.setSQLType(Types.VARCHAR);

        Vector v = new Vector();
        v.add(a);
        v.add(b);

        db.createTable("NewTable", v);

        db.close();

    }

    /**
     *
     * @return
     */
    public String getDstID() {
        return dstID;
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public String getDstIdf() throws SQLException {
        if (dstIdf == null) {
            dstIdf = getDst().getIdentifier();
        }
        return dstIdf;
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public Dataset getDst() throws SQLException {

        if (dst == null) {
            dst = searchEngine.getDataset(getDstID());
        }
        return dst;
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public String getDstNsID() throws SQLException {
        if (dstNsID == null) {
            dstNsID = getDst().getNamespaceID();
        }
        return dstNsID;
    }

    /**
     *
     * @return
     */
    private static String getSchemaURLBase() {
        return Props.getRequiredProperty(PROP_SCHEMA_URL);
    }

    /**
     *
     * @return
     */
    private static String getDstSchemaLocationPrefix() {

        if (dstSchemaLocationPrefix == null) {

            String jspURLPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
            if (jspURLPrefix == null || jspURLPrefix.length() == 0) {
                throw new DDRuntimeException("Missing " + PropsIF.JSP_URL_PREFIX + " property!");
            }

            if (!jspURLPrefix.endsWith("/")) {
                jspURLPrefix = jspURLPrefix + "/";
            }

            dstSchemaLocationPrefix = new StringBuffer(jspURLPrefix).append("namespace.jsp?ns_id=").toString();
        }

        return dstSchemaLocationPrefix;
    }

    /**
     *
     * @return
     */
    private static String getNamespaceURLPrefix() {

        if (namespaceURLPrefix == null) {

            String jspURLPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
            if (jspURLPrefix == null || jspURLPrefix.length() == 0) {
                throw new DDRuntimeException("Missing " + PropsIF.JSP_URL_PREFIX + " property!");
            }

            if (!jspURLPrefix.endsWith("/")) {
                jspURLPrefix = jspURLPrefix + "/";
            }

            namespaceURLPrefix = new StringBuffer(jspURLPrefix).append("namespace.jsp?ns_id=").toString();
        }

        return namespaceURLPrefix;
    }
}
