package eionet.datadict.controllers;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.NamespaceDataService;
import eionet.datadict.services.data.RdfNamespaceDataService;
import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.dao.domain.FixedValue;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import java.util.ArrayList;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;


public class AttributeActionBeanTest {
    private static class DependencyInjector implements ActionBeanDependencyInjector {
        
        private final AttributeActionBean2 actionBean;
        
        public DependencyInjector(AttributeActionBean2 actionBean){ 
            this.actionBean = actionBean;
        }
        
        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof AttributeActionBean2;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            
        }

        @Override
        public boolean shouldReplaceActionBean() {
            return true;
        }

        @Override
        public ActionBean getStubActionBeanFromExecutionContextActionBean(ActionBean bean) {
           AttributeActionBean2 oldActionBean = (AttributeActionBean2)bean;
           actionBean.setAttribute(oldActionBean.getAttribute());
           actionBean.setNamespaces(oldActionBean.getNamespaces());
           actionBean.setRdfNamespaces(oldActionBean.getRdfNamespaces());
           actionBean.setContext(bean.getContext());
           return actionBean;
        }
        
    }
    @Mock
    AclService aclService;
    
    @Mock
    AttributeService attributeService;
    
    @Mock
    RdfNamespaceDataService rdfNamespaceDataService;
    
    @Mock
    AttributeDataService attributeDataService;
    
    @Mock
    NamespaceDataService namespaceDataService;
    
    @Spy
    Attribute attribute;    
  
    @Mock
    DDUser user;
    
    @Spy
    @InjectMocks
    AttributeActionBean2 actionBean;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = 
                new DependencyInjector(actionBean);//, aclService, attributeService, rdfNamespaceDataService, attributeDataService, namespaceDataService, attribute);
    }
    
    @Test
    public void testView() throws ResourceNotFoundException, UserAuthenticationException, UserAuthorizationException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(1).when(attribute).getId();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        Mockito.doReturn(new ArrayList<FixedValue>()).when(attributeDataService).getFixedValues(anyInt());
        
        actionBean.setAttribute(attribute);
        Resolution resolution = actionBean.view();
        
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s1",Permission.VIEW);
        Mockito.verify(attributeDataService).getFixedValues(1);
        Mockito.verify(attributeDataService).getAttribute(1);
        assertNotNull(resolution);
        assertEquals(resolution.getClass(), ForwardResolution.class);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testViewNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        Mockito.doReturn(null).when(actionBean).getUser();
        actionBean.view();
    }
    
    
    @Test(expected = UserAuthorizationException.class)
    public void testViewNotAuthorizedUser() throws BadRequestException, UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(1).when(attribute).getId();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s1", Permission.VIEW);
        actionBean.setAttribute(attribute);
        actionBean.view();
    }
    
    @Test(expected = BadRequestException.class)
    public void testViewBadRequest() throws UserAuthenticationException, BadRequestException, ResourceNotFoundException, UserAuthorizationException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(null).when(attribute).getId();
        actionBean.setAttribute(attribute);
        actionBean.view();
    }
    
    @Test
    public void testAdd() throws UserAuthenticationException, UserAuthorizationException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), any(Permission.class));
        
        Resolution resolution = actionBean.add();
        
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT);
        Mockito.verify(namespaceDataService).getAttributeNamespaces();
        Mockito.verify(rdfNamespaceDataService).getRdfNamespaces();
        assertNotNull(resolution);
        assertEquals(resolution.getClass(), ForwardResolution.class);
        assertNotNull(actionBean.getAttribute());
        assertEquals(Attribute.ValueInheritanceMode.NONE, actionBean.getAttribute().getValueInheritanceMode());
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testAddNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException {
        Mockito.doReturn(null).when(actionBean).getUser();
        actionBean.add();
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testAddNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), any(Permission.class));
        actionBean.add();
    }
    
    @Test 
    public void testEdit() throws ResourceNotFoundException, UserAuthenticationException, UserAuthorizationException, BadRequestException{
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        Mockito.doReturn(null).when(actionBean).getRequestParameter(anyString());
        Resolution resolution = actionBean.edit();
        assertNotNull(resolution);
        assertEquals(ForwardResolution.class, resolution.getClass());
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s0", Permission.UPDATE);
        Mockito.verify(attributeDataService).getAttribute(0);
        Mockito.verify(namespaceDataService).getAttributeNamespaces();
        Mockito.verify(rdfNamespaceDataService).getRdfNamespaces();
    }
    
    @Test
    public void testEditForNewVocabularyId() throws ResourceNotFoundException, UserAuthenticationException, UserAuthorizationException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        Mockito.doReturn("1").when(actionBean).getRequestParameter(anyString());
        Mockito.doReturn(attribute).when(attributeDataService).setNewVocabularyToAttributeObject(any(Attribute.class), anyInt());
        Resolution resolution = actionBean.edit();
        assertNotNull(resolution);
        assertEquals(ForwardResolution.class, resolution.getClass());
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s0", Permission.UPDATE);
        Mockito.verify(attributeDataService).getAttribute(0);
        Mockito.verify(attributeDataService).setNewVocabularyToAttributeObject(attribute, Integer.parseInt("1"));
    } 
    
    @Test(expected = UserAuthenticationException.class)
    public void testEditNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        Mockito.doReturn(null).when(actionBean).getUser();
        actionBean.edit();
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testEditNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        actionBean.edit();
    }
    
    @Test(expected = BadRequestException.class)
    public void testEditBadRequest() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(null).when(attribute).getId();
        actionBean.edit();
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testEditAttributeNotFound() throws ResourceNotFoundException, UserAuthenticationException, UserAuthorizationException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(1).when(attribute).getId();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doThrow(ResourceNotFoundException.class).when(attributeDataService).getAttribute(anyInt());
        actionBean.edit();
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testEditNewVocabularyNotFound() throws ResourceNotFoundException, UserAuthenticationException, UserAuthorizationException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        Mockito.doReturn("1").when(actionBean).getRequestParameter(anyString());
        Mockito.doThrow(ResourceNotFoundException.class).when(attributeDataService).setNewVocabularyToAttributeObject(any(Attribute.class), anyInt());
        Resolution resolution = actionBean.edit();
        assertNotNull(resolution);
        assertEquals(ForwardResolution.class, resolution.getClass());
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s0", Permission.UPDATE);
        Mockito.verify(attributeDataService).getAttribute(0);
        Mockito.verify(attributeDataService).setNewVocabularyToAttributeObject(attribute, Integer.parseInt("1"));
    } 
    
    @Test
    public void testSave() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attributeService).save(any(Attribute.class), any(DDUser.class));
        Resolution resolution = actionBean.save();
        assertNotNull(resolution);
        assertEquals(resolution.getClass(), RedirectResolution.class);
        Mockito.verify(attributeService).save(attribute, user);
        Mockito.verify(namespaceDataService).getAttributeNamespaces();
        Mockito.verify(rdfNamespaceDataService).getRdfNamespaces();
    }
   
    @Test
    public void testDelete() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(attributeDataService).exists(0);
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doNothing().when(attributeService).delete(anyInt(), any(DDUser.class));
        Resolution resolution = actionBean.delete();
        assertNotNull(resolution);
        assertEquals(ForwardResolution.class, resolution.getClass());
        Mockito.verify(attributeService).delete(0, user);
    }
    
    @Test (expected = ResourceNotFoundException.class)
    public void testDeleteResourceNotFound() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.FALSE).when(attributeDataService).exists(anyInt());
        Mockito.doReturn(0).when(attribute).getId();
        actionBean.delete();
    }
    
    @Test
    public void testConfirmDeleteWithNoDependencies() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(Boolean.TRUE).when(attributeDataService).exists(anyInt());
        Mockito.doReturn(0).when(attributeDataService).countAttributeValues(0);
        Mockito.doReturn(null).when(actionBean).delete();
        actionBean.confirmDelete();
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s0", Permission.DELETE);
        Mockito.verify(attributeDataService).exists(0);
        Mockito.verify(attributeDataService).countAttributeValues(0);
        Mockito.verify(actionBean).delete();
    }
    
    @Test
    public void testConfirmDeleteWithDependencies() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(Boolean.TRUE).when(attributeDataService).exists(anyInt());
        Mockito.doReturn(1).when(attributeDataService).countAttributeValues(0);
        Mockito.doReturn(null).when(attributeDataService).getDistinctTypesWithAttributeValues(0);
        Resolution resolution = actionBean.confirmDelete();
        assertNotNull(resolution);
        assertEquals(ForwardResolution.class, resolution.getClass());
        Mockito.verify(actionBean, times(0)).delete();     
    }
    
    @Test (expected = UserAuthenticationException.class)
    public void testConfirmDeleteNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(null).when(actionBean).getUser();
        actionBean.confirmDelete();
    }
    
    @Test (expected = UserAuthorizationException.class)
    public void testConfirmDeleteNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        actionBean.confirmDelete();
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testConfirmDeleteResourceNotFound() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(Boolean.FALSE).when(attributeDataService).exists(0);
        actionBean.confirmDelete();
    }
    
    @Test
    public void testReset() {
        Mockito.doReturn(0).when(attribute).getId();
        Resolution resolution = actionBean.reset();
        assertNotNull(resolution);
        assertEquals(RedirectResolution.class, resolution.getClass());
    }
    
    @Test
    public void testRemoveVocabularyBinding() throws ResourceNotFoundException, UserAuthenticationException, UserAuthorizationException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(0);
        Resolution resolution = actionBean.removeVocabularyBinding();
        assertNotNull(resolution);
        assertEquals(ForwardResolution.class, resolution.getClass());
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s0", Permission.UPDATE);
        Mockito.verify(attribute).setVocabulary(null);
        Mockito.verify(namespaceDataService).getAttributeNamespaces();
        Mockito.verify(rdfNamespaceDataService).getRdfNamespaces();
    }
    
    @Test (expected = UserAuthenticationException.class)
    public void testRemoveVocabularyBindingNotAuthenticatedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(null).when(actionBean).getUser();
        actionBean.removeVocabularyBinding();
    }
    
    @Test (expected = UserAuthorizationException.class)
    public void testRemoveVocabularyBindintNotAuthorizedUser() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(Boolean.FALSE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        actionBean.removeVocabularyBinding();
    }
    
    @Test
    public void testViewRoundTrip() throws ResourceNotFoundException, Exception {
        MockRoundtrip trip = createRoundtrip();
        trip.setParameter("attribute.id", "1");
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        Mockito.doReturn(1).when(attribute).getId();
        Mockito.doReturn(new ArrayList<FixedValue>()).when(attributeDataService).getFixedValues(anyInt());

        trip.execute("view");
    
        Mockito.verify(aclService).hasPermission(user, AclEntity.ATTRIBUTE, "s1",Permission.VIEW);
        Mockito.verify(attributeDataService).getFixedValues(1);
        Mockito.verify(attributeDataService).getAttribute(1);
        assertTrue(trip.getDestination().contains("viewAttribute.jsp"));
    }
    
    @Test
    public void testEditRoundTripWithVocabularyId() throws ResourceNotFoundException, Exception {
        MockRoundtrip trip = createRoundtrip();
        trip.setParameter("attribute.id", "1");
        trip.setParameter("vocabularyId", "1");
        
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        
        trip.execute("edit");
        
        Mockito.verify(attributeDataService).setNewVocabularyToAttributeObject(attribute, 1); 
    }
    
    @Test
    public void testEditRoundTripWithOutVocabularyId() throws ResourceNotFoundException, Exception {
        MockRoundtrip trip = createRoundtrip();
        trip.setParameter("attribute.id", "1");
        
        Mockito.doReturn(user).when(actionBean).getUser();
        Mockito.doReturn(Boolean.TRUE).when(aclService).hasPermission(any(DDUser.class), any(AclEntity.class), anyString(), any(Permission.class));
        Mockito.doReturn(attribute).when(attributeDataService).getAttribute(anyInt());
        
        trip.execute("edit");
        
        Mockito.verify(attributeDataService, times(0)).setNewVocabularyToAttributeObject(attribute, 1); 
    }
     
    
    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, AttributeActionBean2.class);
        
        return trip;
    }

}
