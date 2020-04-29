package eionet.datadict.dal.ldap;

import eionet.config.ApplicationTestContext;
import eionet.datadict.dal.ldap.impl.LdapRoleDaoImpl;
import eionet.datadict.dal.ldap.impl.LdapUserDaoImpl;
import eionet.datadict.model.Role;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = {ApplicationTestContext.class})
public class LdapConnectionTestIT {

   // @Autowired
    LdapUserDaoImpl ldapUserDao;
    LdapRoleDaoImpl ldapRoleDao;

    @Test
    public void testConnection() throws Exception {
        ldapUserDao = new LdapUserDaoImpl();
        ldapRoleDao = new LdapRoleDaoImpl();
        List result = ldapUserDao.findAllUsers();
        List<Role> roles = ldapRoleDao.findUserRoles("dd_admin");
        assertNotNull(result);
        assertNotNull(roles);
    }

}
