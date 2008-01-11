// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta.savers;


import junit.framework.TestCase;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.http.*;

// import eionet.test.Seed;
// import eionet.test.TestingResources;
import eionet.util.Props;
import eionet.util.PropsIF;

import eionet.meta.savers.DataElementHandler;


public class DataElementHandlerTest extends DatabaseTestCase {
	
    private FlatXmlDataSet loadedDataSet;

    /**
     * Provide a connection to the database.
     */
    protected IDatabaseConnection getConnection() throws Exception {
        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection jdbcConn = DriverManager.getConnection(
                Props.getProperty(PropsIF.DBURL),
                Props.getProperty(PropsIF.DBUSR),
                Props.getProperty(PropsIF.DBPSW));
		    
        return new DatabaseConnection(jdbcConn);
    }

    /**
     * Load the data which will be inserted for the test
     * seed-attributes has some fixed values
     */
    protected IDataSet getDataSet() throws Exception {
        loadedDataSet = new FlatXmlDataSet(
                getClass().getClassLoader().getResourceAsStream(
                        "seed-dataelement.xml"));
        return loadedDataSet;
    }

    public void testReplaceID() throws Exception {
        String from_id = "8723";
        String to_id = "16953";

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());

        queryDataSet.addTable("ATTRIBUTE",
                "SELECT count(*) as C FROM ATTRIBUTE WHERE DATAELEM_ID='8723'");
        queryDataSet.addTable("ATTRIBUTE16953",
                "SELECT count(*) as C FROM ATTRIBUTE WHERE DATAELEM_ID='16953'");
        queryDataSet.addTable("FXV",
                "SELECT count(*) as C FROM FXV WHERE OWNER_ID='8723'");

        // Verify that there are the expected number of rows in the table
        ITable tmpTable = queryDataSet.getTable("FXV");

        TestCase.assertEquals("28", tmpTable.getValue(0, "C").toString());

        Connection conn = getConnection().getConnection();

        DataElementHandler.replaceID(from_id, to_id, conn);
                
        // Verify that there are the expected number of rows in the table
        tmpTable = queryDataSet.getTable("ATTRIBUTE");
        TestCase.assertEquals("0", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("ATTRIBUTE16953");
        TestCase.assertEquals("8", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("FXV");
        TestCase.assertEquals("0", tmpTable.getValue(0, "C").toString());

    }
}

