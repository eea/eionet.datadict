package eionet.datadict.services.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;
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

    private List<LdapRole> ldapRoleList;
    private LdapRole ldapRole;
    private static final String USER = "maria";
    private static final String ACL_GROUP = "dd_admin";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ldapRoleList = new ArrayList<>();
        ldapRole = new LdapRole();
        ldapRole.setName(ACL_GROUP);
        ldapRoleList.add(ldapRole);
    }

    @Test
    public void testGetUserLdapRolesSuccess() throws Exception {
        when(ldapRoleDao.findUserRoles(anyString())).thenReturn(ldapRoleList);
        List<LdapRole> results = ldapServiceImpl.getUserLdapRoles(USER);
        Assert.assertEquals(ldapRoleList, results);
    }

    @Test
    public void testGetUserLdapRolesException() throws LdapDaoException {
        when(ldapRoleDao.findUserRoles(anyString())).thenThrow(LdapDaoException.class);
        List<LdapRole> results = ldapServiceImpl.getUserLdapRoles(USER);
        Assert.assertEquals(0, results.size());
    }

    @Test
    public void testGetAllLdapRolesSuccess() throws Exception {
        when(ldapRoleDao.findAllRoles()).thenReturn(ldapRoleList);
        List<LdapRole> results = ldapServiceImpl.getAllLdapRoles();
        Assert.assertEquals(ldapRoleList, results);
    }

    @Test
    public void testGetAllLdapRolesException() throws LdapDaoException {
        when(ldapRoleDao.findAllRoles()).thenThrow(LdapDaoException.class);
        List<LdapRole> results = ldapServiceImpl.getAllLdapRoles();
        Assert.assertEquals(0, results.size());
    }
}
