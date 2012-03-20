package eionet.meta.dao;

import eionet.meta.DDException;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class DAOException extends DDException {

    /**
     *
     */
    public DAOException() {
        super();
    }

    /**
     *
     * @param message
     */
    public DAOException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}