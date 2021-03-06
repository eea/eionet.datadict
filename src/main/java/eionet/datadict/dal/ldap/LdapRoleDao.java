package eionet.datadict.dal.ldap;

import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;

import java.util.Hashtable;
import java.util.List;

public interface LdapRoleDao {

    /**
     * fetches user ldap roles
     * @param user
     * @return
     * @throws Exception
     */
    public List<LdapRole> findUserRoles(String user) throws LdapDaoException;

    /**
     * fetches all ldap roles
     * @return
     * @throws Exception
     */
    public List<LdapRole> findAllRoles() throws LdapDaoException;

    /**
     * fetches users that belong to role roleName
     * @param roleName
     * @return
     * @throws LdapDaoException
     */
    public List<String> findRoleUsers(String roleName) throws LdapDaoException;

    /**
     * fetches information of ldap role
     * @param roleName
     * @return
     * @throws LdapDaoException
     */
    Hashtable<String, Object> getRoleInfo(String roleName) throws LdapDaoException;
}
