package eionet.meta.dao;

import eionet.meta.DDException;

/**
 * 
 *
 */
public class LdapDaoException extends DDException {

    public LdapDaoException() {
    }

    public LdapDaoException(String message) {
        super(message);
    }

    public LdapDaoException(Exception ex) {
        super(ex);
    }
}