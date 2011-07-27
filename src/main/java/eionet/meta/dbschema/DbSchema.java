package eionet.meta.dbschema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;
import eionet.util.sql.SQL;

/*
 *
 * @author Jaanus Heinlaid
 *
 */
public class DbSchema implements ServletContextListener{

    /** */
    private static final Logger LOGGER = Logger.getLogger(DbSchema.class);

    /** */
    private static DbSchema instance;

    /** */
    private HashMap<String,Set<String>> tablesColumns = new HashMap<String, Set<String>>();

    /**
     * @throws DDConnectionException
     * @throws SQLException
     *
     */
    private void init() throws DDConnectionException, SQLException{

        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        try{
            conn = ConnectionUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("show tables");

            while (rs.next()){
                tablesColumns.put(rs.getString(1), null);
            }
            SQL.close(rs);

            for (String tableName : tablesColumns.keySet()){

                HashSet<String> columns = new HashSet<String>();

                rs = stmt.executeQuery("describe " + tableName);
                while (rs.next()){
                    columns.add(rs.getString(1));
                }
                SQL.close(rs);

                tablesColumns.put(tableName, Collections.unmodifiableSet(columns));
            }
        }
        finally{
            SQL.close(rs);
            SQL.close(stmt);
            SQL.close(conn);
        }
    }

    /**
     *
     * @param tableName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getTableColumns(String tableName){

        return instance.tablesColumns.get(tableName);
    }

    /**
     *
     * @param tableName
     * @return
     */
    public static Set<String> getTableColumns(String tableName, String... skipColumns){

        if (skipColumns==null || skipColumns.length==0){
            return getTableColumns(tableName);
        }
        else{
            HashSet<String> result = new HashSet<String>(instance.tablesColumns.get(tableName));
            for (int i=0; i<skipColumns.length; i++){
                result.remove(skipColumns[i]);
            }
            return result;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {

        String simpleName = DbSchema.class.getSimpleName();
        LOGGER.debug("Initializing " + simpleName);
        try {
            init();
            instance = this;

        } catch (DDConnectionException e) {
            LOGGER.fatal("Failed to get connection", e);
        } catch (SQLException e) {
            LOGGER.fatal("Failed to initialize " + simpleName, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
    }
}
