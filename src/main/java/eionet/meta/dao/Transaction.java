package eionet.meta.dao;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public interface Transaction {

    /**
     * Start a transaction.
     * 
     * @throws DAOException
     */
    void begin() throws DAOException;

    /**
     * Commit a transaction.
     * 
     * @throws DAOException
     */
    void commit() throws DAOException;

    /**
     * Rollback a transaction.
     */
    void rollback();

    /**
     * Close resources associated with this transaction.
     */
    void close();
}
