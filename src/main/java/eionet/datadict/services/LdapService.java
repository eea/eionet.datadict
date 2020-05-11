package eionet.datadict.services;

import eionet.datadict.model.LdapRole;

import java.util.List;

public interface LdapService {

    /**
     * fetches user ldap roles
     * @return
     */
    List<LdapRole> getUserLdapRoles(String user, String usersOU, String rolesOU);

    /**
     * fetces all ldap roles
     * @param usersOU
     * @param rolesOU
     * @return
     */
    List<LdapRole> getAllLdapRoles(String usersOU, String rolesOU);
}
