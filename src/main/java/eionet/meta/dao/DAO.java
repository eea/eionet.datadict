package eionet.meta.dao;

import java.sql.Connection;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public interface DAO {

    /**
     * 
     * @param conn
     */
    void setConnection(Connection conn);
}
