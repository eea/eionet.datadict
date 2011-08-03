package eionet.meta.savers;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.dbunit.DatabaseTestCase;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.ColumnFilterTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.test.util.ColumnSpecificReplacementTable;
import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class CopyHandlerTest extends DatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {

        return new DatabaseConnection(ConnectionUtil.getConnection());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {

        return new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream("seed-copyhandler.xml"));
    }

    /**
     * @throws Exception
     * @throws SQLException
     *
     */
    @Test
    public void testCopyAutoIncRow() throws SQLException, Exception{

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where SHORT_NAME='dataset1' order by DATASET_ID");
        ITable table = queryDataSet.getTable("DATASET");

        assertEquals(1, table.getRowCount());

        CopyHandler copyHandler = new CopyHandler(getConnection().getConnection(), null, null);
        int newId = copyHandler.copyAutoIncRow("DATASET", "SHORT_NAME='dataset1'", "DATASET_ID");

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where SHORT_NAME='dataset1'");
        table = queryDataSet.getTable("DATASET");

        assertEquals(2, table.getRowCount());

        int dstId1 = Integer.parseInt(table.getValue(0, "DATASET_ID").toString());
        int dstId2 = Integer.parseInt(table.getValue(1, "DATASET_ID").toString());
        assertTrue(dstId1 != dstId2);
        assertTrue(newId == dstId2);
    }

    /**
     *
     * @throws SQLException
     * @throws Exception
     */
    public void testCopyDst() throws SQLException, Exception {

        CopyHandler copyHandler = new CopyHandler(getConnection().getConnection(), null, null);
        copyHandler.setRecordOldNewMappings(true);

        String oldDstId = "111";
        String newDstId = copyHandler.copyDst(oldDstId, false, false);

        compareDefinitionRows("DATASET", "DATASET_ID", oldDstId, newDstId);
        compareSimpleAttributes("DS", oldDstId, newDstId);
        compareComplexAttributes("DS", oldDstId, newDstId);
        compareDocs("dst", oldDstId, newDstId);
        compareDst2Rod(oldDstId, newDstId);

        Hashtable<String, String> oldNewTables = copyHandler.getOldNewTables();
        assertEquals(2, oldNewTables.size());
        compareDst2Tbl(oldDstId, newDstId, oldNewTables);

        Hashtable<String, String> oldNewElements = copyHandler.getOldNewElements();
        // Although there is 4 elements in the loaded XML dataset, the copy handler will copy only non-common elements,
        // of which there is 2. So we expect no more than 4-2=2 entries in old-new element mappings recorded.
        assertEquals(2, oldNewElements.size());

        for (Entry<String, String> entry : oldNewTables.entrySet()) {

            String oldTblId = entry.getKey();
            String newTblId = entry.getValue();

            compareDefinitionRows("DS_TABLE", "TABLE_ID", oldTblId, newTblId);
            compareSimpleAttributes("T", oldTblId, newTblId);
            compareComplexAttributes("T", oldTblId, newTblId);
            compareDocs("tbl", oldTblId, newTblId);
            compareTbl2Elm(oldTblId, newTblId, oldNewElements);
        }

        for (Entry<String, String> entry : oldNewElements.entrySet()) {

            String oldElmId = entry.getKey();
            String newElmId = entry.getValue();

            compareDefinitionRows("DATAELEM", "DATAELEM_ID", oldElmId, newElmId);
            compareSimpleAttributes("E", oldElmId, newElmId);
            compareComplexAttributes("E", oldElmId, newElmId);
            compareFixedValues(oldElmId, newElmId);
        }

    }

    /**
     *
     * @param tableName
     * @param idColumn
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareDefinitionRows(String tableName, String idColumn, String oldId, String newId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from " + tableName + " where " + idColumn + "=" + oldId);
        queryDataSet.addTable("NEW", "select * from " + tableName + " where " + idColumn + "=" + newId);

        ColumnFilterImpl colFilter = new ColumnFilterImpl(idColumn, "DATE", "VERSION");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());

        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param parentType
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareSimpleAttributes(String parentType, String oldId, String newId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from ATTRIBUTE where DATAELEM_ID=" + oldId + " and PARENT_TYPE='" + parentType
                + "'");
        queryDataSet.addTable("NEW", "select * from ATTRIBUTE where DATAELEM_ID=" + newId + " and PARENT_TYPE='" + parentType
                + "'");

        ColumnFilterImpl colFilter = new ColumnFilterImpl("DATAELEM_ID");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());

        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param parentType
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareComplexAttributes(String parentType, String oldId, String newId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD_ROWS", "select * from COMPLEX_ATTR_ROW where PARENT_ID=" + oldId + " and PARENT_TYPE='"
                + parentType + "'");
        queryDataSet.addTable("NEW_ROWS", "select * from COMPLEX_ATTR_ROW where PARENT_ID=" + newId + " and PARENT_TYPE='"
                + parentType + "'");
        queryDataSet.addTable("OLD_FIELDS", "select COMPLEX_ATTR_FIELD.* from COMPLEX_ATTR_FIELD,COMPLEX_ATTR_ROW "
                + "where COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID and PARENT_ID=" + oldId + " and PARENT_TYPE='"
                + parentType + "'");
        queryDataSet.addTable("NEW_FIELDS", "select COMPLEX_ATTR_FIELD.* from COMPLEX_ATTR_FIELD,COMPLEX_ATTR_ROW "
                + "where COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID and PARENT_ID=" + newId + " and PARENT_TYPE='"
                + parentType + "'");

        ColumnFilterImpl colFilter = new ColumnFilterImpl("PARENT_ID", "ROW_ID");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD_ROWS"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW_ROWS"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());

        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);

        tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD_FIELDS"), colFilter));
        tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW_FIELDS"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param parentType
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareDocs(String parentType, String oldId, String newId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from DOC where OWNER_ID=" + oldId + " and OWNER_TYPE='" + parentType + "'");
        queryDataSet.addTable("NEW", "select * from DOC where OWNER_ID=" + newId + " and OWNER_TYPE='" + parentType + "'");

        ColumnFilterImpl colFilter = new ColumnFilterImpl("OWNER_ID");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());

        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareDst2Rod(String oldId, String newId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from DST2ROD where DATASET_ID=" + oldId);
        queryDataSet.addTable("NEW", "select * from DST2ROD where DATASET_ID=" + newId);

        ColumnFilterImpl colFilter = new ColumnFilterImpl("DATASET_ID");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());

        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareFixedValues(String oldId, String newId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from FXV where OWNER_TYPE='elem' and OWNER_ID=" + oldId);
        queryDataSet.addTable("NEW", "select * from FXV where OWNER_TYPE='elem' and OWNER_ID=" + newId);

        ColumnFilterImpl colFilter = new ColumnFilterImpl("OWNER_ID", "FXV_ID");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter));

        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());

        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param oldDstId
     * @param newDstId
     * @throws Exception
     */
    private void compareDst2Tbl(String oldDstId, String newDstId, Hashtable<String, String> oldNewTables) throws Exception {

        if (oldNewTables==null || oldNewTables.isEmpty()){
            return;
        }

        // get the tables from database
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from DST2TBL where DATASET_ID=" + oldDstId);
        queryDataSet.addTable("NEW", "select * from DST2TBL where DATASET_ID=" + newDstId);

        // process the "old" table
        ColumnFilterImpl colFilter = new ColumnFilterImpl("DATASET_ID");
        ITable filteredTable = new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter);
        ColumnSpecificReplacementTable replacementTable = new ColumnSpecificReplacementTable(filteredTable, "TABLE_ID");
        for (Entry<String, String> entry : oldNewTables.entrySet()){

            String oldTblId = entry.getKey();
            String newTblId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldTblId), Integer.valueOf(newTblId));
        }
        ITable tableOld = new SortedTable(replacementTable);

        // process the "new" table
        filteredTable = new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter);
        replacementTable = new ColumnSpecificReplacementTable(filteredTable, "TABLE_ID");
        for (Entry<String, String> entry : oldNewTables.entrySet()){

            String oldTblId = entry.getKey();
            String newTblId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldTblId), Integer.valueOf(newTblId));
        }
        ITable tableNew = new SortedTable(replacementTable);

        // finally, compare the two resulting tables
        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());
        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param oldTblId
     * @param newTblId
     * @param oldNewElements
     * @throws Exception
     */
    private void compareTbl2Elm(String oldTblId, String newTblId, Hashtable<String, String> oldNewElements) throws Exception {

        if (oldNewElements==null || oldNewElements.isEmpty()){
            return;
        }

        // get the tables from database
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from TBL2ELEM where TABLE_ID=" + oldTblId);
        queryDataSet.addTable("NEW", "select * from TBL2ELEM where TABLE_ID=" + newTblId);

        // process the "old" table
        ColumnFilterImpl colFilter = new ColumnFilterImpl("TABLE_ID");
        ITable filteredTable = new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter);
        ColumnSpecificReplacementTable replacementTable = new ColumnSpecificReplacementTable(filteredTable, "DATAELEM_ID");
        for (Entry<String, String> entry : oldNewElements.entrySet()){

            String oldElmId = entry.getKey();
            String newElmId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldElmId), Integer.valueOf(newElmId));
        }
        ITable tableOld = new SortedTable(replacementTable);

        // process the "new" table
        colFilter = new ColumnFilterImpl("TABLE_ID");
        filteredTable = new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter);
        replacementTable = new ColumnSpecificReplacementTable(filteredTable, "DATAELEM_ID");
        for (Entry<String, String> entry : oldNewElements.entrySet()){

            String oldElmId = entry.getKey();
            String newElmId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldElmId), Integer.valueOf(newElmId));
        }
        ITable tableNew = new SortedTable(replacementTable);

        // finally, compare the two resulting tables
        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());
        DbUnitAssert dbUnitAssert = new DbUnitAssert();
        dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @author Jaanus Heinlaid
     *
     */
    static class ColumnFilterImpl implements IColumnFilter {

        /** */
        HashSet<String> skipColumns = new HashSet<String>();

        /**
         *
         * @param skipColumns
         */
        private ColumnFilterImpl(String... skipColumns) {

            if (skipColumns != null && skipColumns.length > 0) {
                for (int i = 0; i < skipColumns.length; i++) {
                    this.skipColumns.add(skipColumns[i]);
                }
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.dbunit.dataset.filter.IColumnFilter#accept(java.lang.String, org.dbunit.dataset.Column)
         */
        @Override
        public boolean accept(String tableName, Column column) {

            if (skipColumns.contains(column.getColumnName())) {
                return false;
            } else {
                return true;
            }
        }

    }
}
