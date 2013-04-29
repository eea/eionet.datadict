package eionet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eionet.util.Props;
import eionet.util.PropsIF;


public abstract class DDDatabaseTestCase extends DatabaseTestCase {

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(DDDatabaseTestCase.class);

    /** File name of seed to load with getDataSet() */
    private String seedFilename;

    /**
     * Provide a connection to the database.
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("http://www.dbunit.org/properties/datatypeFactory", "org.dbunit.ext.mysql.MySqlDataTypeFactory");

        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection jdbcConn = DriverManager.getConnection(
                Props.getProperty(PropsIF.DBURL),
                Props.getProperty(PropsIF.DBUSR),
                Props.getProperty(PropsIF.DBPSW));

        DatabaseConnection dbConn = new DatabaseConnection(jdbcConn);
        dbConn.getConfig().setPropertiesByString(properties);
        return dbConn;
    }

    /**
     * Load the data which will be inserted for the test
     * seed-attributes has some fixed values
     */
    protected IDataSet getDataSet() throws Exception {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        FlatXmlDataSet loadedDataSet = builder.build(getClass().getClassLoader().getResourceAsStream(getSeedFilename()));
        return loadedDataSet;
    }

    protected String getSeedFilename() {
        return seedFilename;
    }

}

