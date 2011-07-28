package eionet.meta.savers;

import java.sql.SQLException;
import java.util.Arrays;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.SQL;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class CopyHandlerTest extends DatabaseTestCase {

    /*
     * (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {

        return new DatabaseConnection(ConnectionUtil.getConnection());
    }

    /*
     * (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {

        return new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream("seed-dataset.xml"));
    }

    /**
     * @throws Exception
     * @throws SQLException
     *
     */
    @Test
    public void testCopyAutoIncRow() throws SQLException, Exception{

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where SHORT_NAME='test_dataset1' order by DATASET_ID");
        ITable table = queryDataSet.getTable("DATASET");

        assertEquals(1, table.getRowCount());

        CopyHandler copyHandler = new CopyHandler(getConnection().getConnection(), null, null);
        int newId = copyHandler.copyAutoIncRow("DATASET", "SHORT_NAME='test_dataset1'", "DATASET_ID");

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where SHORT_NAME='test_dataset1'");
        table = queryDataSet.getTable("DATASET");

        assertEquals(2, table.getRowCount());

        int dstId1 = Integer.parseInt(table.getValue(0, "DATASET_ID").toString());
        int dstId2 = Integer.parseInt(table.getValue(1, "DATASET_ID").toString());
        assertTrue(dstId1 != dstId2);
        assertTrue(newId == dstId2);
    }

    /**
     * @throws Exception
     * @throws SQLException
     *
     */
    @Test
    public void testPreparedStatementWithNullValues() throws SQLException, Exception{

        String sql1 = "update DATASET set USER=ifnull(?,USER) where IDENTIFIER='test_dataset1'";

        Object[] values = {null};
        SQL.executeUpdate(sql1, Arrays.asList(values), getConnection().getConnection());

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where IDENTIFIER='test_dataset1'");
        ITable table = queryDataSet.getTable("DATASET");
        assertEquals("heinlja", table.getValue(0, "USER").toString());

        Object[] values2 = {"kasperen"};
        SQL.executeUpdate(sql1, Arrays.asList(values2), getConnection().getConnection());

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where IDENTIFIER='test_dataset1'");
        table = queryDataSet.getTable("DATASET");
        assertEquals("kasperen", table.getValue(0, "USER").toString());
    }
}
