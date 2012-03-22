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
import java.sql.SQLException;
import java.sql.Savepoint;

import org.apache.log4j.Logger;

import eionet.meta.DDRuntimeException;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class Transaction {

    /** */
    private static final Logger LOGGER = Logger.getLogger(Transaction.class);

    /** */
    private boolean isRunningInAnotherTransaction = false;

    /** */
    private Connection conn = null;

    /** */
    private Savepoint savepoint = null;

    /**
     * 
     * @param conn
     */
    private Transaction(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("The given connection must not be null!");
        }
        this.conn = conn;
    }

    /**
     *
     *
     */
    private void start() {

        try {
            if (conn.getAutoCommit() == false) {
                isRunningInAnotherTransaction = true;
            } else{
                conn.setAutoCommit(false);
            }
            savepoint = conn.setSavepoint();
        } catch (SQLException e) {
            throw new DDRuntimeException(e.toString(), e);
        }
    }

    /**
     * 
     * @param conn
     * @return
     */
    public static Transaction start(Connection conn) {
        Transaction tx = new Transaction(conn);
        tx.start();
        return tx;
    }

    /**
     * @throws SQLException
     * 
     * 
     */
    public void commit() throws SQLException {

        if (savepoint == null){
            throw new IllegalStateException("Transaction not yet started!");
        }

        if (!isRunningInAnotherTransaction){
            conn.commit();
        }
    }

    /**
     *
     *
     */
    public void rollback() {
        try {
            conn.rollback(savepoint);
        } catch (SQLException e) {
            LOGGER.error("Failure when trying connection rollback:", e);
        }
    }

    /**
     *
     *
     */
    public void end() {
        try {
            conn.releaseSavepoint(savepoint);
            savepoint = null;
            if (!isRunningInAnotherTransaction) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Failure trying savepoint release and transaction end:", e);
        }
    }
}
