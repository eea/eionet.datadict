package eionet.datadict.services;

import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;

import java.util.Hashtable;
import java.util.List;

public interface LdapService {

    /**
     * fetches user ldap roles
     * @return
     */
    List<LdapRole> getUserLdapRoles(String user) throws LdapDaoException;

    /**
     * fetches all ldap roles
     * @return
     */
    List<LdapRole> getAllLdapRoles() throws LdapDaoException;

    /**
     * fetches users that belong to role roleName
     * @param roleName
     * @return
     * @throws LdapDaoException
     */
    List<String> getRoleUsers(String roleName) throws LdapDaoException;

    /**
     * fetches information of ldap role
     * @param roleName
     * @return
     * @throws LdapDaoException
     */
    Hashtable<String, Object> getRoleInfo(String roleName) throws  LdapDaoException;
}
