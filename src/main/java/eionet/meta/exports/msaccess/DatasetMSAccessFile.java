package eionet.meta.exports.msaccess;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import eionet.meta.DDException;
import eionet.meta.DDRuntimeException;
import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.meta.FixedValue;
import eionet.meta.exports.mdb.MdbFile;
import eionet.meta.exports.msaccess.columns.CodeListsColumn;
import eionet.meta.exports.msaccess.columns.DstDefinitionColumn;
import eionet.meta.exports.msaccess.columns.ElmDefinitionColumn;
import eionet.meta.exports.msaccess.columns.TblDefinitionColumn;
import eionet.meta.exports.msaccess.columns.TblElmRelationsColumn;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;
import eionet.util.sql.SQL;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class DatasetMSAccessFile {

    /** */
    private String dstId;

    /** */
    private Connection connection;

    /** */
    private File templateFile;
    private File generatedFile;

    /** */
    private DDSearchEngine searchEngine;

    /** */
    private String fileNameForDownload;

    /** */
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** */
    private static List<String> dataTypes = Arrays.asList("boolean", "date", "double", "float", "integer", "string", "decimal");

    /**
     *
     * @param dstId
     */
    public DatasetMSAccessFile(String datasetId) {

        if (datasetId == null || datasetId.trim().length() == 0) {
            throw new IllegalArgumentException("Dataset ID must not be null or empty");
        }
        this.dstId = datasetId;

        templateFile = new File(Props.getRequiredProperty(PropsIF.DATASET_MS_ACCESS_TEMPLTAE).trim());
        if (!templateFile.exists()) {
            throw new DDRuntimeException("MSAccess template file does not exist at " + templateFile.getAbsolutePath());
        }

        // java.io.File(String parent, String child) contract states that if parent is
        // null, the file will be created as if calling simply java.io.File(String child).
        // Therefore no null-checking done here.
        generatedFile = new File(templateFile.getParent(), datasetId + "_" + System.currentTimeMillis() + ".mdb");
    }

    /**
     *
     * @param datasetId
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws DDException
     */
    public static DatasetMSAccessFile create(String datasetId) throws DDException, IOException, SQLException {

        DatasetMSAccessFile msAccessFile = new DatasetMSAccessFile(datasetId);
        msAccessFile.create();
        return msAccessFile;
    }

    /**
     * @throws DDException
     * @throws IOException
     * @throws SQLException
     *
     */
    private void create() throws DDException, IOException, SQLException {

        boolean success = false;
        Database database = null;
        try {
            // copy template file to generated file
            FileUtils.copyFile(templateFile, generatedFile);

            // open generated file as MSAccess database
            database = Database.open(generatedFile);

            // populate the database
            populate(database);

            // close the database
            close(database);
            success = true;
        } finally {
            // close SQL connection
            closeConnection();

            // if there was an exception, do database and file cleanup
            if (!success) {
                cleanup(database);
            }
        }
    }

    /**
     *
     * Try to silently close the database and to delete the generated file.
     *
     * @param database
     */
    private void cleanup(Database database) {

        close(database);
        if (generatedFile != null && generatedFile.exists()) {
            generatedFile.delete();
        }
    }

    /**
     *
     * @param database
     * @throws SQLException
     * @throws DDException
     * @throws IOException
     */
    private void populate(Database database) throws SQLException, DDException, IOException {

        // get the DD dataset
        Dataset dst = getSearchEngine().getDataset(dstId);
        if (dst == null) {
            throw new DDException("No dataset found whit this id: " + dstId);
        } else {
            // set the dataset's dynamic attributes
            dst.setSimpleAttributes(getSearchEngine().getAttributes(dstId, "DS"));
            fileNameForDownload = dst.getIdentifier() + ".mdb";
        }

        // get the tables of this DD dataset
        Vector tables = getSearchEngine().getDatasetTables(dstId, true);
        if (tables == null || tables.isEmpty()) {
            throw new DDException("Dataset does not contain any tables");
        }

        // write the dataset definition row
        Table dstDefinitionTable = database.getTable(FillableTables.DEFINITIONS_DATASET_DO_NOT_MODIFY.toString());
        dstDefinitionTable.addRow(dstDefinitionTable.asRow(dstDefinitionRow(dst, tables.size())));

        // get other definition tables from Access database
        Table codeListsTable = database.getTable(FillableTables.CODE_LIST_DO_NOT_MODIFY.toString());
        Table validationMetadataTable = database.getTable(FillableTables.VALIDATION_METADATA_DO_NOT_MODIFY.toString());
        Table tblDefinitionsTable = database.getTable(FillableTables.DEFINITIONS_TABLES_DO_NOT_MODIFY.toString());
        Table elmDefinitionsTable = database.getTable(FillableTables.DEFINITIONS_ELEMENTS_DO_NOT_MODIFY.toString());
        Table tblElmRelationsTable = database.getTable(FillableTables.DEFINITIONS_TABLEELEMENTS_DO_NOT_MODIFY.toString());

        // hash-set to remember elements that have already been written into element
        // definitions table, to check later that they don't get written twice
        HashSet<String> writtenElmIds = new HashSet<String>();

        // loop through all the DD dataset's tables
        for (int tblIndex = 0; tblIndex < tables.size(); tblIndex++) {

            DsTable tbl = (DsTable) tables.get(tblIndex);

            // get dynamic attributes and all the elements of this DD table
            tbl.setSimpleAttributes(getSearchEngine().getAttributes(tbl.getID(), "T"));
            Vector elems = searchEngine.getDataElements(null, null, null, null, tbl.getID());

            // write table definition row
            tblDefinitionsTable.addRow(tblDefinitionsTable.asRow(tblDefinitionRow(tbl, dst, elems.size())));

            // loop through the table's elements
            if (elems != null && !elems.isEmpty()) {

                for (int elmIndex = 0; elmIndex < elems.size(); elmIndex++) {

                    DataElement elm = (DataElement) elems.get(elmIndex);

                    // write table-to-element relations row
                    tblElmRelationsTable.addRow(tblElmRelationsTable.asRow(tblElmRelationsRow(elm, tbl, elmIndex + 1)));

                    // write validation metadata row for this DD element
                    validationMetadataTable.addRow(MdbFile.constructVmdRow(dst, tbl, elm));

                    // if the element has already be handled in element definitions and code lists,
                    // skip it
                    if (writtenElmIds.contains(elm.getID())) {
                        continue;
                    }

                    // write element definition row
                    elmDefinitionsTable.addRow(elmDefinitionsTable.asRow(elmDefinitionRow(elm, tbl, elmIndex + 1)));

                    // write code lists rows for this DD element
                    Vector<FixedValue> fixedValues = getSearchEngine().getFixedValues(elm.getID());
                    if (fixedValues != null && !fixedValues.isEmpty()) {
                        for (FixedValue fixedValue : fixedValues) {
                            if (!fixedValue.isEmpty()) {

                                codeListsTable.addRow(codeListsTable.asRow(codeListsRow(tbl, elm, fixedValue)));
                            }
                        }
                    }

                    // remember that we have written this element
                    writtenElmIds.add(elm.getID());
                }
            }
        }
    }

    /**
     *
     * @param dst
     * @return
     * @throws IOException
     */
    private HashMap<String, Object> dstDefinitionRow(Dataset dst, int noOfTables) throws IOException {

        RowMap row = new RowMap(DstDefinitionColumn.values());

        row.put(DstDefinitionColumn.DST_IDENTIFIER, dst.getIdentifier());
        row.put(DstDefinitionColumn.DST_SHORTNAME, dst.getShortName());
        row.put(DstDefinitionColumn.DST_NAME, dst.getAttributeValueByShortName("Name"));
        row.put(DstDefinitionColumn.DST_DEFINITION, dst.getAttributeValueByShortName("Definition"));
        row.put(DstDefinitionColumn.DST_METHODOLOGY, dst.getAttributeValueByShortName("Methodology"));
        row.put(DstDefinitionColumn.DST_URL, DefinitionUrls.get(dst));
        row.put(DstDefinitionColumn.DST_NUMBER_OF_TABLES, Integer.valueOf(noOfTables));
        row.put(DstDefinitionColumn.DST_DATE, dateFormat.format(new Date(Long.valueOf(dst.getDate()))));
        row.put(DstDefinitionColumn.DST_STATUS, Util.getStatusSortString(dst.getStatus()));

        return row;
    }

    /**
     *
     * @param tbl
     * @return
     * @throws IOException
     */
    private HashMap<String, Object> tblDefinitionRow(DsTable tbl, Dataset dst, int noOfElements) throws IOException {

        RowMap row = new RowMap(TblDefinitionColumn.values());

        row.put(TblDefinitionColumn.DST_IDENTIFIER, dst.getIdentifier());
        row.put(TblDefinitionColumn.TBL_IDENTIFIER, tbl.getIdentifier());
        row.put(TblDefinitionColumn.TBL_SHORTNAME, tbl.getShortName());
        row.put(TblDefinitionColumn.TBL_NAME, tbl.getAttributeValueByShortName("Name"));
        row.put(TblDefinitionColumn.TBL_DEFINITION, tbl.getAttributeValueByShortName("Definition"));
        row.put(TblDefinitionColumn.TBL_METHODOLOGY, tbl.getAttributeValueByShortName("Methodology"));
        row.put(TblDefinitionColumn.TBL_SHORTDESC, tbl.getAttributeValueByShortName("ShortDescription"));
        row.put(TblDefinitionColumn.TBL_URL, DefinitionUrls.get(tbl));
        row.put(TblDefinitionColumn.TBL_NUMBER_OF_ELEMENTS, Integer.valueOf(noOfElements));
        row.put(TblDefinitionColumn.TBL_ID, tbl.getID());

        return row;
    }

    /**
     *
     * @param elm
     * @param tbl
     * @param elmOrder
     * @return
     * @throws IOException
     */
    private HashMap<String, Object> elmDefinitionRow(DataElement elm, DsTable tbl, int elmOrder) throws IOException {

        RowMap row = new RowMap(TblDefinitionColumn.values());

        row.put(ElmDefinitionColumn.ELM_IDENTIFIER, elm.getIdentifier());
        row.put(ElmDefinitionColumn.ELM_SHORTNAME, elm.getShortName());
        row.put(ElmDefinitionColumn.ELM_COMMON, Boolean.valueOf(elm.isCommon()));
        row.put(ElmDefinitionColumn.ELM_TYPE, castElmType(elm.getType()));
        row.put(ElmDefinitionColumn.ELM_NAME, elm.getAttributeValueByShortName("Name"));
        row.put(ElmDefinitionColumn.ELM_DEFINITION, elm.getAttributeValueByShortName("Definition"));
        row.put(ElmDefinitionColumn.ELM_METHODOLOGY, elm.getAttributeValueByShortName("Methodology"));
        row.put(ElmDefinitionColumn.ELM_DATATYPE, castElmDatatype(elm.getAttributeValueByShortName("Datatype")));
        row.put(ElmDefinitionColumn.ELM_MINSIZE, elmAttrInteger(elm, "MinSize"));
        row.put(ElmDefinitionColumn.ELM_MAXSIZE, elmAttrInteger(elm, "MaxSize"));
        row.put(ElmDefinitionColumn.ELM_DECIMALPRECISION, elmAttrInteger(elm, "DecimalPrecision"));
        row.put(ElmDefinitionColumn.ELM_UNIT, elm.getAttributeValueByShortName("Unit"));
        row.put(ElmDefinitionColumn.ELM_MININCLUSIVE, elm.getAttributeValueByShortName("MinInclusiveValue"));
        row.put(ElmDefinitionColumn.ELM_MAXINCLUSIVE, elm.getAttributeValueByShortName("MaxInclusiveValue"));
        row.put(ElmDefinitionColumn.ELM_MINEXCLUSIVE, elm.getAttributeValueByShortName("MinExclusiveValue"));
        row.put(ElmDefinitionColumn.ELM_MAXEXCLUSIVE, elm.getAttributeValueByShortName("MaxExclusiveValue"));
        row.put(ElmDefinitionColumn.ELM_PUBLICORINTERNAL,
                castElmPublicOrInternal(elm.getAttributeValueByShortName("PublicOrInternal")));
        row.put(ElmDefinitionColumn.ELM_MULTIVALUEDELIM, elm.getValueDelimiter());
        row.put(ElmDefinitionColumn.ELM_URL, DefinitionUrls.get(elm));
        row.put(ElmDefinitionColumn.ELM_ORDER, Integer.valueOf(elmOrder));
        row.put(ElmDefinitionColumn.ELM_ID, elm.getID());
        row.put(ElmDefinitionColumn.ELM_DATASERVICE_DEFINITION, elm.getAttributeValueByShortName("DataserviceDefinition"));
        row.put(ElmDefinitionColumn.ELM_DATASERVICE_NOTE, elm.getAttributeValueByShortName("DataserviceNote"));

        return row;
    }

    /**
     *
     * @param elm
     * @param tbl
     * @param elmOrder
     * @return
     * @throws IOException
     */
    private HashMap<String, Object> tblElmRelationsRow(DataElement elm, DsTable tbl, int elmOrder) throws IOException {

        RowMap row = new RowMap(TblDefinitionColumn.values());

        row.put(TblElmRelationsColumn.ELM_ID, elm.getID());
        row.put(TblElmRelationsColumn.TBL_ID, tbl.getID());
        row.put(TblElmRelationsColumn.ELM_ORDER, Integer.valueOf(elmOrder));
        row.put(TblElmRelationsColumn.ELM_MULTIVALUEDELIM, elm.getValueDelimiter());
        row.put(TblElmRelationsColumn.ELM_MANDATORY, Boolean.valueOf(elm.isMandatoryFlag()));

        return row;
    }

    /**
     *
     * @param elm
     * @return
     * @throws IOException
     */
    private HashMap<String, Object> codeListsRow(DsTable tbl, DataElement elm, FixedValue fixedValue) throws IOException {

        RowMap row = new RowMap(CodeListsColumn.values());

        row.put(CodeListsColumn.ELM_ID, elm.getID());
        row.put(CodeListsColumn.VALUE, fixedValue.getValue());
        row.put(CodeListsColumn.DEFINITION, fixedValue.getDefinition());
        row.put(CodeListsColumn.SHORT_DESC, fixedValue.getShortDesc());

        return row;
    }

    /**
     *
     */
    protected void close(Database database) {

        if (database != null) {
            try {
                database.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     *
     * @return
     * @throws DDConnectionException
     * @throws SQLException
     */
    protected Connection getConnection() throws DDConnectionException, SQLException {

        if (connection == null) {
            connection = ConnectionUtil.getConnection();
        }

        return connection;
    }

    /**
     *
     */
    protected void closeConnection() {

        SQL.close(connection);
    }

    /**
     *
     * @return
     * @throws DDConnectionException
     * @throws SQLException
     */
    protected DDSearchEngine getSearchEngine() throws DDConnectionException, SQLException {

        if (searchEngine == null) {
            searchEngine = new DDSearchEngine(getConnection());
        }

        return searchEngine;
    }

    /**
     *
     * @param elm
     * @param attrName
     * @return
     */
    private Integer elmAttrInteger(DataElement elm, String attrName) {
        int minMaxSizeValue = this.tryHandleMinMaxSizeOfGeometryType(elm, attrName);
        
        if (minMaxSizeValue > -1) {
            return minMaxSizeValue;
        }

        String attrValue = elm.getAttributeValueByShortName(attrName);
        return attrValue == null ? null : Integer.valueOf(attrValue);
    }
    
    private int tryHandleMinMaxSizeOfGeometryType(DataElement elm, String attrName) {
        boolean isMinSizeAttribute = "MinSize".equals(attrName);
        boolean isMaxSizeAttribute = "MaxSize".equals(attrName);
        
        if (isMinSizeAttribute || isMaxSizeAttribute) {
            String elementDataType = elm.getAttributeValueByShortName("Datatype");
            
            if (this.isGeometryDataType(elementDataType)) {
                if (isMinSizeAttribute) {
                    return 0;
                }
                else {
                    return 1000; // must be > 256 for MS Access macros to generate large text column.
                }
            }
        }
        
        return -1;
    }
    
    /**
     *
     * @param type
     * @return
     */
    private Integer castElmType(String type) {

        if (type == null || type.trim().length() == 0) {
            throw new IllegalArgumentException("Element type must not be null or null");
        }

        if (type.equals("CH1") || type.equals("CH3")) {
            return Integer.valueOf(1);
        } else if (type.equals("CH2")) {
            return Integer.valueOf(2);
        } else {
            throw new IllegalArgumentException("Unknown element type: " + type);
        }
    }

    /**
     *
     * @param datatype
     * @return
     */
    private Integer castElmDatatype(String datatype) {

        if (datatype == null || datatype.trim().length() == 0) {
            throw new IllegalArgumentException("Element datatype must not be null or null");
        }

        String coercedDataType = this.coerceElmDataType(datatype);
        int index = dataTypes.indexOf(coercedDataType);
        
        if (index < 0) {
            throw new IllegalArgumentException("Unknown element datatype: " + datatype);
        } else {
            return index + 1;
        }
    }

    private String coerceElmDataType(String dataType) {
        final String stringType = "string";
        
        if ("reference".equals(dataType)) {
            return stringType;
        }
        
        if (this.isGeometryDataType(dataType)) {
            return stringType;
        }
        
        return dataType;
    }
    
    private boolean isGeometryDataType(String dataType) {
        if ("point".equals(dataType)) {
            return true;
        }
        
        if ("linestring".equals(dataType)) {
            return true;
        }
        
        if ("polygon".equals(dataType)) {
            return true;
        }
        
        return false;
    }
    
    /**
     *
     * @param publicOrInternal
     * @return
     */
    private Integer castElmPublicOrInternal(String publicOrInternal) {

        if (publicOrInternal == null || publicOrInternal.trim().length() == 0) {
            return Integer.valueOf(0);
        } else if (publicOrInternal.trim().equals("undefined")) {
            return Integer.valueOf(0);
        } else if (publicOrInternal.trim().equals("Public attribute")) {
            return Integer.valueOf(1);
        } else if (publicOrInternal.trim().equals("Administrative attribute")) {
            return Integer.valueOf(2);
        } else {
            throw new IllegalArgumentException("Unknown value for PublicOrInternal: " + publicOrInternal);
        }
    }

    /**
     * @return the generatedFile
     */
    public File getGeneratedFile() {
        return generatedFile;
    }

    /**
     * @return the fileNameForDownload
     */
    public String getFileNameForDownload() {
        return fileNameForDownload;
    }

    /**
     *
     * @param args
     * @throws DDException
     * @throws IOException
     * @throws SQLException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws DDException, IOException, SQLException, URISyntaxException {

        long time = System.currentTimeMillis();
        DatasetMSAccessFile msAccessFile = new DatasetMSAccessFile("2873");
        msAccessFile.create();
        System.out.println("Done! Time elapsed: " + (System.currentTimeMillis() - time) + " ms");
    }
}
