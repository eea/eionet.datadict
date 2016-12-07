package eionet.datadict.services.acl.impl;

import eionet.datadict.services.acl.impl.AclServiceImpl;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.Permission;
import eionet.meta.DDUser;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.junit.Assert.assertFalse;


public class AclServiceTest {
   
    @Mock
    DDUser user;
    
    @Spy
    AclServiceImpl aclService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testHasPermission() throws Exception {
        Mockito.doReturn("a_name_for_user_with_no_permission").when(user).getUserName();
        assertFalse(aclService.hasPermission(user, AclEntity.ATTRIBUTE, Permission.DELETE));
    }
    
    @Test
    public void testHasPermissionWithEntityId() throws Exception {
        Mockito.doReturn("a_name_for_user_with_no_permission").when(user).getUserName();
        assertFalse(aclService.hasPermission(user, AclEntity.ATTRIBUTE, "id", Permission.DELETE));
    }
    
    @Test
    public void testGetUserName(){
        Mockito.doReturn("name").when(user).getUserName();
        assertEquals("name", aclService.getUserName(user));
        assertEquals("", aclService.getUserName(null));
    }
    
    @Test
    public void testGetEntityPath() {
        assertEquals("/attributes/id", aclService.getEntityPath(AclEntity.ATTRIBUTE, "id"));
    }
}
