package eionet.datadict.dal.ldap;

import eionet.datadict.model.Role;

import java.util.List;

public interface LdapRoleDao {

    public List<Role> findUserRoles(String user) throws Exception;
}
