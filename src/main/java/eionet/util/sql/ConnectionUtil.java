package eionet.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class ConnectionUtil {

    /** */
    public static final String SIMPLE_CONNECTION = "simple";
    public static final String JNDI_CONNECTION = "jndi";
    private static final String DEFAULT_CONNECTION = JNDI_CONNECTION;

    /** */
    private static final String DATA_SOURCE_NAME = "jdbc/datadict";

    /** */
    private static LogServiceIF logger = new Log4jLoggerImpl();

    /** */
    private static DataSource dataSource = null;
    private static String connectionType = DEFAULT_CONNECTION;

    /**
     *
     * @throws NamingException
     * @throws DAOException
     */
    private static void initDataSource() throws NamingException {
        Context initContext = new InitialContext();
        Context context = (Context) initContext.lookup("java:comp/env");
        dataSource = (javax.sql.DataSource)context.lookup(DATA_SOURCE_NAME);
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws DDConnectionException {

        if (ConnectionUtil.connectionType.equals(SIMPLE_CONNECTION))
            return getSimpleConnection();
        else if (ConnectionUtil.connectionType.equals(JNDI_CONNECTION))
            return getJNDIConnection();
        else
            throw new DDConnectionException("Unknown connection type: " + ConnectionUtil.connectionType);
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    private static synchronized Connection getJNDIConnection() throws DDConnectionException {

        try {
            if (dataSource==null) {
                initDataSource();
            }
            return dataSource.getConnection();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DDConnectionException("Failed to get connection through JNDI: " + e.toString(), e);
        }
    }

    /**
     *
     * @return
     * @throws SQLException
     * @throws SQLException
     */
    public static Connection getSimpleConnection() throws DDConnectionException {

        String drv = Props.getProperty(PropsIF.DBDRV);
        if (drv==null || drv.trim().length()==0)
            throw new DDConnectionException("Failed to get connection, missing property: " + PropsIF.DBDRV);

        String url = Props.getProperty(PropsIF.DBURL);
        if (url==null || url.trim().length()==0)
            throw new DDConnectionException("Failed to get connection, missing property: " + PropsIF.DBURL);

        String usr = Props.getProperty(PropsIF.DBUSR);
        if (usr==null || usr.trim().length()==0)
            throw new DDConnectionException("Failed to get connection, missing property: " + PropsIF.DBUSR);

        String pwd = Props.getProperty(PropsIF.DBPSW);
        if (pwd==null || pwd.trim().length()==0)
            throw new DDConnectionException("Failed to get connection, missing property: " + PropsIF.DBPSW);

        try {
            Class.forName(drv);
            return DriverManager.getConnection(url, usr, pwd);
        }
        catch (Exception e) {
            throw new DDConnectionException("Failed to get connection through DriverManager: " + e.toString(), e);
        }
    }

    /**
     *
     * @param conn
     */
    public static void close(Connection conn) {
        try {
            if (conn!=null && !conn.isClosed())
                conn.close();
        }
        catch (SQLException e) {
            logger.error("Failed to close connection", e);
        }
    }

    /**
     * @return Returns the connectionType.
     */
    public static String getConnectionType() {
        return connectionType;
    }

    /**
     *
     * @param connectionType The connectionType to set.
     */
    public static synchronized void setConnectionType(String type) {

        if (type==null)
            throw new NullPointerException();
        ConnectionUtil.connectionType = type;
    }
}