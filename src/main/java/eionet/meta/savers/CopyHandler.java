package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.dbschema.DbSchema;
import eionet.util.Util;
import eionet.util.sql.SQL;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class CopyHandler extends OldCopyHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(CopyHandler.class);

    /**  */
    private DDUser user = null;

    /** */
    private HashMap<String, String> oldNewElements = new HashMap<String, String>();
    private HashMap<String, String> oldNewTables = new HashMap<String, String>();
    private HashMap<String, String> oldNewDatasets = new HashMap<String, String>();

    /**
     *
     * @param conn
     * @param ctx
     * @param searchEngine
     */
    public CopyHandler(Connection conn, ServletContext ctx, DDSearchEngine searchEngine) {
        this.conn = conn;
        if (searchEngine != null) {
            this.searchEngine = searchEngine;
        } else {
            this.searchEngine = new DDSearchEngine(conn);
        }
    }

    /**
     *
     * @param user
     */
    public void setUser(DDUser user) {
        this.user = user;
    }

    /**
     *
     * @param dstId
     * @param makeItWorkingCopy
     * @param resetVersionAndStatus
     * @return
     * @throws Exception
     */
    public String copyDst(String dstId, boolean makeItWorkingCopy, boolean resetVersionAndStatus) throws Exception {

        // If no dataset ID provided, exit without further actions.
        if (Util.isEmpty(dstId)) {
            return null;
        }

        LOGGER.debug("Getting ids of datasets to copy ...");

        // Place current id already into the mappings
        oldNewDatasets.put(dstId, null);

        // Find all tables and elements in this datasets, that require copying (only non-common elements require copying).
        findDatasetTablesAndElementsToCopy(dstId);

        // Create new dataset
        String newDstId = createNewDataset(dstId, makeItWorkingCopy, resetVersionAndStatus);
        LOGGER.debug("Created new dataset row with ID = " + newDstId);

        // Create new tables
        for (Entry<String, String> entry : oldNewTables.entrySet()) {
            entry.setValue(createNewTable(entry.getKey()));
        }

        // Create new elements
        for (Entry<String, String> entry : oldNewElements.entrySet()) {
            entry.setValue(createNewElement(entry.getKey(), false, false, false));
        }

        // Copy all the dependencies for the newly created datasets/tables/elements.
        LOGGER.debug("Going to run various copy statements ...");

        copySimpleAttributes();
        copyDocuments();
        copyFixedValues();
        copyFkRelations();
        copyDstToTblRelations();
        copyTblToElmRelations();
        this.copyInferenceRules();

        return newDstId;
    }

    /**
     *
     *
     * @param elmId
     * @param makeItWorkingCopy
     * @param isCopyTbl2ElmRelations
     * @param resetVersionAndStatus
     * @return
     * @throws Exception
     */
    public String copyElm(String elmId, boolean makeItWorkingCopy, boolean isCopyTbl2ElmRelations, boolean resetVersionAndStatus)
    throws Exception {

        // If no element id provided, return without further actions
        if (Util.isEmpty(elmId)) {
            return null;
        }

        // Create new element.
        String newId = createNewElement(elmId, makeItWorkingCopy, isCopyTbl2ElmRelations, resetVersionAndStatus);

        // If new element successfully created and its ID available, copy attributes, fixed values, etc.
        if (newId != null) {

            copySimpleAttributes();
            copyFixedValues();
            copyFkRelations();

            //common elements may have bindings in Vocabularies
            //copyElementBindings();

            // Element-to-table relations will also be copied if required by the method input.
            if (isCopyTbl2ElmRelations) {
                copyElmToTblRelations();
            }
            
            this.copyInferenceRules();
        }
        return newId;
    }

    /**
     *
     * @param tableName
     * @param whereClause
     * @param autoIncColumn
     * @return
     * @throws SQLException if database access fails
     */
    protected int copyAutoIncRow(String tableName, String whereClause, String autoIncColumn) throws SQLException {

        return copyAutoIncRow(tableName, whereClause, autoIncColumn, null);
    }

    /**
     *
     * @param tableName
     * @param whereClause
     * @param autoIncColumn
     * @param newValuesMap
     * @return
     * @throws SQLException if database access fails
     */
    protected int
    copyAutoIncRow(String tableName, String whereClause, String autoIncColumn, final Map<String, Object> newValuesMap)
    throws SQLException {

        if (Util.isEmpty(tableName) || Util.isEmpty(autoIncColumn)) {
            throw new IllegalArgumentException("Table name and auto-increment column name must be given!");
        }

        Map<String, Object> map = newValuesMap == null ? new HashMap<String, Object>() : new HashMap<String, Object>(newValuesMap);
        map.put(autoIncColumn, null);
        String sql = rowsCopyStatement(tableName, whereClause, map);

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            try {
                return Integer.parseInt(getLastInsertID(stmt));
            } catch (Exception e) {
                return 0;
            }
        } finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @param tableName
     * @param whereClause
     * @param newValuesMap
     * @return
     */
    protected static String rowsCopyStatement(String tableName, String whereClause, Map<String, Object> newValuesMap) {

        return rowsCopyStatement(tableName, tableName, whereClause, newValuesMap);
    }

    /**
     *
     * @param tableName
     * @param whereClause
     * @param newValues
     * @return
     */
    protected static String rowsCopyStatement(String tableName, String fromClause, String whereClause,
            Map<String, Object> newValuesMap) {

        if (Util.isEmpty(tableName)) {
            throw new IllegalArgumentException("Table name must be given!");
        }

        LinkedHashMap<String, Object> newValues = new LinkedHashMap<String, Object>();
        if (newValuesMap != null) {
            newValues.putAll(newValuesMap);
        }
        boolean isNewValuesEmpty = Util.isEmpty(newValues);

        List<String> columnNames = DbSchema.getTableColumns(tableName, isNewValuesEmpty ? null : newValues.keySet());
        boolean isColumnNamesEmpty = Util.isEmpty(columnNames);

        StringBuilder sql = new StringBuilder();

        if (isNewValuesEmpty && isColumnNamesEmpty) {

            sql.append("insert into ").append(tableName).append(" select * from ").append(fromClause);

        } else {

            sql.append("insert into ").append(tableName).append(" (");

            int i = 0;
            if (!isNewValuesEmpty) {
                for (String columnName : newValues.keySet()) {
                    sql.append(i++ > 0 ? "," : "").append(columnName);
                }
            }

            String columnNamesCSV = Util.toCSV(columnNames);
            if (!isColumnNamesEmpty) {
                sql.append(i == 0 ? "" : ",").append(columnNamesCSV);
            }

            sql.append(") select ");

            i = 0;
            if (!isNewValuesEmpty) {
                for (Object value : newValues.values()) {
                    sql.append(i++ > 0 ? "," : "").append(value == null ? "NULL" : value);
                }
            }

            if (!isColumnNamesEmpty) {
                sql.append(i == 0 ? "" : ",").append(columnNamesCSV);
            }

            sql.append(" from ").append(fromClause);
        }

        if (!Util.isEmpty(whereClause)) {
            sql.append(" where ").append(whereClause);
        }

        return sql.toString();
    }

    /**
     * @return the oldNewElements
     */
    protected Map<String, String> getOldNewElements() {
        return oldNewElements;
    }

    /**
     * @return the oldNewTables
     */
    protected Map<String, String> getOldNewTables() {
        return oldNewTables;
    }

    /**
     * @return the oldNewDatasets
     */
    protected Map<String, String> getOldNewDatasets() {
        return oldNewDatasets;
    }

    /**
     *
     * @param dstId
     * @throws SQLException if database access fails
     */
    private void findDatasetTablesAndElementsToCopy(String dstId) throws SQLException {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            // Find all tables in this dataset.

            LOGGER.debug("Getting ids of tables to copy ...");

            String sql = "select TABLE_ID from DST2TBL where DATASET_ID=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(dstId));

            rs = pstmt.executeQuery();
            while (rs.next()) {
                oldNewTables.put(rs.getString(1), null);
            }
            SQL.close(rs);

            LOGGER.debug(oldNewTables.size() + " tables found");

            // If any tables found in this dataset, proceed to find all their elements too.

            if (!oldNewTables.isEmpty()) {

                LOGGER.debug("Getting ids of elements to copy ...");

                // Only non-common elements will be copied, hence the "DATAELEM.PARENT_NS is not null" filter
                sql =
                    "select distinct TBL2ELEM.DATAELEM_ID from TBL2ELEM,DATAELEM "
                    + "where TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID and DATAELEM.PARENT_NS is not null and TABLE_ID in ("
                    + Util.toCSV(oldNewTables.keySet()) + ")";
                rs = pstmt.executeQuery(sql);
                while (rs.next()) {
                    oldNewElements.put(rs.getString(1), null);
                }
                LOGGER.debug(oldNewElements.size() + " elements found");
            }
        } finally {
            SQL.close(rs);
            SQL.close(pstmt);
        }
    }

    /**
     *
     * @param dstId
     * @param makeItWorkingCopy
     * @param resetVersionAndStatus
     * @throws SQLException if database access fails
     */
    private String createNewDataset(String dstId, boolean makeItWorkingCopy, boolean resetVersionAndStatus) throws SQLException {

        LOGGER.debug("Copying dataset row ...");

        // Copy the dataset row.

        Map<String, Object> newValues = toValueMap("DATE", Long.valueOf(System.currentTimeMillis()));
        if (makeItWorkingCopy) {
            newValues.put("WORKING_COPY", SQL.toLiteral("Y"));
        }
        if (user != null) {
            newValues.put("USER", SQL.toLiteral(user.getUserName()));
        }
        if (resetVersionAndStatus) {
            newValues.put("VERSION", Integer.valueOf(1));
            newValues.put("REG_STATUS", SQL.toLiteral("Incomplete"));
        } else {
            newValues.put("VERSION", "VERSION+1");
        }
        String newId = String.valueOf(copyAutoIncRow("DATASET", "DATASET_ID=" + dstId, "DATASET_ID", newValues));

        // If dataset row successfully copied and the new auto-generated id available, place it into old-new mappings
        if (newId != null) {
            oldNewDatasets.put(dstId, newId);
        }

        return newId;
    }

    /**
     *
     * @param tblId
     * @throws SQLException if database access fails
     */
    private String createNewTable(String tblId) throws SQLException {

        LOGGER.debug("Copying table row ...");

        // Copy the table row.

        Map<String, Object> newValues = toValueMap("DATE", Long.valueOf(System.currentTimeMillis()));
        if (user != null) {
            newValues.put("USER", SQL.toLiteral(user.getUserName()));
        }
        String newId = String.valueOf(copyAutoIncRow("DS_TABLE", "TABLE_ID=" + tblId, "TABLE_ID", newValues));

        // If table row successfully copied and the new auto-generated id available, place it into old-new mappings
        if (newId != null) {
            oldNewTables.put(tblId, newId);
        }

        return newId;
    }

    /**
     *
     * @param elmId
     * @param makeItWorkingCopy
     * @param isCopyTbl2ElmRelations
     * @param resetVersionAndStatus
     * @throws SQLException if database access fails
     */
    private String createNewElement(String elmId, boolean makeItWorkingCopy, boolean isCopyTbl2ElmRelations,
            boolean resetVersionAndStatus) throws SQLException {

        LOGGER.debug("Copying element row ...");

        // Copy the element row.

        Map<String, Object> newValues = toValueMap("DATE", Long.valueOf(System.currentTimeMillis()));
        if (makeItWorkingCopy) {
            newValues.put("WORKING_COPY", SQL.toLiteral("Y"));
        }
        if (user != null) {
            newValues.put("USER", SQL.toLiteral(user.getUserName()));
        }
        if (resetVersionAndStatus) {
            newValues.put("VERSION", Integer.valueOf(1));
            newValues.put("REG_STATUS", SQL.toLiteral("Incomplete"));
        } else {
            newValues.put("VERSION", "VERSION+1");
        }
        String newId = String.valueOf(copyAutoIncRow("DATAELEM", "DATAELEM_ID=" + elmId, "DATAELEM_ID", newValues));

        // If element row successfully copied and the new auto-generated id available, place it into old-new mappings.
        if (newId != null) {
            oldNewElements.put(elmId, newId);
        }

        return newId;
    }

    /**
     *
     * @throws SQLException if database access fails
     */
    private void copySimpleAttributes() throws SQLException {

        LOGGER.debug("Copying all simple attributes ...");

        if (oldNewDatasets.isEmpty() && oldNewTables.isEmpty() && oldNewElements.isEmpty()) {
            return;
        }

        String possibleOR = "";
        String selectSQL = "select * from ATTRIBUTE where";
        if (!oldNewDatasets.isEmpty()) {
            selectSQL = selectSQL + " (PARENT_TYPE='DS' and DATAELEM_ID in (" + Util.toCSV(oldNewDatasets.keySet()) + "))";
            possibleOR = " or";
        }
        if (!oldNewTables.isEmpty()) {
            selectSQL =
                selectSQL + possibleOR + " (PARENT_TYPE='T' and DATAELEM_ID in (" + Util.toCSV(oldNewTables.keySet()) + "))";
            possibleOR = " or";
        }
        if (!oldNewElements.isEmpty()) {
            selectSQL =
                selectSQL + possibleOR + " (PARENT_TYPE='E' and DATAELEM_ID in (" + Util.toCSV(oldNewElements.keySet()) + "))";
        }

        String insertSQL = "insert into ATTRIBUTE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {

                String oldId = rs.getString("DATAELEM_ID");
                String parentType = rs.getString("PARENT_TYPE");
                String newId = null;
                if (parentType != null) {
                    if (parentType.equals("DS")) {
                        newId = oldNewDatasets.get(oldId);
                    } else if (parentType.equals("T")) {
                        newId = oldNewTables.get(oldId);
                    } else if (parentType.equals("E")) {
                        newId = oldNewElements.get(oldId);
                    }

                }

                if (newId != null) {

                    String mAttrId = rs.getString("M_ATTRIBUTE_ID");
                    String value = rs.getString("VALUE");
                    if (insertSQL.length() > insertSQLLengthBefore) {
                        insertSQL = insertSQL + ",";
                    }
                    insertSQL =
                        insertSQL + "(" + newId + "," + SQL.toLiteral(parentType) + "," + mAttrId + "," + SQL.toLiteral(value)
                        + ")";
                }
            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
    *
    * @throws SQLException if database access fails
    */
   private void copyElementBindings() throws SQLException {

       LOGGER.debug("Copying element bindings ...");

       if (oldNewElements.isEmpty()) {
           return;
       }

       String selectSQL =
           "SELECT * FROM VOCABULARY2ELEM ve LEFT JOIN VOCABULARY_CONCEPT_ELEMENT ev ON ve.DATAELEM_ID = ev.DATAELEM_ID "
               + "WHERE ve.DATAELEM_ID IN (" + Util.toCSV(oldNewElements.keySet()) + ") ORDER BY ve.DATAELEM_ID";

       final String insertBindingSQL = "insert into VOCABULARY2ELEM (DATAELEM_ID, VOCABULARY_ID) values (?, ?) ";

       final String insertValueSQL = "insert into VOCABULARY_CONCEPT_ELEMENT "
                  + "(DATAELEM_ID, VOCABULARY_CONCEPT_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID, LINK_TEXT) values "
                  + "(?, ?, ?, ?, ?, ?) ";

       int insertBindingSQLLengthBefore = insertBindingSQL.length();


       ResultSet rs = null;
       Statement stmt = null;

       PreparedStatement stmtInsertBinding = conn.prepareStatement(insertBindingSQL);
       PreparedStatement stmtInsertValue = conn.prepareStatement(insertValueSQL);

       try {
           stmt = conn.createStatement();
           rs = stmt.executeQuery(selectSQL);
           int prevElemId = 0;
           int elementId = 0;
           while (rs.next()) {

               String oldElmId = rs.getString("DATAELEM_ID");
               String newElmId = oldNewElements.get(oldElmId);
               if (!Util.isEmpty(newElmId)) {

                   int vocabularyFolderId = rs.getInt("VOCABULARY_ID");
                   elementId = rs.getInt("ve.DATAELEM_ID");

                   if (elementId != prevElemId) {
                       stmtInsertBinding.setInt(1, Integer.valueOf(newElmId));
                       stmtInsertBinding.setInt(2, vocabularyFolderId);
                       stmtInsertBinding.execute();
                   }
                   Integer conceptId = rs.getInt("VOCABULARY_CONCEPT_ID");

                   if (conceptId != null) {
                       String value = rs.getString("ELEMENT_VALUE");
                       String language = StringUtils.trimToNull(rs.getString("LANGUAGE"));
                       Integer relatedConceptId = (rs.getInt("RELATED_CONCEPT_ID") == 0 ? null : rs.getInt("RELATED_CONCEPT_ID"));
                       String linkText = rs.getString("LINK_TEXT");

                       stmtInsertValue.setInt(1, Integer.valueOf(newElmId));
                       stmtInsertValue.setInt(2, conceptId);
                       stmtInsertValue.setString(3, value);
                       stmtInsertValue.setString(4, language);
                       if (relatedConceptId != null) {
                           stmtInsertValue.setInt(5, relatedConceptId);
                       } else {
                           stmtInsertValue.setNull(5, Types.INTEGER);
                       }
                       stmtInsertValue.setString(6, linkText);

                       stmtInsertValue.execute();
                   }

               }

               prevElemId = elementId;
               }
           SQL.close(rs);

       } finally {
           SQL.close(rs);
           SQL.close(stmt);

           SQL.close(stmtInsertBinding);
           SQL.close(stmtInsertValue);

       }
   }

    /**
     *
     * @throws SQLException if database access fails
     */
    private void copyDocuments() throws SQLException {

        LOGGER.debug("Copying all document references ...");

        if (oldNewDatasets.isEmpty() && oldNewTables.isEmpty()) {
            return;
        }

        String possibleOR = "";
        String selectSQL = "select * from DOC where";
        if (!oldNewDatasets.isEmpty()) {
            selectSQL = selectSQL + " (OWNER_TYPE='dst' and OWNER_ID in (" + Util.toCSV(oldNewDatasets.keySet()) + "))";
            possibleOR = " or";
        }
        if (!oldNewTables.isEmpty()) {
            selectSQL = selectSQL + possibleOR + " (OWNER_TYPE='tbl' and OWNER_ID in (" + Util.toCSV(oldNewTables.keySet()) + "))";
            possibleOR = " or";
        }

        String insertSQL = "insert into DOC (OWNER_ID,OWNER_TYPE,MD5_PATH,ABS_PATH,TITLE) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {

                String oldId = rs.getString("OWNER_ID");
                String ownerType = rs.getString("OWNER_TYPE");
                String newId = null;
                if (ownerType != null) {
                    if (ownerType.equals("dst")) {
                        newId = oldNewDatasets.get(oldId);
                    } else if (ownerType.equals("tbl")) {
                        newId = oldNewTables.get(oldId);
                    }
                }

                if (newId != null) {

                    String md5Path = rs.getString("MD5_PATH");
                    String absPath = rs.getString("ABS_PATH");
                    String title = rs.getString("TITLE");

                    if (insertSQL.length() > insertSQLLengthBefore) {
                        insertSQL = insertSQL + ",";
                    }
                    insertSQL =
                        insertSQL + "(" + newId + "," + SQL.toLiteral(ownerType) + "," + SQL.toLiteral(md5Path) + ","
                        + SQL.toLiteral(absPath) + "," + SQL.toLiteral(title) + ")";
                }

            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     *
     * @throws SQLException if database access fails
     */
    private void copyDstToTblRelations() throws SQLException {

        LOGGER.debug("Copying all dataset-to-table relations ...");

        if (oldNewDatasets.isEmpty()) {
            return;
        }

        String selectSQL = "select * from DST2TBL where DATASET_ID in (" + Util.toCSV(oldNewDatasets.keySet()) + ")";
        String insertSQL = "insert into DST2TBL (DATASET_ID,TABLE_ID,POSITION) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {

                String oldDstId = rs.getString("DATASET_ID");
                String oldTblId = rs.getString("TABLE_ID");
                String position = rs.getString("POSITION");

                String newDstId = oldNewDatasets.get(oldDstId);
                if (!Util.isEmpty(newDstId)) {

                    String newTblId = oldNewTables.get(oldTblId);
                    if (!Util.isEmpty(newTblId)) {

                        if (insertSQL.length() > insertSQLLengthBefore) {
                            insertSQL = insertSQL + ",";
                        }
                        insertSQL = insertSQL + "(" + newDstId + "," + newTblId + "," + position + ")";
                    }
                }
            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     * @throws SQLException if database access fails
     *
     */
    private void copyTblToElmRelations() throws SQLException {

        LOGGER.debug("Copying all table-to-element relations ...");

        if (oldNewTables.isEmpty()) {
            return;
        }

        String selectSQL = "select * from TBL2ELEM where TABLE_ID in (" + Util.toCSV(oldNewTables.keySet()) + ")";
        String insertSQL = "insert into TBL2ELEM (TABLE_ID,DATAELEM_ID,POSITION,MULTIVAL_DELIM,MANDATORY,PRIM_KEY) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);

            while (rs.next()) {

                String oldTblId = rs.getString("TABLE_ID");
                String oldElmId = rs.getString("DATAELEM_ID");
                String position = rs.getString("POSITION");
                String multiValueDelim = rs.getString("MULTIVAL_DELIM");
                String mandatory = rs.getString("MANDATORY");
                String primKey = rs.getString("PRIM_KEY");

                String newTblId = oldNewTables.get(oldTblId);
                if (!Util.isEmpty(newTblId)) {

                    String newElmId = oldNewElements.get(oldElmId);
                    if (Util.isEmpty(newElmId)) {
                        // if no new element if for the old one found, assume the old one is a common elements,
                        // in which case we copy the relation with the old id, because common elements were not copied
                        newElmId = oldElmId;
                    }

                    String nullSafeMultiValueDelim = multiValueDelim == null ? "NULL" : SQL.toLiteral(multiValueDelim);
                    if (insertSQL.length() > insertSQLLengthBefore) {
                        insertSQL = insertSQL + ",";
                    }
                    insertSQL =
                        insertSQL + "(" + newTblId + "," + newElmId + "," + position + "," + nullSafeMultiValueDelim + ","
                        + mandatory + "," + primKey + ")";
                }
            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }

    }

    /**
     *
     * @throws SQLException if database access fails
     */
    private void copyElmToTblRelations() throws SQLException {

        LOGGER.debug("Copying element-to-table relations ...");

        if (oldNewElements.isEmpty()) {
            return;
        }

        String selectSQL = "select * from TBL2ELEM where DATAELEM_ID in (" + Util.toCSV(oldNewElements.keySet()) + ")";
        String insertSQL = "insert into TBL2ELEM (TABLE_ID,DATAELEM_ID,POSITION,MULTIVAL_DELIM,MANDATORY,PRIM_KEY) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);

            while (rs.next()) {

                String oldElmId = rs.getString("DATAELEM_ID");
                String newElmId = oldNewElements.get(oldElmId);
                if (newElmId != null) {

                    String tblId = rs.getString("TABLE_ID");
                    String position = rs.getString("POSITION");
                    String multiValueDelim = nullSafeLiteral(rs.getString("MULTIVAL_DELIM"));
                    String mandatory = rs.getString("MANDATORY");
                    String primKey = rs.getString("PRIM_KEY");

                    if (insertSQL.length() > insertSQLLengthBefore) {
                        insertSQL = insertSQL + ",";
                    }
                    insertSQL =
                        insertSQL + "(" + tblId + "," + newElmId + "," + position + "," + multiValueDelim + "," + mandatory
                        + "," + primKey + ")";
                }
            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }

    }

    /**
     *
     * @throws SQLException if database access fails
     */
    private void copyFixedValues() throws SQLException {

        LOGGER.debug("Copying all fixed values ...");

        if (oldNewElements.isEmpty()) {
            return;
        }

        String selectSQL =
            "select * from FXV where OWNER_TYPE='elem' and OWNER_ID in (" + Util.toCSV(oldNewElements.keySet()) + ")";
        String insertSQL = "insert into FXV (OWNER_ID,OWNER_TYPE,VALUE,IS_DEFAULT,DEFINITION,SHORT_DESC) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {

                String oldElmId = rs.getString("OWNER_ID");
                String newElmId = oldNewElements.get(oldElmId);
                if (!Util.isEmpty(newElmId)) {

                    String value = nullSafeLiteral(rs.getString("VALUE"));
                    String isDefault = nullSafeLiteral(rs.getString("IS_DEFAULT"));
                    String definition = nullSafeLiteral(rs.getString("DEFINITION"));
                    String shortDesc = nullSafeLiteral(rs.getString("SHORT_DESC"));

                    if (insertSQL.length() > insertSQLLengthBefore) {
                        insertSQL = insertSQL + ",";
                    }
                    insertSQL =
                        insertSQL + "(" + newElmId + ",'elem'," + value + "," + isDefault + "," + definition + "," + shortDesc
                        + ")";
                }
            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     *
     * @throws SQLException if database access fails
     */
    private void copyFkRelations() throws SQLException {

        LOGGER.debug("Copying all foreign-key relations ...");

        if (oldNewElements.isEmpty()) {
            return;
        }

        String csv = Util.toCSV(oldNewElements.keySet());
        String selectSQL = "select * from FK_RELATION where A_ID in (" + csv + ") or B_ID in (" + csv + ")";
        String insertSQL = "insert into FK_RELATION (A_ID,B_ID,A_CARDIN,B_CARDIN,DEFINITION) values ";
        int insertSQLLengthBefore = insertSQL.length();

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {

                String oldAId = rs.getString("A_ID");
                String newAId = oldNewElements.get(oldAId);
                if (newAId == null) {
                    newAId = oldAId;
                }
                String oldBId = rs.getString("B_ID");
                String newBId = oldNewElements.get(oldBId);
                if (newBId == null) {
                    newBId = oldBId;
                }

                String aCardinality = nullSafeLiteral(rs.getString("A_CARDIN"));
                String bCardinality = nullSafeLiteral(rs.getString("B_CARDIN"));
                String definition = nullSafeLiteral(rs.getString("DEFINITION"));

                if (insertSQL.length() > insertSQLLengthBefore) {
                    insertSQL = insertSQL + ",";
                }
                insertSQL =
                    insertSQL + "(" + newAId + "," + newBId + "," + aCardinality + "," + bCardinality + "," + definition + ")";
            }
            SQL.close(rs);

            if (insertSQL.length() > insertSQLLengthBefore) {
                stmt.executeUpdate(insertSQL);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    private void copyInferenceRules() throws SQLException {
        for (String oldElementId : this.oldNewElements.keySet()) {
            String newElementId = this.oldNewElements.get(oldElementId);
            this.copyInferenceRules(oldElementId, newElementId);
            this.copyInvertedInferenceRules(oldElementId, newElementId);
        }
    }

    private void copyInferenceRules(String oldElementId, String newElementId) throws SQLException {
        String sql = "insert into INFERENCE_RULE (DATAELEM_ID, RULE, TARGET_ELEM_ID) select ?, RULE, TARGET_ELEM_ID from INFERENCE_RULE where DATAELEM_ID = ?";
        PreparedStatement stmt = null;
        
        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.valueOf(newElementId));
            stmt.setInt(2, Integer.valueOf(oldElementId));
            stmt.executeUpdate();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private void copyInvertedInferenceRules(String oldElementId, String newElementId) throws SQLException {
        String sql = "insert into INFERENCE_RULE (DATAELEM_ID, RULE, TARGET_ELEM_ID) select DATAELEM_ID, RULE, ? from INFERENCE_RULE where RULE='owl:inverseOf' and TARGET_ELEM_ID = ?";
        PreparedStatement stmt = null;
        
        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.valueOf(newElementId));
            stmt.setInt(2, Integer.valueOf(oldElementId));
            stmt.executeUpdate();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     *
     * @param stmt
     * @return
     * @throws SQLException if database access fails
     */
    private String getLastInsertID(Statement stmt) throws SQLException {

        ResultSet rs = null;
        boolean statementGiven = stmt != null;
        try {
            if (!statementGiven) {
                stmt = conn.createStatement();
            }

            rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
            rs.clearWarnings();
            return rs.next() ? rs.getString(1) : null;
        } finally {
            if (!statementGiven) {
                SQL.close(stmt);
            }
        }
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    private static Map<String, Object> toValueMap(String key, Object value) {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }

    /**
     *
     * @param s
     * @return
     */
    private String nullSafeLiteral(String s) {
        return s == null ? "NULL" : SQL.toLiteral(s);
    }
}
