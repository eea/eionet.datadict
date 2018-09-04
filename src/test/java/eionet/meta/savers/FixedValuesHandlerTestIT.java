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


public class FixedValuesHandlerTestIT extends DDDatabaseTestCase {
    
    @Override
    protected String getSeedFilename() {
        return "seed-attributes.xml";
    }

    private void runEditFixedValue(String definition, String short_desc) throws Exception {
        String fxv_id = "1280";

        QueryDataSet queryDataSet = createQueries();

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
        queryDataSet = createQueries();
        tmpTable = queryDataSet.getTable("FXV");
        TestCase.assertEquals("20", tmpTable.getValue(0, "C").toString());

        tmpTable = queryDataSet.getTable("EDITREC");
        TestCase.assertEquals(definition, tmpTable.getValue(0, "DEFINITION"));
        TestCase.assertEquals(short_desc, tmpTable.getValue(0, "SHORT_DESC"));

    }

    private QueryDataSet createQueries() throws Exception {
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());

        queryDataSet.addTable("FXV", "SELECT count(*) as C FROM FXV");
        queryDataSet.addTable("EDITREC", "SELECT * FROM FXV WHERE FXV_ID='1280'");
        return queryDataSet;
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

