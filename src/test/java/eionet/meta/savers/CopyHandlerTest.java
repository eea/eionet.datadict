package eionet.meta.savers;

import java.sql.SQLException;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.util.sql.ConnectionUtil;

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
        queryDataSet.addTable("DATASET", "select * from DATASET where SHORT_NAME='test_dataset1'");
        ITable table = queryDataSet.getTable("DATASET");
        int rowCountBefore = table.getRowCount();

        assertEquals(1, rowCountBefore);

        CopyHandler copyHandler = new CopyHandler(getConnection().getConnection(), null, null);
        copyHandler.copyAutoIncRow("DATASET", "SHORT_NAME='test_dataset1'", "DATASET_ID");

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DATASET", "select * from DATASET where SHORT_NAME='test_dataset1'");
        table = queryDataSet.getTable("DATASET");

        int rowCountAfter = table.getRowCount();
        assertEquals(2, table.getRowCount());

        int dstId1 = Integer.parseInt(table.getValue(0, "DATASET_ID").toString());
        int dstId2 = Integer.parseInt(table.getValue(1, "DATASET_ID").toString());
        assertTrue(dstId1 != dstId2);
    }
}
