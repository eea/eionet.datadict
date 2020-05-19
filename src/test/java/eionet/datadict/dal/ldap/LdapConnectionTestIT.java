package eionet.datadict.dal.ldap;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import eionet.config.ApplicationTestContext;
import eionet.datadict.dal.ldap.impl.LdapRoleDaoImpl;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.impl.LdapServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = {ApplicationTestContext.class})
public class LdapConnectionTestIT {

    LdapRoleDaoImpl ldapRoleDaoImpl;
    LdapRoleDao ldapRoleDao;
//    LdapService ldapService;

    @Test
    public void testConnection() throws Exception {
        ldapRoleDaoImpl = new LdapRoleDaoImpl();
//        ldapService = new LdapServiceImpl(ldapRoleDao);
        List<LdapRole> roles = ldapRoleDaoImpl.findUserRoles("favvmary", "Users", "Roles");
        List<LdapRole> ldapRoles = ldapRoleDaoImpl.findAllRoles("Roles");
        //List<LdapRole> ldapRoles = ldapService.getUserLdapRoles("maria", "Users", "DD_roles");
//        assertNotNull(result);
        assertNotNull(roles);
        assertNotNull(ldapRoles);
    }

}

