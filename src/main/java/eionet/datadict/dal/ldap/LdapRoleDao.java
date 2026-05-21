package eionet.datadict.dal.ldap;

import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;

import java.util.List;

public interface LdapRoleDao {

    /**
     * fetches user ldap roles
     * @param user
     * @return
     * @throws Exception
     */
    List<LdapRole> findUserRoles(String user) throws LdapDaoException;

}
