package eionet.test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.io.FileUtils;
import org.dbunit.Assertion;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * This class demonstrates runs a few test cases to demonstrate DbUnit in action
 * @author Phil Zoio
 */
public class AclUnitTest extends DatabaseTestCase
{

    public static final String TABLE_NAME = "ACLS";

    private FlatXmlDataSet loadedDataSet;

    /**
     * Provide a connection to the database
     */
    protected IDatabaseConnection getConnection() throws Exception
    {
        Class driverClass = Class.forName("com.mysql.jdbc.Driver");
        Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost/test_dd", "ddtest", "smrsmr");
        return new DatabaseConnection(jdbcConnection);
    }

    /**
     * Load the data which will be inserted for the test
     * The table must already exist
     */
    protected IDataSet getDataSet() throws Exception
    {
        loadedDataSet = new FlatXmlDataSet(this.getClass().getClassLoader().getResourceAsStream("/input-acls.xml"));
        return loadedDataSet;
    }

    /**
     * Sanity check that the data has been loaded
     */
    public void testCheckDataLoaded() throws Exception
    {
        assertNotNull(loadedDataSet);
        int rowCount = loadedDataSet.getTable(TABLE_NAME).getRowCount();
        assertEquals(2, rowCount);
    }

    /**
     * Show how a data set can be extracted and used to compare with the XML representation
     */
    public void testCompareDataSet() throws Exception
    {
        IDataSet createdDataSet = getConnection().createDataSet(new String[]
        {
            TABLE_NAME
        });
        Assertion.assertEquals(loadedDataSet, createdDataSet);
    }

    /**
     * Compare test data with query-generated IDataSet
     */
    public void testCompareQuery() throws Exception
    {
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
        Assertion.assertEquals(loadedDataSet, queryDataSet);
    }

    /**
     * Test the DbUnit export mechanism
     */
    public void testExportData() throws Exception
    {
        IDataSet dataSet = getConnection().createDataSet(new String[]
        {
            TABLE_NAME
        });

        URL url = DatabaseTestCase.class.getResource("/input-acls.xml");
        assertNotNull(url);
        File inputFile = new File(url.getPath());
        File outputFile = new File(inputFile.getParent(), "output.xml");
        FlatXmlDataSet.write(dataSet, new FileOutputStream(outputFile));

        assertEquals(FileUtils.readFileToString(inputFile, "UTF8"), FileUtils.readFileToString(outputFile, "UTF8"));

    }

}
