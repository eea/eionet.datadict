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

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * This class demonstrates runs a few test cases to demonstrate DbUnit in action.
 * 
 * @author Phil Zoio
 * @author Søren Roug
 * @author Jaanus Heinlaid
 */
public class AttributeTest extends DatabaseTestCase {

	/** */
    public static final String TABLE_NAME = "ATTRIBUTE";

    /** */
    private FlatXmlDataSet loadedDataSet;

    /**
     * Provide a connection to the database.
     */
    protected IDatabaseConnection getConnection() throws Exception
    {
    	Class.forName(Props.getProperty(PropsIF.DBDRV));
		Connection jdbcConn = DriverManager.getConnection(
				Props.getProperty(PropsIF.DBURL),
				Props.getProperty(PropsIF.DBUSR),
				Props.getProperty(PropsIF.DBPSW));
		
        return new DatabaseConnection(jdbcConn);
    }

    /**
     * Load the data which will be inserted for the test
     * The table must already exist
     */
    protected IDataSet getDataSet() throws Exception
    {
        loadedDataSet = new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream(Seed.ATTRIBUTES));
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
     * This should not be enabled. It is just an example
     */
    /*
    public void testExportData() throws Exception
    {
        IDataSet dataSet = getConnection().createDataSet(new String[]
        {
            TABLE_NAME
        });

        URL url = getClass().getClassLoader().getResource(Seed.ATTRIBUTES);
        assertNotNull(url);
        File inputFile = new File(url.getPath());
        File outputFile = new File(inputFile.getParent(), "output.xml");
        FlatXmlDataSet.write(dataSet, new FileOutputStream(outputFile));

        assertEquals(FileUtils.readLines(inputFile, "UTF-8"), FileUtils.readLines(outputFile, "UTF-8"));

    }
    */
}
