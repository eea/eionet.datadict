// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta.savers;


import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.TestCase;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.DDDatabaseTestCase;


public class DataElementHandlerTest extends DDDatabaseTestCase {
    
    @Override
    protected String getSeedFilename() {
        return "seed-dataelement.xml";
    }

    public void testReplaceID() throws Exception {
        String from_id = "8723";
        String to_id = "16953";

        QueryDataSet queryDataSet = createQueries();
        // Verify that there are the expected number of rows in the table
        ITable tmpTable = queryDataSet.getTable("FXV");

        TestCase.assertEquals("28", tmpTable.getValue(0, "C").toString());

        // Verify that there are the expected number of rows in the table
        tmpTable = queryDataSet.getTable("ATTRIBUTE");
        TestCase.assertEquals("8", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("ATTRIBUTE16953");
        TestCase.assertEquals("0", tmpTable.getValue(0, "C").toString());

        // Run the operation
        Connection conn = getConnection().getConnection();
        DataElementHandler.replaceID(from_id, to_id, conn);
                
        // Verify that there are the expected number of rows in the table
        queryDataSet = createQueries();

        tmpTable = queryDataSet.getTable("ATTRIBUTE");
        TestCase.assertEquals("0", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("ATTRIBUTE16953");
        TestCase.assertEquals("8", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("FXV");
        TestCase.assertEquals("0", tmpTable.getValue(0, "C").toString());

    }

    private QueryDataSet createQueries() throws Exception {
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());

        queryDataSet.addTable("ATTRIBUTE", "SELECT count(*) as C FROM ATTRIBUTE WHERE DATAELEM_ID='8723'");
        queryDataSet.addTable("ATTRIBUTE16953", "SELECT count(*) as C FROM ATTRIBUTE WHERE DATAELEM_ID='16953'");
        queryDataSet.addTable("FXV", "SELECT count(*) as C FROM FXV WHERE OWNER_ID='8723'");
        return queryDataSet;
    }
}

