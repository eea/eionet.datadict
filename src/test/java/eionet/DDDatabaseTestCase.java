package eionet;

import eionet.meta.ActionBeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestResult;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eionet.util.Props;
import eionet.util.PropsIF;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class DDDatabaseTestCase extends DatabaseTestCase {

    /** Logger. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DDDatabaseTestCase.class);

    /** File name of seed to load with getDataSet() */
    private String seedFilename;


    @Override
    protected void runTest() throws Throwable {
        String fName = super.getName();
        assertNotNull("TestCase.fName cannot be null", fName); // Some VMs crash when calling getMethod(null,null);
        Method runMethod = null;
        try {
            // use getMethod to get all public inherited
            // methods. getDeclaredMethods returns all
            // methods of this class but excludes the
            // inherited ones.
            runMethod = super.getClass().getMethod(fName, (Class[]) null);
            if(isMethodMarkedToBeIgnored(runMethod)){
                return;
            }
        } catch (NoSuchMethodException e) {
            fail("Method \"" + fName + "\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            fail("Method \"" + fName + "\" should be public");
        }

        try {
            runMethod.invoke(this);
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
        }
    }

    /**
     * Provide a connection to the database.
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {
        ActionBeanUtils.getServletContext();
        Properties properties = new Properties();
        properties.setProperty("http://www.dbunit.org/properties/datatypeFactory", "org.dbunit.ext.mysql.MySqlDataTypeFactory");

        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection jdbcConn = DriverManager.getConnection(
                Props.getProperty(PropsIF.DBURL),
                Props.getProperty(PropsIF.DBUSR),
                Props.getProperty(PropsIF.DBPSW));


        DatabaseConnection dbConn = new DatabaseConnection(jdbcConn,"datadict");
        dbConn.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        dbConn.getConfig().setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new MySqlMetadataHandler());

        dbConn.getConfig().setPropertiesByString(properties);
        OPEN_CONNECTIONS.add(jdbcConn);
        return dbConn;
    }

    /**
     * Load the data which will be inserted for the test
     * seed-attributes has some fixed values
     */
    @Override
    protected IDataSet getDataSet() throws Exception {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        FlatXmlDataSet loadedDataSet = builder.build(getClass().getClassLoader().getResourceAsStream(getSeedFilename()));

        //this below is needed to input null values in seed files in format: [NULL]
        ReplacementDataSet replDataSet = new ReplacementDataSet(loadedDataSet);
        replDataSet.addReplacementObject("[NULL]", null);

        return replDataSet;
    }

    protected String getSeedFilename() {
        return seedFilename;
    }

    protected boolean isMethodMarkedToBeIgnored(Method method){
       return method.getDeclaredAnnotation(Ignore.class)!=null ?true :false;
    }

    private static final Set<Connection> OPEN_CONNECTIONS = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void tearDown() throws Exception {
        OPEN_CONNECTIONS.removeIf(c -> {
            try {
                return c.isClosed();
            } catch (SQLException e) {
                return false;
            }
        });
        System.out.println("Open connections after " + getName() + ": " + OPEN_CONNECTIONS.size());

        try {
            for (Connection conn : OPEN_CONNECTIONS) {
                if (conn != null && !conn.isClosed()) {
                    try {
                        conn.close();
                    } catch (Exception e) {
                        LOGGER.warn("Error closing DB connection", e);
                    }
                }
            }
            OPEN_CONNECTIONS.clear();
            System.out.println("Open connections after CLEAR " + getName() + ": " + OPEN_CONNECTIONS.size());
        } finally {
            super.tearDown();
        }
    }

}

