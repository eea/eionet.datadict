package eionet.datadict.services.acl.impl;

import eionet.datadict.services.acl.impl.AclServiceImpl;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.Permission;
import eionet.meta.DDUser;
import eionet.meta.service.DBUnitHelper;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.junit.Assert.assertFalse;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;


@SpringApplicationContext("mock-spring-context.xml")
public class AclServiceTestIT extends UnitilsJUnit4 {
   
    @Mock
    DDUser user;
    
    @Spy
    AclServiceImpl aclService;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        DBUnitHelper.loadData("seed-acldata.xml");
    }
    
    @After
    public void delete() throws Exception {
        DBUnitHelper.deleteData("seed-acldata.xml");
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
