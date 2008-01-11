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

import eionet.meta.savers.FixedValuesHandler;


public class FixedValuesHandlerTest extends DatabaseTestCase {
	
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
                        "seed-attributes.xml"));
        return loadedDataSet;
    }

    private void runEditFixedValue(String definition, String short_desc) throws Exception {
        String fxv_id = "1280";

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());

        queryDataSet.addTable("FXV", "SELECT count(*) as C FROM FXV");
        queryDataSet.addTable("EDITREC", "SELECT * FROM FXV WHERE FXV_ID='1280'");

        // Verify that there are the expected number of rows in the table
        ITable tmpTable = queryDataSet.getTable("FXV");

        TestCase.assertEquals("20", tmpTable.getValue(0, "C").toString());

        Connection jdbcConn = getConnection().getConnection();

        Parameters pars = new Parameters();

        pars.addParameterValue("mode", "edit");
        pars.addParameterValue("fxv_id", fxv_id);
        pars.addParameterValue("delem_id", "9923");
        pars.addParameterValue("parent_type", "elem");
        pars.addParameterValue("definition", definition);
        pars.addParameterValue("short_desc", short_desc);
            
        FixedValuesHandler handler = new FixedValuesHandler(jdbcConn, pars, null);

        handler.execute();
	
        // Verify that there are the expected number of rows in the table
        tmpTable = queryDataSet.getTable("FXV");
        TestCase.assertEquals("20", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("EDITREC");
        TestCase.assertEquals(definition, tmpTable.getValue(0, "DEFINITION"));
        TestCase.assertEquals(short_desc, tmpTable.getValue(0, "SHORT_DESC"));

    }

    public void testSimpleValues() throws Exception {
        runEditFixedValue("plaaplaatt", "plaaplaarrrr");
    }

    public void testQuotes() throws Exception {
        runEditFixedValue("plaap'laatt", "plaap'laarrrr");
    }

    public void testQuoteBackslash() throws Exception {
        runEditFixedValue("plaap\'laatt", "plaap\'laarrrr");
    }

    public void testGreek() throws Exception {
        runEditFixedValue("Τίτλος: Ηλέκτρα",
                "Τίτλος: Ηλέκτρα");
    }
}

