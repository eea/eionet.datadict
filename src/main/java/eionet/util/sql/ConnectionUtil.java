package eionet.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eionet.meta.DDRuntimeException;
import eionet.util.IsJUnitRuntime;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class ConnectionUtil {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ConnectionUtil.class);

    /** */
    private static final String DATASOURCE_NAME = "jdbc/datadict";

    /** */
    private static DataSource dataSource = null;
    private static String connectionUrl = null;
    private static Boolean isJNDIDataSource = null;

    /** Lock objects */
    private static Object isJNDIDataSourceLock = new Object();

    /**
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {

        if (isJNDIDataSource()) {
            return dataSource.getConnection();
        } else {
            return getSimpleConnection(IsJUnitRuntime.VALUE);
        }
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    private static Connection getSimpleConnection(boolean isUnitTest) throws SQLException {

        // property names depending on whether the code is being run by a unit test
        // (this is just to avoid shooting in the leg by running unit tests
        // accidentally against the real database)
        String drvProperty = isUnitTest ? PropsIF.DB_UNITTEST_DRV : PropsIF.DBDRV;
        String urlProperty = isUnitTest ? PropsIF.DB_UNITTEST_URL : PropsIF.DBURL;
        String usrProperty = isUnitTest ? PropsIF.DB_UNITTEST_USR : PropsIF.DBUSR;
        String pwdProperty = isUnitTest ? PropsIF.DB_UNITTEST_PWD : PropsIF.DBPSW;

        String drv = Props.getProperty(drvProperty);
        if (drv == null || drv.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + drvProperty);
        }

        String url = Props.getProperty(urlProperty);
        if (url == null || url.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + urlProperty);
        }

        String usr = Props.getProperty(usrProperty);
        if (usr == null || usr.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + usrProperty);
        }

        String pwd = Props.getProperty(pwdProperty);
        if (pwd == null || pwd.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + pwdProperty);
        }

        try {
            Class.forName(drv);
            return DriverManager.getConnection(url, usr, pwd);
        } catch (ClassNotFoundException e) {
            throw new DDRuntimeException("Failed to get connection, driver class not found: " + drv, e);
        }
    }

    /**
     *
     * @return
     */
    private static boolean isJNDIDataSource() {

        if (isJNDIDataSource == null) {
            synchronized (isJNDIDataSourceLock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (isJNDIDataSource == null) {

                    try {
                        Context initContext = new InitialContext();
                        Context context = (Context) initContext.lookup("java:comp/env");
                        dataSource = (javax.sql.DataSource) context.lookup(DATASOURCE_NAME);

                        isJNDIDataSource = Boolean.TRUE;
                        LOGGER.info("Found and initialized JNDI data source named " + DATASOURCE_NAME);
                    } catch (NamingException e) {
                        isJNDIDataSource = Boolean.FALSE;
                        LOGGER.info("No JNDI data source named " + DATASOURCE_NAME + " could be found: " + e.toString());
                    }
                }
            }
        }

        return isJNDIDataSource.booleanValue();
    }
}