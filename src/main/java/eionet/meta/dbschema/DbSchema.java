package eionet.meta.dbschema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;
import eionet.util.sql.SQL;

/*
 *
 * @author Jaanus Heinlaid
 *
 */
public class DbSchema{

    /** */
    private static final Logger LOGGER = Logger.getLogger(DbSchema.class);

    /** */
    private static DbSchema instance = new DbSchema();

    /** */
    private HashMap<String,Set<String>> tablesColumns = new HashMap<String, Set<String>>();

    /**
     *
     */
    private DbSchema(){

        try {
            init();
        } catch (DDConnectionException e) {
            LOGGER.fatal("Failed to get connection", e);
        } catch (SQLException e) {
            LOGGER.fatal("Failed to initialize " + DbSchema.class.getSimpleName(), e);
        }
    }

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

                LinkedHashSet<String> columns = new LinkedHashSet<String>();

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

    /**
     *
     * @param tableName
     * @param skipColumns
     * @return
     */
    public static Set<String> getTableColumns(String tableName, Collection<String> skipColumns){

        if (skipColumns==null || skipColumns.isEmpty()){
            return getTableColumns(tableName);
        }
        else{
            HashSet<String> result = new HashSet<String>(instance.tablesColumns.get(tableName));
            for (String skipColumn : skipColumns){
                result.remove(skipColumn);
            }
            return result;
        }
    }
}
