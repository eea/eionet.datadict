package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

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
public class AlternativeCopyHandler extends OldCopyHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(AlternativeCopyHandler.class);

    /**  */
    private DDUser user = null;

    /** */
    private HashMap<String, String> oldNewElements = new HashMap<String, String>();
    private HashMap<String, String> oldNewTables = new HashMap<String, String>();
    private HashMap<String, String> oldNewDatasets = new HashMap<String, String>();

    /** */
    private HashMap<String, Set<String>> dst2Tables = new HashMap<String, Set<String>>();
    private HashMap<String, Set<String>> tbl2Elements = new HashMap<String, Set<String>>();

    /**
     *
     * @param conn
     * @param ctx
     * @param searchEngine
     */
    public AlternativeCopyHandler(Connection conn, ServletContext ctx, DDSearchEngine searchEngine) {
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

        oldNewDatasets.put(dstId, null);

        // Find all tables and elements in this datasets, that require copying (only non-common elements require copying).
        findDatasetTablesAndElementsToCopy(dstId);

        // create new dataset
        String newDstId = createNewDataset(dstId, makeItWorkingCopy, resetVersionAndStatus);

        // create new tables
        for (Entry<String, String> entry : oldNewTables.entrySet()) {
            entry.setValue(createNewTable(entry.getKey()));
        }

        // create new elements
        for (Entry<String, String> entry : oldNewElements.entrySet()) {
            entry.setValue(createNewElement(entry.getKey(), false, false, false));
        }

        // Copy all the dependencies for the newly created datasets/tables/elements.
        copyDependencies(false);

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
    public String copyElm(String elmId, boolean makeItWorkingCopy, boolean isCopyTbl2ElmRelations, boolean resetVersionAndStatus) throws Exception {

        // If no element id provided, return without further actions
        if (!Util.isEmpty(elmId)) {
            return null;
        }

        // Create new element.
        String newId = createNewElement(elmId, makeItWorkingCopy, isCopyTbl2ElmRelations, resetVersionAndStatus);

        // If new element successfully created and its ID available, copy attributes, fixed values, etc.
        if (newId != null) {

            // Element-to-table relations will also be copied if required by the method input.
            copyDependencies(isCopyTbl2ElmRelations);
        }
        return newId;
    }

    /**
     *
     * @param tableName
     * @param whereClause
     * @param autoIncColumn
     * @return
     * @throws SQLException
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
     * @throws SQLException
     */
    protected int copyAutoIncRow(String tableName, String whereClause,
            String autoIncColumn, final Map<String, Object> newValuesMap) throws SQLException {

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
     *
     * @param oldId
     * @param newId
     * @param parentType
     * @return
     */
    protected static String simpleAttrsCopyStatement(String oldId, String newId, String parentType) {

        Map<String, Object> newValues = toValueMap("DATAELEM_ID", newId);
        String whereClause = "PARENT_TYPE='" + parentType + "' and DATAELEM_ID=" + oldId;
        return rowsCopyStatement("ATTRIBUTE", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @param parentType
     * @return
     */
    protected static String complexAttrRowsCopyStatement(String oldId, String newId, String parentType) {

        Map<String, Object> newValues = toValueMap("PARENT_ID", newId);
        newValues.put("ROW_ID", "md5(concat('" + newId + "',PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION))");
        String whereClause = "PARENT_TYPE='" + parentType + "' and PARENT_ID=" + oldId;
        return rowsCopyStatement("COMPLEX_ATTR_ROW", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @param parentType
     * @return
     */
    protected static String complexAttrFieldsCopyStatement(String oldId, String newId, String parentType) {

        Map<String, Object> newValues =
            toValueMap("ROW_ID", "md5(concat('" + newId + "',PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION))");
        String fromClause = "COMPLEX_ATTR_FIELD,COMPLEX_ATTR_ROW";
        String whereClause =
            "COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID and PARENT_TYPE='" + parentType + "' and PARENT_ID=" + oldId;
        return rowsCopyStatement("COMPLEX_ATTR_FIELD", fromClause, whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @param ownerType
     * @return
     */
    protected static String documentsCopyStatement(String oldId, String newId, String ownerType) {

        Map<String, Object> newValues = toValueMap("OWNER_ID", newId);
        String whereClause = "OWNER_TYPE='" + ownerType + "' and OWNER_ID=" + oldId;
        return rowsCopyStatement("DOC", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @param ownerType
     * @return
     */
    protected static String rodLinksCopyStatement(String oldId, String newId) {

        Map<String, Object> newValues = toValueMap("DATASET_ID", newId);
        String whereClause = "DATASET_ID=" + oldId;
        return rowsCopyStatement("DST2ROD", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @return
     */
    protected static String fixedValuesCopyStatement(String oldId, String newId) {

        Map<String, Object> newValues = toValueMap("OWNER_ID", newId);
        newValues.put("FXV_ID", null);
        String whereClause = "OWNER_TYPE='elem' and OWNER_ID=" + oldId;
        return rowsCopyStatement("FXV", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @return
     */
    protected static String fkRelationsCopyStatementA(String oldId, String newId) {

        Map<String, Object> newValues = toValueMap("A_ID", newId);
        newValues.put("REL_ID", null);
        String whereClause = "A_ID=" + oldId;
        return rowsCopyStatement("FK_RELATION", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @return
     */
    protected static String fkRelationsCopyStatementB(String oldId, String newId) {

        Map<String, Object> newValues = toValueMap("B_ID", newId);
        newValues.put("REL_ID", null);
        String whereClause = "B_ID=" + oldId;
        return rowsCopyStatement("FK_RELATION", whereClause, newValues);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @return
     */
    protected static String elmToTblRelationsCopyStatement(String oldId, String newId) {

        Map<String, Object> newValues = toValueMap("DATAELEM_ID", newId);
        String whereClause = "DATAELEM_ID=" + oldId;
        return rowsCopyStatement("TBL2ELEM", whereClause, newValues);
    }

    /**
     *
     * @param oldDstId
     * @param newDstId
     * @param oldTblId
     * @param newTblId
     * @return
     */
    protected static String dstToTblRelationsCopyStatement(String oldDstId, String newDstId, String oldTblId, String newTblId) {

        Map<String, Object> newValues = toValueMap("DATASET_ID", newDstId);
        newValues.put("TABLE_ID", newTblId);
        String whereClause = "DATASET_ID=" + oldDstId + " and TABLE_ID=" + oldTblId;
        return rowsCopyStatement("DST2TBL", whereClause, newValues);
    }

    /**
     *
     * @param oldTblId
     * @param newTblId
     * @param oldElmId
     * @param newElmId
     * @return
     */
    protected static String tblToElmRelationsCopyStatement(String oldTblId, String newTblId, String oldElmId, String newElmId) {

        Map<String, Object> newValues = toValueMap("TABLE_ID", newTblId);
        newValues.put("DATAELEM_ID", newElmId);
        String whereClause = "TABLE_ID=" + oldTblId + " and DATAELEM_ID=" + oldElmId;
        return rowsCopyStatement("TBL2ELEM", whereClause, newValues);
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
     * @param dstId
     * @throws SQLException
     */
    private void findDatasetTablesAndElementsToCopy(String dstId) throws SQLException {

        String sql = "select TABLE_ID from DST2TBL where DATASET_ID=?";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(dstId));

            LOGGER.debug("Getting ids of tables to copy ...");

            rs = pstmt.executeQuery();
            while (rs.next()) {
                oldNewTables.put(rs.getString(1), null);
            }
            SQL.close(rs);

            LOGGER.debug(oldNewTables.size() + " tables found");

            if (!oldNewTables.isEmpty()) {

                dst2Tables.put(dstId, oldNewTables.keySet());

                LOGGER.debug("Getting ids of elements to copy ...");

                sql = "select distinct TBL2ELEM.TABLE_ID,TBL2ELEM.DATAELEM_ID,DATAELEM.PARENT_NS from TBL2ELEM,DATAELEM "
                    + "where TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID and TABLE_ID in ("
                    + Util.toCSV(oldNewTables.keySet()) + ")";
                rs = pstmt.executeQuery(sql);
                while (rs.next()) {

                    String elmId = rs.getString("DATAELEM_ID");
                    String parentNs = rs.getString("PARENT_NS");
                    if (parentNs != null) {
                        oldNewElements.put(elmId, null);
                    }

                    String tblId = rs.getString("TABLE_ID");
                    Set<String> tblElms = tbl2Elements.get(tblId);
                    if (tblElms == null) {
                        tblElms = new HashSet<String>();
                        tbl2Elements.put(tblId, tblElms);
                    }
                    tblElms.add(elmId);
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
     * @throws SQLException
     */
    private String createNewDataset(String dstId, boolean makeItWorkingCopy, boolean resetVersionAndStatus) throws SQLException{

        LOGGER.debug("Copying dataset row");

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
        if (newId != null) {
            oldNewDatasets.put(dstId, newId);
        }
        return newId;
    }

    /**
     *
     * @param tblId
     * @throws SQLException
     */
    private String createNewTable(String tblId) throws SQLException{

        LOGGER.debug("Copying table row");
        Map<String, Object> newValues = toValueMap("DATE", Long.valueOf(System.currentTimeMillis()));
        if (user != null) {
            newValues.put("USER", SQL.toLiteral(user.getUserName()));
        }

        String newId = String.valueOf(copyAutoIncRow("DS_TABLE", "TABLE_ID=" + tblId, "TABLE_ID", newValues));
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
     * @throws SQLException
     */
    private String createNewElement(String elmId, boolean makeItWorkingCopy, boolean isCopyTbl2ElmRelations, boolean resetVersionAndStatus) throws SQLException{

        LOGGER.debug("Copying element row");
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
        if (newId != null) {
            oldNewElements.put(elmId, newId);
        }
        return newId;
    }

    /**
     * @param isCopyTbl2ElmRelations
     * @throws SQLException
     *
     */
    private void copyDependencies(boolean isCopyTbl2ElmRelations) throws SQLException{

        LOGGER.debug("Going to run various copy statements ...");

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            boolean isEmptyBatch = true;

            for (Entry<String, String> entry : oldNewDatasets.entrySet()) {

                stmt.addBatch(simpleAttrsCopyStatement(entry.getKey(), entry.getValue(), "DS"));
                isEmptyBatch = false;
                stmt.addBatch(complexAttrRowsCopyStatement(entry.getKey(), entry.getValue(), "DS"));
                stmt.addBatch(complexAttrFieldsCopyStatement(entry.getKey(), entry.getValue(), "DS"));
                stmt.addBatch(documentsCopyStatement(entry.getKey(), entry.getValue(), "dst"));
                stmt.addBatch(rodLinksCopyStatement(entry.getKey(), entry.getValue()));
            }

            for (Entry<String, String> entry : oldNewTables.entrySet()) {

                stmt.addBatch(simpleAttrsCopyStatement(entry.getKey(), entry.getValue(), "T"));
                isEmptyBatch = false;
                stmt.addBatch(complexAttrRowsCopyStatement(entry.getKey(), entry.getValue(), "T"));
                stmt.addBatch(complexAttrFieldsCopyStatement(entry.getKey(), entry.getValue(), "T"));
                stmt.addBatch(documentsCopyStatement(entry.getKey(), entry.getValue(), "tbl"));
            }

            for (Entry<String, String> entry : oldNewElements.entrySet()) {

                stmt.addBatch(simpleAttrsCopyStatement(entry.getKey(), entry.getValue(), "E"));
                isEmptyBatch = false;
                stmt.addBatch(complexAttrRowsCopyStatement(entry.getKey(), entry.getValue(), "E"));
                stmt.addBatch(complexAttrFieldsCopyStatement(entry.getKey(), entry.getValue(), "E"));
                stmt.addBatch(fixedValuesCopyStatement(entry.getKey(), entry.getValue()));
                stmt.addBatch(fkRelationsCopyStatementA(entry.getKey(), entry.getValue()));
                stmt.addBatch(fkRelationsCopyStatementB(entry.getKey(), entry.getValue()));
                if (isCopyTbl2ElmRelations) {
                    stmt.addBatch(elmToTblRelationsCopyStatement(entry.getKey(), entry.getValue()));
                }

            }

            for (Entry<String, Set<String>> entry : dst2Tables.entrySet()) {

                String oldDstId = entry.getKey();
                String newDstId = oldNewDatasets.get(oldDstId);
                for (String oldTblId : entry.getValue()) {
                    String newTblId = oldNewTables.get(oldTblId);
                    stmt.addBatch(dstToTblRelationsCopyStatement(oldDstId, newDstId, oldTblId, newTblId));
                    isEmptyBatch = false;
                }
            }

            for (Entry<String, Set<String>> entry : tbl2Elements.entrySet()) {

                String oldTblId = entry.getKey();
                String newTblId = oldNewTables.get(oldTblId);
                for (String oldElmId : entry.getValue()) {
                    String newElmId = oldNewElements.get(oldElmId);
                    if (newElmId == null) {
                        newElmId = oldElmId;
                    }
                    stmt.addBatch(tblToElmRelationsCopyStatement(oldTblId, newTblId, oldElmId, newElmId));
                    isEmptyBatch = false;
                }
            }

            if (isEmptyBatch == false) {
                LOGGER.debug("Executing mega-batch ...");
                stmt.executeBatch();
            }
        } finally {
            SQL.close(stmt);
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
     * @param stmt
     * @return
     * @throws SQLException
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
}
