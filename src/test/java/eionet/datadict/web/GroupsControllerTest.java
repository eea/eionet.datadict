package eionet.datadict.web;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.web.viewmodel.GroupDetails;
import eionet.meta.DDUser;
import eionet.meta.dao.LdapDaoException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static eionet.util.SecurityUtil.REMOTEUSER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class GroupsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AclService aclService;

    @Mock
    private LdapService ldapService;

    @Spy
    @InjectMocks
    private GroupsController groupsController;

    private DDUser user;
    private MockHttpSession session;
    private List<LdapRole> ldapRoles;
    private LdapRole ldapRole;
    private Hashtable<String, Vector<String>> groupsAndUsers;
    private ArrayList<String> roles;
    private GroupDetails groupDetails;
    private static final String ACL_GROUP = "dd_admin";
    private static final String TEST_USER = "testUser";
    private static final String TEST_ROLE = "testRole";

    @Before
    public void setUp() throws LdapDaoException, AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        MockitoAnnotations.initMocks(this);
        user = mock(DDUser.class);
        when(user.getUserName()).thenReturn(TEST_USER);
        setSession();
        setLdapRoles();
        setGroupsAndUsers();
        setRoleNames();
        setGroupDetails();
        UserUtils.setDdGroupsAndUsers(groupsAndUsers);
        when(ldapService.getUserLdapRoles(anyString())).thenReturn(ldapRoles);
        when(ldapService.getAllLdapRoles()).thenReturn(ldapRoles);
        when(user.isAuthentic()).thenReturn(true);
        when(user.hasPermission(anyString(), anyString())).thenReturn(true);
        Mockito.doNothing().when(groupsController).refreshUserGroupResults(any(HttpServletRequest.class));
        mockMvc = MockMvcBuilders.standaloneSetup(groupsController).build();
    }

    private void setGroupDetails() {
        groupDetails = new GroupDetails();
        groupDetails.setLdapGroupName("testRole");
    }

    private void setRoleNames() {
        roles = new ArrayList<>();
        roles.add(TEST_ROLE);
    }

    void setGroupsAndUsers() {
        groupsAndUsers = new Hashtable<>();
        Vector<String> vector = new Vector<>();
        vector.add(TEST_USER);
        groupsAndUsers.put(ACL_GROUP, vector);
    }

    void setSession() {
        session = new MockHttpSession();
        session.setAttribute(REMOTEUSER, user);
    }

    void setLdapRoles() {
        ldapRoles = new ArrayList<>();
        ldapRole = new LdapRole();
        ldapRole.setName(TEST_ROLE);
        ldapRoles.add(ldapRole);
    }

    @Test
    public void testGetGroupsAndUsersSuccess() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list")
                .session(session);
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("groupsAndUsers"));
    }

    @Test
    public void testGetGroupsAndUsersUserNotLoggedIn() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list");
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("message"));
    }

    @Test
    public void testGetGroupsAndUsersNoPermission() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list")
                .session(session);
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("message"));
    }

    @Test
    public void testGetLdapListSuccess() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/ldapOptions")
                .param("term", "test");
        mockMvc.perform(builder)
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllLdapRolesSuccess() throws LdapDaoException {
        List<String> result = groupsController.getAllLdapRoles();
        assertEquals(roles, result);
    }

    @Test
    public void testAddUserSuccess() throws Exception {
        when(ldapService.getAllLdapRoles()).thenReturn(ldapRoles);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testAddUserNoPermission() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(view().name("message"));
    }

    @Test
    public void testAddUserGroupNotExist() throws Exception {
        groupDetails.setLdapGroupName("test");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(view().name("message"));
    }

    @Test
    public void testRemoveUserSuccess() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/removeUser")
                .session(session).param("ddGroupName", ACL_GROUP).param("memberName", "test");
        mockMvc.perform(builder)
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testRemoveUserNoPermission() throws Exception {
        when(user.hasPermission(anyString(), anyString())).thenReturn(false);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/removeUser")
                .session(session).param("ddGroupName", ACL_GROUP).param("memberName", "test");
        mockMvc.perform(builder)
                .andExpect(view().name("message"));
    }
}
