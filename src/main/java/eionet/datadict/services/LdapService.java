package eionet.datadict.services;

import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;

import java.util.List;

public interface LdapService {

    /**
     * fetches user ldap roles
     * @return
     */
    List<LdapRole> getUserLdapRoles(String user) throws LdapDaoException;

    /**
     * fetces all ldap roles
     * @return
     */
    List<LdapRole> getAllLdapRoles() throws LdapDaoException;
}
