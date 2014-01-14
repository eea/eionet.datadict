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

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.util.CellRangeAddressList;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.FixedValue;
import eionet.meta.exports.CachableIF;
import eionet.meta.exports.pdf.PdfUtil;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

/**
 * Excel template generator for a table.
 */
public class TblXls extends Xls implements CachableIF {

    /** Default file name. */
    private static final String DEFAULT_FILE_NAME = "table.xls";
    /** Cell name suffix when creating formulas for drop-down items. */
    private static final String CELL_NAME_SUFFIX_FOR_DROP_DOWN_FORMULA = "hiddenfxv";

    /** Name of sheet that store fixed values. Value is set from Props. */
    private String dropDownReferencesHiddenSheetName = null;
    /** Hidden sheet to store fixed values for drop-down menu. */
    private HSSFSheet dropDownReferencesHiddenSheet = null;
    /** Hidden sheet row index. */
    private int dropDownReferencesHiddenSheetNewIndex = 0;
    /** ID for this Xls (it can be table id or dataset id) */
    protected String xlsId = null;

    /**
     * Class constructor.
     */
    public TblXls() {
        fileName = TblXls.DEFAULT_FILE_NAME;
        wb = new HSSFWorkbook();
    }

    /**
     * 
     * Class constructor.
     * 
     * @param conn
     */
    public TblXls(Connection conn) {
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
    public TblXls(DDSearchEngine searchEngine, OutputStream os) {
        this();
        this.searchEngine = searchEngine;
        this.os = os;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.xls.XlsIF#create(java.lang.String)
     */
    @Override
    public void create(String xlsId) throws Exception {
        create(xlsId, false);
    }

    /**
     * 
     * @param xlsId
     * @param caching
     * @throws Exception
     */
    protected void create(String xlsId, boolean caching) throws Exception {
        // don't create if its already in cache
        if (!caching && isCached(xlsId)) {
            fileName = cacheFileName;
            return;
        }
        this.xlsId = xlsId;

        createHiddenSheetForDropdownMenuReferences();
        generateContent(xlsId);
        setSchemaUrl();
    }   

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.xls.XlsIF#write()
     */
    @Override
    public void write() throws Exception {
        write(false);
    }

    /**
     * 
     * @param caching
     * @throws Exception
     */
    protected void write(boolean caching) throws Exception {
        // if available in cache, write from cache and return
        if (!caching && cacheFileName != null) {
            writeFromCache();
            return;
        }

        wb.write(os);
    }

    /**
     * Creates a hidden sheet to store drop-down menu items values.
     */
    private void createHiddenSheetForDropdownMenuReferences() {
        this.dropDownReferencesHiddenSheetName = Props.getProperty(PropsIF.XLS_DROPDOWN_FXV_SHEET);
        this.dropDownReferencesHiddenSheetNewIndex = 0;
        this.dropDownReferencesHiddenSheet = wb.createSheet(dropDownReferencesHiddenSheetName);
        this.wb.setSheetHidden(0, true);// hide references sheet
        HSSFRow row = this.dropDownReferencesHiddenSheet.createRow(this.dropDownReferencesHiddenSheetNewIndex);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("Please do not delete or modify this sheet!!! It is used for drop-down items in this file for your convenience.");
        this.dropDownReferencesHiddenSheetNewIndex++;
    }

    /**
     * 
     * @param tblID
     * @throws Exception
     */
    protected void generateContent(String tblID) throws Exception {
        DsTable tbl = searchEngine.getDatasetTable(tblID);
        if (tbl == null) {
            throw new Exception("Table " + tblID + " not found!");
        }
        // fileName = tbl.getDatasetName() + "_" + tbl.getShortName() + FILE_EXT;
        // for the fileName we now use Identifier, cause short name might contain characters
        // illegal for a filename
        fileName = tbl.getDstIdentifier() + "_" + tbl.getIdentifier() + FILE_EXT;

        sheet = wb.createSheet(tbl.getIdentifier());
        row = sheet.createRow(0);
        addElements(tbl);
        sheet.createFreezePane(0, 1);
    }

    /**
     * 
     * @param tbl
     * @throws Exception
     */
    protected void addElements(DsTable tbl) throws Exception {
        Vector<DataElement> elems = searchEngine.getDataElements(null, null, null, null, tbl.getID());
        if (elems == null || elems.size() == 0) {
            return;
        }

        for (int i = 0; i < elems.size(); i++) {
            addElement(elems.get(i), (short) i);
        }
    }

    /**
     * 
     * @param elm
     * @param index
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    private void addElement(DataElement elm, short index) throws Exception {
        HSSFCell cell = row.createCell(index);
        String title = elm.getIdentifier();
        title = PdfUtil.processUnicode(title);
        setColWidth(title, index);
        cell.setCellValue(title);
        cell.setCellStyle(getStyle(ElmStyle.class));

        // if element has fixed values, add a drop-down and validation for the cell
        if (elm.getType().equals("CH1")) {
            Vector<FixedValue> fxvs = searchEngine.getFixedValues(elm.getID());
            if (fxvs != null && fxvs.size() > 0) {
                // create a row for fixed values
                HSSFRow refRow = dropDownReferencesHiddenSheet.createRow(dropDownReferencesHiddenSheetNewIndex);
                HSSFCell refCell = refRow.createCell(0);
                // set a label
                refCell.setCellValue("Fixed Values of " + title);
                // add each fxv to a new column
                for (int i = 0; i < fxvs.size(); i++) {
                    refCell = refRow.createCell(i + 1);
                    String value = PdfUtil.processUnicode(fxvs.get(i).getValue());
                    refCell.setCellValue(value);
                }

                // create a name cell for formula reference
                String name = TblXls.CELL_NAME_SUFFIX_FOR_DROP_DOWN_FORMULA + dropDownReferencesHiddenSheetNewIndex;
                HSSFName namedCell = wb.createName();
                namedCell.setNameName(name);
                String endColumnLetter = CellReference.convertNumToColString(fxvs.size());
                int rowNum = dropDownReferencesHiddenSheetNewIndex + 1;// row num is one greater than index
                // reference starts from column B because column A is used as a label
                StringBuilder sb = new StringBuilder();
                sb.append("'").append(dropDownReferencesHiddenSheetName).append("'!");// reference sheet name
                sb.append("$B$").append(rowNum).append(":");// starting cell ($column$row)
                sb.append("$").append(endColumnLetter).append("$").append(rowNum);// end cell
                namedCell.setRefersToFormula(sb.toString());

                // set constraints and drop-down items to current sheet
                DVConstraint constraintForElement = DVConstraint.createFormulaListConstraint(name);
                CellRangeAddressList fixedValuesForElement = new CellRangeAddressList(1, Short.MAX_VALUE, index, index);// span all
                                                                                                                        // column
                HSSFDataValidation dataValidation = new HSSFDataValidation(fixedValuesForElement, constraintForElement);
                dataValidation.setSuppressDropDownArrow(false);
                sheet.addValidationData(dataValidation);

                // increment row index by one
                dropDownReferencesHiddenSheetNewIndex++;
            }
        }
    }
    
    /**
     * Set schema urls
     * @throws Exception when an error occurs in super class method
     */
    protected void setSchemaUrl() throws Exception {
        setSchemaUrl("TBL" + this.xlsId);
    }

    /**
     * 
     * @param title
     * @param index
     */
    @SuppressWarnings("deprecation")
    private void setColWidth(String title, short index) {
        short width = (short) (title.length() * ElmStyle.FONT_HEIGHT * 50);
        sheet.setColumnWidth(index, width);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.xls.XlsIF#getName()
     */
    @Override
    public String getName() {
        return fileName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.CachableIF#updateCache(java.lang.String)
     */
    @Override
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
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception ee) {
                }
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.CachableIF#clearCache(java.lang.String)
     */
    @Override
    public void clearCache(String id) throws Exception {
        String fn = deleteCacheEntry(id, conn);
        File file = new File(cachePath + fn);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.CachableIF#setCachePath(java.lang.String)
     */
    @Override
    public void setCachePath(String path) throws Exception {
        cachePath = path;
        if (cachePath != null) {
            cachePath.trim();
            if (!cachePath.endsWith(File.separator)) {
                cachePath = cachePath + File.separator;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
     */
    @Override
    public boolean isCached(String id) throws Exception {
        if (searchEngine == null) {
            throw new Exception("TblXls.isCached(): missing searchEngine!");
        }

        cacheFileName = searchEngine.getCacheFileName(id, "tbl", "xls");
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
     * Called when the output is present in cache. Writes the cached document into the output stream.
     */
    public void writeFromCache() throws Exception {
        if (Util.isEmpty(cachePath)) {
            throw new Exception("Cache path is missing!");
        }
        if (Util.isEmpty(cacheFileName)) {
            throw new Exception("Cache file name is missing!");
        }

        String fullName = cachePath + cacheFileName;
        File file = new File(fullName);
        if (!file.exists()) {
            throw new Exception("Cache file <" + fullName + "> does not exist!");
        }

        int i = 0;
        byte[] buf = new byte[1024];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            while ((i = in.read(buf, 0, buf.length)) != -1) {
                os.write(buf, 0, i);
            }
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
     * @return
     * @throws SQLException
     */
    protected static int storeCacheEntry(String id, String fn, Connection conn) throws SQLException {
        if (id == null || fn == null || conn == null) {
            return -1;
        }

        INParameters inParams = new INParameters();
        PreparedStatement stmt = null;
        try {
            // first delete the old entry
            StringBuffer buf =
                    new StringBuffer().append("delete from CACHE where OBJ_TYPE='tbl' and ARTICLE='xls' and OBJ_ID=").append(
                            inParams.add(id, Types.INTEGER));
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
            stmt.close();

            // now create the new entry
            inParams = new INParameters();
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("OBJ_ID", inParams.add(id, Types.INTEGER));
            map.put("OBJ_TYPE", SQL.surroundWithApostrophes("tbl"));
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

    /**
     * 
     * @param id
     * @param conn
     * @return
     * @throws SQLException
     */
    protected static String deleteCacheEntry(String id, Connection conn) throws SQLException {
        if (id == null || conn == null) {
            return null;
        }

        INParameters inParams = new INParameters();
        StringBuffer buf =
                new StringBuffer("select FILENAME from CACHE where ").append("OBJ_TYPE='tbl' and ARTICLE='xls' and OBJ_ID=")
                        .append(inParams.add(id, Types.INTEGER));

        String fn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                fn = rs.getString(1);
                inParams = new INParameters();
                buf =
                        new StringBuffer("delete from CACHE where ").append("OBJ_TYPE='tbl' and ARTICLE='xls' and OBJ_ID=")
                                .append(inParams.add(id, Types.INTEGER));
                stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
                stmt.executeUpdate();
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return fn;
    }
}
