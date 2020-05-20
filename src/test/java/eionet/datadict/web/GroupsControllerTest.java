package eionet.datadict.web;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.acl.AclOperationsService;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.web.viewmodel.GroupDetails;
import eionet.meta.DDUser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static eionet.util.SecurityUtil.REMOTEUSER;
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
    private AclOperationsService aclOperationsService;

    @Mock
    private LdapService ldapService;

    @InjectMocks
    GroupsController groupsController;

    DDUser user;
    MockHttpSession session;
    List<LdapRole> ldapRoles;
    LdapRole ldapRole;
    Hashtable<String, Vector<String>> groupsAndUsers;

    @Before
    public void setUp() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        MockitoAnnotations.initMocks(this);
        this.groupsController = new GroupsController(aclService, aclOperationsService, ldapService);
        user = mock(DDUser.class);
        when(groupsController.getGroupsAndUsers()).thenReturn(groupsAndUsers);
        when(ldapService.getUserLdapRoles(anyString())).thenReturn(ldapRoles);
        when(user.isAuthentic()).thenReturn(true);
        when(user.hasPermission(anyString(), anyString())).thenReturn(true);
        setSession();
        setLdapRoles();
        setGroupsAndUsers();
        mockMvc = MockMvcBuilders.standaloneSetup(groupsController).build();
    }

    void setGroupsAndUsers() {
        groupsAndUsers = new Hashtable<>();
        Vector<String> vector = new Vector<>();
        groupsAndUsers.put("key", vector);
    }

    void setSession() {
        session = new MockHttpSession();
        session.setAttribute(REMOTEUSER, user);
    }

    void setLdapRoles() {
        ldapRoles = new ArrayList<>();
        ldapRole = new LdapRole();
        ldapRole.setName("testRole");
        ldapRoles.add(ldapRole);
    }

    @Test
    public void getGroupsAndUsersTest() throws Exception {
        when(groupsController.getGroupsAndUsers()).thenReturn(groupsAndUsers);
        when(ldapService.getUserLdapRoles(anyString())).thenReturn(ldapRoles);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/list")
                .session(session);
        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(view().name("groupsAndUsers"));
    }

    @Test
    public void getLdapListTest() throws Exception {
        when(ldapService.getAllLdapRoles()).thenReturn(ldapRoles);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/ldapOptions")
                .param("term", "test");
        mockMvc.perform(builder)
                .andExpect(status().isOk());
    }

    @Test
    public void addUserTest() throws Exception {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setLdapGroupName("testRole");
        when(ldapService.getAllLdapRoles()).thenReturn(ldapRoles);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/admintools/addUser")
                .session(session).flashAttr("groupDetails", groupDetails);
        mockMvc.perform(builder)
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void removeUserTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admintools/removeUser")
                .session(session).param("ddGroupName", "testG").param("memberName", "test");
        mockMvc.perform(builder)
                .andExpect(status().is3xxRedirection());
    }
}
