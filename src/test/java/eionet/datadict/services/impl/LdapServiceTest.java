package eionet.datadict.services.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.LdapRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class LdapServiceTest {

    @Mock
    private LdapRoleDao ldapRoleDao;

    @InjectMocks
    private LdapServiceImpl ldapServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetLdapRoles() throws Exception {
        List<LdapRole> ldapRoleList = new ArrayList<>();
        LdapRole ldapRole = new LdapRole();
        ldapRole.setName("dd_admin");
        ldapRoleList.add(ldapRole);
        when(ldapRoleDao.findUserRoles(anyString())).thenReturn(ldapRoleList);
        String user = "maria";
        String usersOU = "Users";
        String rolesOU = "DD_roles";
        List<LdapRole> results = ldapServiceImpl.getUserLdapRoles(user);
        Assert.assertEquals(ldapRoleList, results);
    }
}
