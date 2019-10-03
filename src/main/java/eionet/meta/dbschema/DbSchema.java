package eionet.meta.dbschema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;
import eionet.util.sql.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *
 * @author Jaanus Heinlaid
 *
 */
public class DbSchema {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbSchema.class);

    /** */
    private static DbSchema instance = new DbSchema();

    /** */
    private HashMap<String, List<String>> tablesColumns = new HashMap<String, List<String>>();

    /**
     *
     */
    private DbSchema() {

        try {
            init();
        } catch (DDConnectionException e) {
            LOGGER.error("Failed to get connection", e);
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize " + DbSchema.class.getSimpleName(), e);
        }
    }

    /**
     * @throws DDConnectionException
     * @throws SQLException
     *
     */
    private void init() throws DDConnectionException, SQLException {

        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        ArrayList<String> tableNamesCaseSensitive = new ArrayList<String>();
        try {
            conn = ConnectionUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("show tables");

            while (rs.next()) {
                String name = rs.getString(1);
                tableNamesCaseSensitive.add(name);
                tablesColumns.put(name.toUpperCase(), null);
            }
            SQL.close(rs);

            for (String tableName : tableNamesCaseSensitive) {

                ArrayList<String> columns = new ArrayList<String>();

                rs = stmt.executeQuery("describe `" + tableName + "`");
                while (rs.next()) {
                    columns.add(rs.getString(1));
                }
                SQL.close(rs);

                tablesColumns.put(tableName.toUpperCase(), Collections.unmodifiableList(columns));
            }
        } finally {
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
    public static List<String> getTableColumns(String tableName) {

        return instance.tablesColumns.get(tableName.toUpperCase());
    }

    /**
     *
     * @param tableName
     * @return
     */
    public static List<String> getTableColumns(String tableName, String... skipColumns) {

        if (skipColumns == null || skipColumns.length == 0) {
            return getTableColumns(tableName);
        } else {
            List<String> columns = getTableColumns(tableName);
            List<String> result = (columns == null) ? new ArrayList<String>() : new ArrayList<String>(columns);
            for (int i = 0; i < skipColumns.length; i++) {
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
    public static List<String> getTableColumns(String tableName, Collection<String> skipColumns) {

        if (skipColumns == null || skipColumns.isEmpty()) {
            return getTableColumns(tableName);
        } else {
            List<String> columns = getTableColumns(tableName);
            List<String> result = (columns == null) ? new ArrayList<String>() : new ArrayList<String>(columns);
            for (String skipColumn : skipColumns) {
                result.remove(skipColumn);
            }
            return result;
        }
    }
}
