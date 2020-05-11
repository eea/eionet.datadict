package eionet.datadict.dal.ldap;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import eionet.config.ApplicationTestContext;
import eionet.datadict.dal.ldap.impl.LdapRoleDaoImpl;
import eionet.datadict.dal.ldap.impl.LdapUserDaoImpl;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
public class LdapConnectionTestIT {

    // @Autowired
    LdapUserDaoImpl ldapUserDaoImpl;
    LdapRoleDaoImpl ldapRoleDaoImpl;
    LdapRoleDao ldapRoleDao;
    @Autowired
    LdapService ldapService;

    @Test
    public void testConnection() throws Exception {
//        ldapUserDaoImpl = new LdapUserDaoImpl();
//        ldapRoleDaoImpl = new LdapRoleDaoImpl();
////        ldapService = new LdapServiceImpl(ldapRoleDao);
//        List result = ldapUserDaoImpl.findAllUsers();
//        List<LdapRole> roles = ldapRoleDaoImpl.findUserRoles("maria", "Users", "DD_roles");
//        List<LdapRole> ldapRoles = ldapService.getLdapRoles("maria", "Users", "DD_roles");
//        assertNotNull(result);
//        assertNotNull(roles);
    }

}

