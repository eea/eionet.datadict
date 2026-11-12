package eionet.datadict.services.impl;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.Attribute.ObligationType;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.AttributeDataService;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;


public class AttributeServiceTest {

    @Mock
    Namespace namespace;
    
    @Mock
    Attribute attribute;
    
    @Mock
    AclService aclService;
    
    @Mock
    AttributeDataService attributeDataService;
   
    @Spy
    @InjectMocks
    AttributeServiceImpl attributeService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testSaveForCreation() throws UserAuthorizationException, BadRequestException, UserAuthenticationException{
        DDUser user = new DDUser("name", true);
        Mockito.doReturn(0).when(attributeService).saveWithCreate(any(Attribute.class), any(DDUser.class));
        Mockito.doReturn(null).when(attribute).getId();
        
        attributeService.save(attribute, user);
        
        Mockito.verify(attributeService, times(1)).saveWithCreate(attribute, user);
    }
    
    @Test
    public void testSaveForUpdate() throws UserAuthorizationException, BadRequestException, UserAuthenticationException {
        DDUser user = new DDUser("name", true);
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(0).when(attributeService).saveWithUpdate(attribute, user);
        
        attributeService.save(attribute, user);
        
        Mockito.verify(attributeService, times(1)).saveWithUpdate(attribute, user);
    }
    
    @Test
    public void testSaveWithCreate() throws BadRequestException, UserAuthorizationException {
        DDUser user = new DDUser();

        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT);
        Mockito.doNothing().when(aclService).grantAccess(any(DDUser.class), any(AclEntity.class), anyString(), anyString());
        Mockito.doNothing().when(attributeService).validateMandatoryAttributeFields(any(Attribute.class));
        Mockito.doReturn(1).when(attributeDataService).createAttribute(any(Attribute.class));
        
        attributeService.saveWithCreate(attribute, user);

        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT);
        Mockito.verify(attributeService, times(1)).validateMandatoryAttributeFields(attribute);
        Mockito.verify(aclService, times(1)).grantAccess(any(DDUser.class), any(AclEntity.class), anyString(), anyString());
        Mockito.verify(attributeDataService, times(1)).createAttribute(attribute);
    }

    @Test
    public void testSaveWithUpdate() throws BadRequestException, UserAuthorizationException {
        DDUser user = new DDUser();
        
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doNothing().when(attributeService).validateMandatoryAttributeFields(attribute);
        Mockito.doNothing().when(attributeDataService).updateAttribute(attribute);
        Mockito.doReturn(1).when(attribute).getId();
        
        attributeService.saveWithUpdate(attribute, user);
        
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s1", Permission.UPDATE);
        Mockito.verify(attributeService).validateMandatoryAttributeFields(attribute);
        Mockito.verify(attributeDataService).updateAttribute(attribute);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testSaveNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        attributeService.save(attribute, null);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testSaveCreateNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = new DDUser();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), any(Permission.class));
        Mockito.doReturn(null).when(attribute).getId();
        attributeService.save(attribute, user);
    } 
    
    @Test(expected = UserAuthorizationException.class)
    public void testSaveUpdateNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = new DDUser();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(1).when(attribute).getId();
        attributeService.save(attribute, user);
    }
        
    @Test(expected = BadRequestException.class)
    public void testSaveCreateMandatoryFieldMissing() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = new DDUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), any(Permission.class));
        Mockito.doReturn(null).when(attribute).getId();
        attributeService.save(attribute, user);
    }
          
    @Test(expected = BadRequestException.class)
    public void testSaveUpdateMandatoryFieldMissing() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = new DDUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(1).when(attribute).getId();
        attributeService.save(attribute, user);
    }
    
    @Test
    public void testValidateMandatoryFields() throws BadRequestException {
        Mockito.doReturn("shortName").when(attribute).getShortName();
        Mockito.doReturn("name").when(attribute).getName();
        Mockito.doReturn(namespace).when(attribute).getNamespace();
        Mockito.doReturn(1).when(namespace).getId();
        Mockito.doReturn(ObligationType.CONDITIONAL).when(attribute).getObligationType();
        attributeService.validateMandatoryAttributeFields(attribute);
    }
    
    @Test
    public void testDelete() throws UserAuthenticationException, UserAuthorizationException {
        DDUser user = new DDUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doNothing().when(attributeDataService).deleteAttributeById(anyInt());
        Mockito.doNothing().when(aclService).removeAccessRightsForDeletedEntity(any(AclEntity.class), anyString());
        
        attributeService.delete(0, user);
        
        Mockito.verify(aclService, times(1)).hasPermission(user, AclEntity.ATTRIBUTE, "s0", Permission.DELETE);
        Mockito.verify(attributeDataService, times(1)).deleteAttributeById(0);
        Mockito.verify(aclService, times(1)).removeAccessRightsForDeletedEntity(AclEntity.ATTRIBUTE, "s0");
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testDeleteNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException {
        attributeService.delete(9, null);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testDeleteNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException {
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        attributeService.delete(9, new DDUser());
    }
    
    @Test
    public void testgetAttributeAclId() {
        assertEquals("s1", attributeService.getAttributeAclId(1));
    }

}
