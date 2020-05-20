package eionet.datadict.services;

import eionet.datadict.model.LdapRole;

import java.util.List;

public interface LdapService {

    /**
     * fetches user ldap roles
     * @return
     */
    List<LdapRole> getUserLdapRoles(String user);

    /**
     * fetces all ldap roles
     * @return
     */
    List<LdapRole> getAllLdapRoles();
}
