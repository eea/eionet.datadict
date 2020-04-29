package eionet.datadict.dal.ldap;

import eionet.datadict.model.User;

import java.util.List;

public interface LdapUserDao {

    public List<User> findAllUsers();
}
