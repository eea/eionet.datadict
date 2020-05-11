package eionet.datadict.dal.ldap;

import eionet.datadict.model.LdapUser;

import java.util.List;

public interface LdapUserDao {

    public List<LdapUser> findAllUsers();
}
