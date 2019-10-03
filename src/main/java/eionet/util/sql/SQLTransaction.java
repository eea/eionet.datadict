/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TietoEnator Estonia are
 * Copyright (C) 2003 European Environment Agency. All
 * Rights Reserved.
 *
 * Contributor(s):
 */
package eionet.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;

import eionet.meta.dao.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class SQLTransaction implements Transaction {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLTransaction.class);

    /** */
    private boolean isRunningInAnotherTransaction = false;

    /** */
    private Connection conn = null;

    /** */
    private Savepoint savepoint = null;

    /** */
    private boolean isExternalConnection;

    /**
     * 
     * @param conn
     */
    private SQLTransaction(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("The given connection must not be null!");
        }
        this.conn = conn;
        this.isExternalConnection = true;
    }

    /**
     * @see eionet.util.sql.Transaction#begin()
     */
    @Override
    public void begin() throws DAOException {

        try {
            if (conn == null) {
                conn = ConnectionUtil.getConnection();
            }
            if (conn.getAutoCommit() == false) {
                isRunningInAnotherTransaction = true;
            } else {
                conn.setAutoCommit(false);
            }
            savepoint = conn.setSavepoint();
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        }
    }

    /**
     *
     * @param conn
     * @return
     */
    public static SQLTransaction begin(Connection conn) throws DAOException {
        SQLTransaction tx = new SQLTransaction(conn);
        tx.begin();
        return tx;
    }

    /**
     * @throws DAOException
     * @see eionet.util.sql.Transaction#commit()
     */
    @Override
    public void commit() throws DAOException {

        if (savepoint == null) {
            throw new IllegalStateException("Transaction not yet started!");
        }

        if (!isRunningInAnotherTransaction) {
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new DAOException(e.toString(), e);
            }
        }
    }

    /**
     * @see eionet.util.sql.Transaction#rollback()
     */
    @Override
    public void rollback() {
        try {
            if (conn != null && savepoint != null) {
                conn.rollback(savepoint);
            }
        } catch (SQLException e) {
            LOGGER.error("Failure when trying connection rollback:", e);
        }
    }

    /**
     * @see eionet.util.sql.Transaction#close()
     */
    @Override
    public void close() {

        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                LOGGER.error("Failure trying to close the transaction:", e);
            }
        }
    }

    /**
     * 
     */
    public void end() {
        try {
            if (conn != null && savepoint != null) {
                conn.releaseSavepoint(savepoint);
                savepoint = null;
                if (!isRunningInAnotherTransaction) {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failure trying savepoint release and transaction end:", e);
        }
    }

    /**
     * 
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {

        if (savepoint == null) {
            throw new IllegalStateException("Transaction not yet started!");
        } else {
            return conn;
        }
    }

    /**
     * 
     * @param transaction
     */
    public static void rollback(SQLTransaction transaction) {

        if (transaction != null) {
            transaction.rollback();
        }
    }

    /**
     * 
     * @param transaction
     */
    public static void end(SQLTransaction transaction) {

        if (transaction != null) {
            transaction.end();
        }
    }
    /**
     */
    public static void main(String[] args) throws ClassNotFoundException {

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url =
                "jdbc:mysql://localhost/DataDict?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&emptyStringsConvertToZero=false&jdbcCompliantTruncation=false";
            conn = DriverManager.getConnection(url, "dduser", "xxx");
            conn.setAutoCommit(false);
            System.out.println("Current auto-commit = " + conn.getAutoCommit());
            int i = conn.createStatement().executeUpdate("update M_ATTRIBUTE set SHORT_NAME=SHORT_NAME");
            System.out.println("i = " + i);
            conn.commit();
            System.out.println("Now auto-commit = " + conn.getAutoCommit());
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException sqle) {
                    LOGGER.error(sqle.getMessage(), sqle);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
