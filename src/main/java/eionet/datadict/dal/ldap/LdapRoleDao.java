package eionet.datadict.dal.ldap;

import eionet.datadict.model.LdapRole;

import java.util.List;

public interface LdapRoleDao {

    public List<LdapRole> findUserRoles(String user, String usersOU, String rolesOU) throws Exception;

    public List<LdapRole> findAllRoles(String rolesOU) throws Exception;
}
