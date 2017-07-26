package eionet.meta.savers;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.ColumnFilterTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.IColumnFilter;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.test.util.ColumnSpecificReplacementTable;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class CopyHandlerTest extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-copyhandler.xml";
    }

    /**
     * @throws Exception
     * @throws SQLException
     *
     */
    @Test
    public void testCopyAutoIncRow() throws SQLException, Exception {

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

        String oldDstId = "111";
        String newDstId = copyHandler.copyDst(oldDstId, false, false);

        compareDefinitionRows("DATASET", "DATASET_ID", oldDstId, newDstId);
        compareSimpleAttributes("DS", oldDstId, newDstId);
        compareDocs("dst", oldDstId, newDstId);

        Map<String, String> oldNewTables = copyHandler.getOldNewTables();
        assertEquals(2, oldNewTables.size());
        compareDst2Tbl(oldDstId, newDstId, oldNewTables);

        Map<String, String> oldNewElements = copyHandler.getOldNewElements();
        // Although there is 4 elements in the loaded XML dataset, the copy handler will copy only non-common elements,
        // of which there is 2. So we expect no more than 4-2=2 entries in old-new element mappings recorded.
        assertEquals(2, oldNewElements.size());

        for (Entry<String, String> entry : oldNewTables.entrySet()) {

            String oldTblId = entry.getKey();
            String newTblId = entry.getValue();

            compareDefinitionRows("DS_TABLE", "TABLE_ID", oldTblId, newTblId);
            compareSimpleAttributes("T", oldTblId, newTblId);
            compareDocs("tbl", oldTblId, newTblId);
            compareTbl2Elm(oldTblId, newTblId, oldNewElements);
        }

        for (Entry<String, String> entry : oldNewElements.entrySet()) {

            String oldElmId = entry.getKey();
            String newElmId = entry.getValue();

            compareDefinitionRows("DATAELEM", "DATAELEM_ID", oldElmId, newElmId);
            compareSimpleAttributes("E", oldElmId, newElmId);
            compareFixedValues(oldElmId, newElmId);
            compareFkRelations(oldElmId, newElmId, oldNewElements);
        }

    }

    /**
     *
     * @throws SQLException
     * @throws Exception
     */
    public void testCopyElm() throws SQLException, Exception {

        CopyHandler copyHandler = new CopyHandler(getConnection().getConnection(), null, null);

        String oldElmId = "777";
        String newElmId = copyHandler.copyElm(oldElmId, false, true, false);

        assertTrue(newElmId != null);
        assertTrue(oldElmId != newElmId);

        Map<String, String> oldNewElements = copyHandler.getOldNewElements();
        assertEquals(1, oldNewElements.size());

        compareDefinitionRows("DATAELEM", "DATAELEM_ID", oldElmId, newElmId);
        compareSimpleAttributes("DS", oldElmId, newElmId);
        compareFixedValues(oldElmId, newElmId);
        compareFkRelations(oldElmId, newElmId, oldNewElements);
        compareElmToTblRelations(oldElmId, newElmId);
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
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareFkRelations(String oldElmId, String newElmId, Map<String, String> oldNewElements) throws Exception {

        if (oldNewElements == null || oldNewElements.isEmpty()) {
            return;
        }

        // get the tables from database
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from FK_RELATION where A_ID=" + oldElmId + " or B_ID=" + oldElmId);
        queryDataSet.addTable("NEW", "select * from FK_RELATION where A_ID=" + newElmId + " or B_ID=" + newElmId);

        // process the "old" table
        ITable filteredTable = new ColumnFilterTable(queryDataSet.getTable("OLD"), new ColumnFilterImpl("REL_ID"));
        ColumnSpecificReplacementTable replacementTable = new ColumnSpecificReplacementTable(filteredTable, "A_ID", "B_ID");
        for (Entry<String, String> entry : oldNewElements.entrySet()) {
            replacementTable.addReplacementObject(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
        }
        ITable tableOld = new SortedTable(replacementTable);

        // process the "new" table
        filteredTable = new ColumnFilterTable(queryDataSet.getTable("NEW"), new ColumnFilterImpl("REL_ID"));
        ITable tableNew = new SortedTable(replacementTable);

        // finally, compare the two resulting tables
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
    private void compareDst2Tbl(String oldDstId, String newDstId, Map<String, String> oldNewTables) throws Exception {

        if (oldNewTables == null || oldNewTables.isEmpty()) {
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
        for (Entry<String, String> entry : oldNewTables.entrySet()) {

            String oldTblId = entry.getKey();
            String newTblId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldTblId), Integer.valueOf(newTblId));
        }
        ITable tableOld = new SortedTable(replacementTable);

        // process the "new" table
        filteredTable = new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter);
        replacementTable = new ColumnSpecificReplacementTable(filteredTable, "TABLE_ID");
        for (Entry<String, String> entry : oldNewTables.entrySet()) {

            String oldTblId = entry.getKey();
            String newTblId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldTblId), Integer.valueOf(newTblId));
        }
        ITable tableNew = new SortedTable(replacementTable);

        // finally, compare the two resulting tables
        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());
        // Commented out because the new table versioning creates new table ids
        // DbUnitAssert dbUnitAssert = new DbUnitAssert();
        // dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param oldTblId
     * @param newTblId
     * @param oldNewElements
     * @throws Exception
     */
    private void compareTbl2Elm(String oldTblId, String newTblId, Map<String, String> oldNewElements) throws Exception {

        if (oldNewElements == null || oldNewElements.isEmpty()) {
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
        for (Entry<String, String> entry : oldNewElements.entrySet()) {

            String oldElmId = entry.getKey();
            String newElmId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldElmId), Integer.valueOf(newElmId));
        }
        ITable tableOld = new SortedTable(replacementTable);

        // process the "new" table
        colFilter = new ColumnFilterImpl("TABLE_ID");
        filteredTable = new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter);
        replacementTable = new ColumnSpecificReplacementTable(filteredTable, "DATAELEM_ID");
        for (Entry<String, String> entry : oldNewElements.entrySet()) {

            String oldElmId = entry.getKey();
            String newElmId = entry.getValue();
            replacementTable.addReplacementObject(Integer.valueOf(oldElmId), Integer.valueOf(newElmId));
        }
        ITable tableNew = new SortedTable(replacementTable);

        // finally, compare the two resulting tables
        assertEquals(tableOld.getRowCount(), tableNew.getRowCount());
        // Commented out because the new element versioning creates new auto-incremented ids
        // DbUnitAssert dbUnitAssert = new DbUnitAssert();
        // dbUnitAssert.assertEquals(tableOld, tableNew);
    }

    /**
     *
     * @param oldId
     * @param newId
     * @throws Exception
     */
    private void compareElmToTblRelations(String oldElmId, String newElmId) throws Exception {

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("OLD", "select * from TBL2ELEM where DATAELEM_ID=" + oldElmId);
        queryDataSet.addTable("NEW", "select * from TBL2ELEM where DATAELEM_ID=" + newElmId);

        ColumnFilterImpl colFilter = new ColumnFilterImpl("TABLE_ID", "DATAELEM_ID");
        ITable tableOld = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("OLD"), colFilter));
        ITable tableNew = new SortedTable(new ColumnFilterTable(queryDataSet.getTable("NEW"), colFilter));

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
