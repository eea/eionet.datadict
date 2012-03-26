package eionet.meta.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.SQL;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class SQLBaseDAO extends BaseDAO {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SQLBaseDAO.class);

    /** */
    private Connection conn;

    /**
     * 
     * @return
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {

        // If instance connection has been set, return that one (because it must have been set by
        // external code which might be running a transaction). Otherwise return a new connection.
        if (conn != null) {
            return conn;
        } else {
            return ConnectionUtil.getConnection();
        }
    }

    /**
     * 
     * @param conn
     * @throws SQLException
     */
    protected void beginTransaction(Connection conn) throws SQLException {

        // Do it only if the given connection is not the one we have as instance variable.
        // Because if it's the instance variable, then the latter must have been set by
        // external code which might be running a transaction.
        if (conn != null && conn != this.conn) {
            conn.setAutoCommit(false);
        }
    }

    /**
     * 
     * @param conn
     * @throws SQLException
     */
    protected void endTransaction(Connection conn) throws SQLException {

        // Do it only if the given connection is not the one we have as instance variable.
        // Because if it's the instance variable, then the latter must have been set by
        // external code which might be running a transaction.
        if (conn != null && conn != this.conn) {
            conn.setAutoCommit(true);
        }
    }

    /**
     * 
     * @param conn
     * @throws SQLException
     */
    protected void commit(Connection conn) throws SQLException {

        // Do it only if the given connection is not the one we have as instance variable.
        // Because if it's the instance variable, then the latter must have been set by
        // external code which might be running a transaction.
        if (conn != null && conn != this.conn) {
            conn.commit();
        }
    }

    /**
     * 
     * @param conn
     */
    protected void rollback(Connection conn) {

        // Do it only if the given connection is not the one we have as instance variable.
        // Because if it's the instance variable, then the latter must have been set by
        // external code which might be running a transaction.
        if (conn != null && conn != this.conn) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOGGER.error("Failed to do rollback", e);
            }
        }
    }

    /**
     * 
     * @param conn
     */
    protected void close(Connection conn) {

        // Close only if the given connection is not the one we have as instance variable.
        // Because if it's the instance variable, then the latter must have been set by
        // external code which might be running a transaction.
        if (conn != null && conn != this.conn) {
            SQL.close(conn);
        }
    }

    /**
     * @see eionet.meta.dao.DAO#setConnection(java.sql.Connection)
     */
    @Override
    public void setConnection(Connection conn) {
        this.conn = conn;
    }
}
