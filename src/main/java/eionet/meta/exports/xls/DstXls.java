package eionet.meta.exports.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.meta.exports.CachableIF;
import eionet.meta.exports.pdf.PdfUtil;
import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

public class DstXls extends Xls implements XlsIF, CachableIF{

    private Vector tables = new Vector();

    public DstXls() {
        fileName = "dataset.xls";
        wb = new HSSFWorkbook();
    }

    public DstXls(Connection conn) {
        this();
        this.conn = conn;
        this.searchEngine = new DDSearchEngine(this.conn);
    }

    public DstXls(DDSearchEngine searchEngine, OutputStream os) {
        this();
        this.searchEngine = searchEngine;
        this.os = os;
    }

    public void create(String dstID) throws Exception {
        create(dstID, false);
    }

    private void create(String dstID, boolean caching) throws Exception {

        // don't create if its already in cache
        if (!caching && isCached(dstID)) {
            fileName = cacheFileName;
            return;
        }

        addTables(dstID);
        setSchemaUrls(dstID, tables);
    }

    public void write() throws Exception {
        write(false);
    }

    private void write(boolean caching) throws Exception {

        // if available in cache, write from cache and return
        if (!caching && cacheFileName != null) {
            writeFromCache();
            return;
        }

        wb.write(os);
    }

    private void addTables(String dstID) throws Exception {

        Dataset dst = searchEngine.getDataset(dstID);
        if (dst == null) throw new Exception("Dataset " + dstID + " not found!");
        // fileName = dst.getShortName() + FILE_EXT;
        // for the fileName we now use Identifier, cause short name might contain characters
        // illegal for a filename
        fileName = dst.getIdentifier() + FILE_EXT;
        tables = searchEngine.getDatasetTables(dstID, true);
        for (int i = 0; tables != null && i < tables.size(); i++) {
            addTable((DsTable)tables.get(i));
        }
    }

    private void addTable(DsTable tbl) throws Exception {

        tbl.setGIS(searchEngine.hasGIS(tbl.getID()));
        sheet = wb.createSheet(tbl.getIdentifier());
        row = sheet.createRow(0);
        addElements(tbl);
    }

    private void addElements(DsTable tbl) throws Exception {

        Vector elems = searchEngine.getDataElements(null, null, null, null, tbl.getID());
        if (elems == null || elems.size() == 0) return;

        int done = 0;
        for (int i = 0; i < elems.size(); i++) {
            if (tbl.hasGIS()) {
                DataElement elm = (DataElement)elems.get(i);
                if (elm.getGIS() == null) {
                    addElement((DataElement)elems.get(i), (short)done);
                    done++;
                }
            } else {
                addElement((DataElement)elems.get(i), (short)done);
                done++;
            }
        }

        if (done<elems.size()) {
            sheet = wb.createSheet(tbl.getIdentifier() + "-meta");
            row = sheet.createRow(0);
            done = 0;
            for (int i = 0; i < elems.size(); i++) {
                DataElement elm = (DataElement)elems.get(i);
                if (elm.getGIS() != null) {
                    addElement((DataElement)elems.get(i), (short)done);
                    done++;
                }
            }
        }
    }

    private void addElement(DataElement elm, short index) throws Exception {

        HSSFCell cell = row.createCell(index);
        String title = elm.getIdentifier();
        title = PdfUtil.processUnicode(title);
        setColWidth(title, index);
        cell.setCellValue(title);
        cell.setCellStyle(getStyle(ElmStyle.class));

        /*String elmDataType = "";
        Integer cellType = (Integer)cellTypes.get(elmDataType);
        cellType = cellType == null ? new Integer(HSSFCell.CELL_TYPE_STRING) : cellType;
        cell.setCellType(cellType.intValue());*/
    }

    private void setColWidth(String title, short index) {
        short width = (short)(title.length() * ElmStyle.FONT_HEIGHT * 50);
        sheet.setColumnWidth(index, width);
    }

    public String getName() {
        return fileName;
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.exports.CachableIF#updateCache(java.lang.String)
     */
    public void updateCache(String id) throws Exception {

        create(id, true);
        if (cachePath != null && fileName != null) {
            String fn = cachePath + fileName;
            try {
                os = new FileOutputStream(fn);
                write(true);
                os.flush();
                storeCacheEntry(id, fileName, conn);
            } catch (Exception e) {
                try {
                    File file = new File(fn);
                    if (file.exists()) file.delete();
                }
                catch (Exception ee) {}
            } finally {
                if (os != null) os.close();
            }
        }
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.exports.CachableIF#clearCache(java.lang.String)
     */
    public void clearCache(String id) throws Exception {

        String fn = deleteCacheEntry(id, conn);
        File file = new File(cachePath + fn);
        if (file.exists() && file.isFile())
            file.delete();
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.exports.CachableIF#setCachePath(java.lang.String)
     */
    public void setCachePath(String path) throws Exception {
        cachePath = path;
        if (cachePath != null) {
            cachePath.trim();
            if (!cachePath.endsWith(File.separator))
                cachePath = cachePath + File.separator;
        }
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
     */
    public boolean isCached(String id) throws Exception {
        if (searchEngine == null)
            throw new Exception("DstXls.isCached(): missing searchEngine!");

        cacheFileName = searchEngine.getCacheFileName(id, "dst", "xls");
        if (Util.isEmpty(cacheFileName)) return false;

        // if the file is referenced in CACHE table, but does not actually exist, we say false
        File file = new File(cachePath + cacheFileName);
        if (!file.exists()) {
            cacheFileName = null;
            return false;
        }

        return true;
    }

    /*
     * Called when the output is present in cache.
     * Writes the cached document into the output stream.
     */
    public void writeFromCache() throws Exception {

        if (Util.isEmpty(cachePath)) throw new Exception("Cache path is missing!");
        if (Util.isEmpty(cacheFileName)) throw new Exception("Cache file name is missing!");

        String fullName = cachePath + cacheFileName;
        File file = new File(fullName);
        if (!file.exists()) throw new Exception("Cache file <" + fullName + "> does not exist!");

        int i = 0;
        byte[] buf = new byte[1024];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            while ((i=in.read(buf, 0, buf.length)) != -1)
                os.write(buf, 0, i);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     *
     * @param id
     * @param fn
     * @param conn
     * @throws SQLException
     */
    protected static int storeCacheEntry(String id, String fn, Connection conn) throws SQLException {

        if (id==null || fn==null || conn==null)
            return -1;

        INParameters inParams = new INParameters();
        PreparedStatement stmt = null;
        try {
            // first delete the old entry
            StringBuffer buf = new StringBuffer().
            append("delete from CACHE where OBJ_TYPE='dst' and ARTICLE='xls' and OBJ_ID=").append(inParams.add(id, Types.INTEGER));
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
            stmt.close();

            // now create the new entry
            inParams = new INParameters();
            LinkedHashMap map = new LinkedHashMap();
            map.put("OBJ_ID", inParams.add(id, Types.INTEGER));
            map.put("OBJ_TYPE", SQL.surroundWithApostrophes("dst"));
            map.put("ARTICLE", SQL.surroundWithApostrophes("xls"));
            map.put("FILENAME", SQL.surroundWithApostrophes(fn));
            map.put("CREATED", inParams.add(String.valueOf(System.currentTimeMillis()), Types.BIGINT));

            stmt = SQL.preparedStatement(SQL.insertStatement("CACHE", map), inParams, conn);
            return stmt.executeUpdate();
        } finally {
            try {
                if (stmt != null) stmt.close();
            }
            catch (SQLException e) {}
        }
    }

    /**
     *
     * @param id
     * @param conn
     * @return
     * @throws SQLException
     */
    protected static String deleteCacheEntry(String id, Connection conn) throws SQLException {

        if (id==null || conn==null)
            return null;

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("select FILENAME from CACHE where ").
        append("OBJ_TYPE='dst' and ARTICLE='xls' and OBJ_ID=").append(inParams.add(id, Types.INTEGER));

        String fn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                fn = rs.getString(1);
                inParams = new INParameters();
                buf = new StringBuffer("delete from CACHE where ").
                append("OBJ_TYPE='dst' and ARTICLE='xls' and OBJ_ID=").append(inParams.add(id, Types.INTEGER));
                stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
                stmt.executeUpdate();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            }
            catch (SQLException e) {}
        }

        return fn;
    }
}
