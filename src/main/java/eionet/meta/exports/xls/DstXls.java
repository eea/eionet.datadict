package eionet.meta.exports.xls;

import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

/**
 * Excel template creator for a dataset.
 * 
 * @author Jaanus
 */
public class DstXls extends TblXls {

    /** Default file name. */
    private static final String DEFAULT_FILE_NAME = "dataset.xls";

    /** All tables in dataset. */
    private Vector<DsTable> tables = null;

    /**
     * 
     * Class constructor.
     */
    public DstXls() {
        super();
        this.fileName = DstXls.DEFAULT_FILE_NAME;
    }

    /**
     * 
     * Class constructor.
     * 
     * @param conn
     */
    public DstXls(Connection conn) {
        this();
        this.conn = conn;
        this.searchEngine = new DDSearchEngine(this.conn);
    }

    /**
     * 
     * Class constructor.
     * 
     * @param searchEngine
     * @param os
     */
    public DstXls(DDSearchEngine searchEngine, OutputStream os, boolean withDropDown) {
        this();
        this.searchEngine = searchEngine;
        this.os = os;
        this.withDropDown = withDropDown;
    }

    /**
     * Create empty sheet for table
     * 
     * @param dstID
     * @throws Exception
     */
    @Override
    protected void createEmptySheets(String dstID) throws Exception {
        Dataset dst = searchEngine.getDataset(dstID);
        if (dst == null) {
            throw new Exception("Dataset " + dstID + " not found!");
        }

        tables = searchEngine.getDatasetTables(dstID, true);
        for (int i = 0; tables != null && i < tables.size(); i++) {
            wb.createSheet(tables.get(i).getIdentifier());
        }
    }

    /**
     * 
     * @param dstID
     * @throws Exception
     */
    @Override
    protected void generateContent(String dstID) throws Exception {
        Dataset dst = searchEngine.getDataset(dstID);
        if (dst == null) {
            throw new Exception("Dataset " + dstID + " not found!");
        }
        // fileName = dst.getShortName() + FILE_EXT;
        // for the fileName we now use Identifier, cause short name might contain characters
        // illegal for a filename
        fileName = dst.getIdentifier() + FILE_EXT;
        for (int i = 0; tables != null && i < tables.size(); i++) {
            addTable(tables.get(i));
        }
    }

    /**
     * 
     * @param tbl
     * @throws Exception
     */
    private void addTable(DsTable tbl) throws Exception {
        sheet = wb.getSheet(tbl.getIdentifier());
        if (sheet == null) {
            sheet = wb.createSheet(tbl.getIdentifier());
        }
        row = sheet.createRow(0);
        addElements(tbl);
        sheet.createFreezePane(0, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.xls.TblXls#setSchemaUrl()
     */
    @Override
    protected void setSchemaUrl() throws Exception {
        setSchemaUrls(this.xlsId, tables);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
     */
    @Override
    public boolean isCached(String id) throws Exception {
        if (searchEngine == null) {
            throw new Exception("DstXls.isCached(): missing searchEngine!");
        }

        cacheFileName = searchEngine.getCacheFileName(id, "dst", "xls");
        if (Util.isEmpty(cacheFileName)) {
            return false;
        }

        // if the file is referenced in CACHE table, but does not actually exist, we say false
        File file = new File(cachePath + cacheFileName);
        if (!file.exists()) {
            cacheFileName = null;
            return false;
        }

        return true;
    }

    /**
     * 
     * @param id
     * @param fn
     * @param conn
     * @throws SQLException
     */
    protected int storeCacheEntry(String id, String fn, Connection conn) throws SQLException {
        if (id == null || fn == null || conn == null) {
            return -1;
        }

        INParameters inParams = new INParameters();
        PreparedStatement stmt = null;
        try {
            // first delete the old entry
            StringBuffer buf =
                    new StringBuffer().append("delete from CACHE where OBJ_TYPE='dst' and ARTICLE='xls' and OBJ_ID=").append(
                            inParams.add(id, Types.INTEGER));
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
            stmt.close();

            // now create the new entry
            inParams = new INParameters();
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("OBJ_ID", inParams.add(id, Types.INTEGER));
            map.put("OBJ_TYPE", SQL.surroundWithApostrophes("dst"));
            map.put("ARTICLE", SQL.surroundWithApostrophes("xls"));
            map.put("FILENAME", SQL.surroundWithApostrophes(fn));
            map.put("CREATED", inParams.add(String.valueOf(System.currentTimeMillis()), Types.BIGINT));

            stmt = SQL.preparedStatement(SQL.insertStatement("CACHE", map), inParams, conn);
            return stmt.executeUpdate();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

}
