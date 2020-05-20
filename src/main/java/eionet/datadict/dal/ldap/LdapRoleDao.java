package eionet.datadict.dal.ldap;

import eionet.datadict.model.LdapRole;

import java.util.List;

public interface LdapRoleDao {

    /**
     * fetches user ldap roles
     * @param user
     * @return
     * @throws Exception
     */
    public List<LdapRole> findUserRoles(String user) throws Exception;

    /**
     * fetches all ldap roles
     * @return
     * @throws Exception
     */
    public List<LdapRole> findAllRoles() throws Exception;
}
