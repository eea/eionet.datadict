package eionet.meta.dao.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eionet.meta.dao.DAOException;
import eionet.meta.dao.SQLBaseDAO;
import eionet.util.sql.SQL;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class MySQLBaseDAO extends SQLBaseDAO {

    /**
     * 
     * @param conn
     * @return
     * @throws DAOException
     */
    protected int getLastInsertId(Connection conn) throws DAOException {
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select last_insert_id()");
            return (rs != null && rs.next()) ? new Integer(rs.getInt(1)) : null;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }
}
