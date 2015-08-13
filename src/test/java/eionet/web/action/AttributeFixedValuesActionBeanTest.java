package eionet.web.action;

import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.meta.ActionBeanUtils;
import eionet.meta.application.AppContextProvider;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.fixedvalues.AttributeFixedValuesViewModelBuilder;
import eionet.web.action.fixedvalues.FixedValuesViewModel;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.mockito.Mockito.*;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class AttributeFixedValuesActionBeanTest {
    
    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final AttributeFixedValuesController controller;
        private final AttributeFixedValuesViewModelBuilder viewModelBuilder;
        private final ErrorPageService errorPageService;

        public DependencyInjector(AttributeFixedValuesController controller, 
                AttributeFixedValuesViewModelBuilder viewModelBuilder, ErrorPageService errorPageService) {
            this.controller = controller;
            this.viewModelBuilder = viewModelBuilder;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof AttributeFixedValuesActionBean;
        }
        
        @Override
        public void injectDependencies(ActionBean bean) {
            AttributeFixedValuesActionBean actionBean = (AttributeFixedValuesActionBean) bean;
            actionBean.setController(controller);
            actionBean.setViewModelBuilder(viewModelBuilder);
            actionBean.setErrorPageService(errorPageService);
        }
        
    }
    
    @Spy
    private ErrorPageServiceImpl errorPageService;
    
    @Mock
    private AttributeFixedValuesController controller;
    
    @Spy
    private AttributeFixedValuesViewModelBuilder viewModelBuilder;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new DependencyInjector(controller, viewModelBuilder, errorPageService);
    }
    
    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }
    
    @Test
    public void testViewAllValues() throws Exception {
        final int ownerId = 5;
        final boolean isEditView = false;
        final CompoundDataObject controllerResult = new CompoundDataObject();
        final FixedValuesViewModel viewModel = new FixedValuesViewModel();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenReturn(controllerResult);
        doReturn(viewModel).when(viewModelBuilder).buildFromAllValuesModel(controllerResult, isEditView);
        trip.execute("view");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(1)).buildFromAllValuesModel(controllerResult, isEditView);
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertEquals(viewModel, actionBean.getViewModel());
        assertEquals(AttributeFixedValuesActionBean.PAGE_FIXED_VALUES_VIEW, trip.getForwardUrl());
    }
    
    @Test
    public void testFailToViewAllValuesBecauseOfMalformedOwnerId() throws Exception {
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip("5a");
        trip.execute("view");
        verify(controller, times(0)).getAllValuesModel(any(AppContextProvider.class), any(Integer.class));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewAllValuesBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenThrow(UserAuthenticationException.class);
        trip.execute("view");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewAllValuesBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenThrow(new FixedValueOwnerNotFoundException(ownerId));
        trip.execute("view");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewAllValuesBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenThrow(NotAFixedValueOwnerException.class);
        trip.execute("view");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testEditAllValues() throws Exception {
        final int ownerId = 5;
        final boolean isEditView = true;
        final CompoundDataObject controllerResult = new CompoundDataObject();
        final FixedValuesViewModel viewModel = new FixedValuesViewModel();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenReturn(controllerResult);
        doReturn(viewModel).when(viewModelBuilder).buildFromAllValuesModel(controllerResult, isEditView);
        trip.execute("edit");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(1)).buildFromAllValuesModel(controllerResult, isEditView);
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertEquals(viewModel, actionBean.getViewModel());
        assertEquals(AttributeFixedValuesActionBean.PAGE_FIXED_VALUES_EDIT, trip.getForwardUrl());
    }
    
    @Test
    public void testFailToEditAllValuesBecauseOfMalformedOwnerId() throws Exception {
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip("5a");
        trip.execute("edit");
        verify(controller, times(0)).getAllValuesModel(any(AppContextProvider.class), any(Integer.class));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditAllValuesBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenThrow(UserAuthenticationException.class);
        trip.execute("edit");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditAllValuesBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenThrow(new FixedValueOwnerNotFoundException(ownerId));
        trip.execute("edit");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditAllValuesBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId))).thenThrow(NotAFixedValueOwnerException.class);
        trip.execute("edit");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromAllValuesModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testDeleteAllValues() throws Exception {
        final int ownerId = 5;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        String redirectUrl = this.composeEditPageRedirectUrl(ownerId);
        assertTrue(trip.getRedirectUrl().endsWith(redirectUrl));
    }
    
    @Test
    public void testFailToDeleteAllValuesBecauseOfMalformedOwnerId() throws Exception {
        MockRoundtrip trip = this.prepareRoundTrip("5a");
        trip.execute("delete");
        verify(controller, times(0)).deleteFixedValues(any(AppContextProvider.class), any(Integer.class));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteAllValuesBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        doThrow(UserAuthenticationException.class).when(controller).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteAllValuesBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        doThrow(new FixedValueOwnerNotFoundException(ownerId)).when(controller).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteAllValuesBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        doThrow(NotAFixedValueOwnerException.class).when(controller).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValues(any(AppContextProvider.class), eq(ownerId));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testViewSingleValue() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final boolean isEditView = false;
        final CompoundDataObject controllerResult = new CompoundDataObject();
        final FixedValuesViewModel viewModel = new FixedValuesViewModel();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenReturn(controllerResult);
        doReturn(viewModel).when(viewModelBuilder).buildFromSingleValueModel(controllerResult, isEditView);
        trip.execute("view");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(1)).buildFromSingleValueModel(controllerResult, isEditView);
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertEquals(viewModel, actionBean.getViewModel());
        assertEquals(AttributeFixedValuesActionBean.PAGE_FIXED_VALUE_VIEW, trip.getForwardUrl());
    }
    
    @Test
    public void testFailToViewSingleValueBecauseOfMalformedOwnerId() throws Exception {
        final boolean isEditView = false;
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip("5a", value);
        trip.execute("view");
        verify(controller, times(0)).getSingleValueModel(any(AppContextProvider.class), any(Integer.class), eq(value));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewSingleValueBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(UserAuthenticationException.class);
        trip.execute("view");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewSingleValueBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        final String value = "val";
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(new FixedValueOwnerNotFoundException(ownerId));
        trip.execute("view");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewSingleValueBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        final String value = "val";
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(NotAFixedValueOwnerException.class);
        trip.execute("view");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToViewSingleValueBecauseOfValueNotFound() throws Exception {
        final int ownerId = 17;
        final String value = "val";
        final boolean isEditView = false;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(new FixedValueNotFoundException(value));
        trip.execute("view");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testEditSingleValue() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final boolean isEditView = true;
        final CompoundDataObject controllerResult = new CompoundDataObject();
        final FixedValuesViewModel viewModel = new FixedValuesViewModel();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenReturn(controllerResult);
        doReturn(viewModel).when(viewModelBuilder).buildFromSingleValueModel(controllerResult, isEditView);
        trip.execute("edit");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(1)).buildFromSingleValueModel(controllerResult, isEditView);
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertEquals(viewModel, actionBean.getViewModel());
        assertEquals(AttributeFixedValuesActionBean.PAGE_FIXED_VALUE_EDIT, trip.getForwardUrl());
    }
    
    @Test
    public void testFailToEditSingleValueBecauseOfMalformedOwnerId() throws Exception {
        final boolean isEditView = true;
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip("5a", value);
        trip.execute("edit");
        verify(controller, times(0)).getSingleValueModel(any(AppContextProvider.class), any(Integer.class), eq(value));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditSingleValueBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(UserAuthenticationException.class);
        trip.execute("edit");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditSingleValueBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        final String value = "val";
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(new FixedValueOwnerNotFoundException(ownerId));
        trip.execute("edit");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditSingleValueBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        final String value = "val";
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(NotAFixedValueOwnerException.class);
        trip.execute("edit");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToEditSingleValueBecauseOfValueNotFound() throws Exception {
        final int ownerId = 17;
        final String value = "val";
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        when(controller.getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value))).thenThrow(new FixedValueNotFoundException(value));
        trip.execute("edit");
        verify(controller, times(1)).getSingleValueModel(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(viewModelBuilder, times(0)).buildFromSingleValueModel(any(CompoundDataObject.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testDeleteSingleValue() throws Exception {
        final int ownerId = 5;
        final String value ="val";
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        String redirectUrl = this.composeEditPageRedirectUrl(ownerId);
        assertTrue(trip.getRedirectUrl().endsWith(redirectUrl));
    }
    
    @Test
    public void testFailToDeleteSingleValueBecauseOfMalformedOwnerId() throws Exception {
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip("5a", value);
        trip.execute("delete");
        verify(controller, times(0)).deleteFixedValue(any(AppContextProvider.class), any(Integer.class), eq(value));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteSingleValueBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        doThrow(UserAuthenticationException.class).when(controller).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteSingleValueBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        doThrow(new FixedValueOwnerNotFoundException(ownerId)).when(controller).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteSingleValueBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        doThrow(NotAFixedValueOwnerException.class).when(controller).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToDeleteSingleValueBecauseOfValueNotFound() throws Exception {
        final int ownerId = 15;
        final String value = "val";
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value);
        doThrow(new FixedValueNotFoundException(value)).when(controller).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        trip.execute("delete");
        verify(controller, times(1)).deleteFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testAddValue() throws Exception {
        final int ownerId = 5;
        final boolean isEditView = true;
        final SimpleAttribute controllerResult = new SimpleAttribute();
        final FixedValuesViewModel viewModel = new FixedValuesViewModel();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getOwnerAttribute(any(AppContextProvider.class), eq(ownerId))).thenReturn(controllerResult);
        doReturn(viewModel).when(viewModelBuilder).buildFromOwner(controllerResult, isEditView);
        trip.execute("add");
        verify(controller, times(1)).getOwnerAttribute(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(1)).buildFromOwner(controllerResult, isEditView);
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertEquals(viewModel, actionBean.getViewModel());
        assertEquals(AttributeFixedValuesActionBean.PAGE_FIXED_VALUE_EDIT, trip.getForwardUrl());
    }
    
    @Test
    public void testFailToAddValueBecauseOfMalformedOwnerId() throws Exception {
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip("5a");
        trip.execute("add");
        verify(controller, times(0)).getOwnerAttribute(any(AppContextProvider.class), any(Integer.class));
        AttributeFixedValuesActionBean actionBean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        verify(viewModelBuilder, times(0)).buildFromOwner(any(SimpleAttribute.class), eq(isEditView));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToAddValueBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getOwnerAttribute(any(AppContextProvider.class), eq(ownerId))).thenThrow(UserAuthenticationException.class);
        trip.execute("add");
        verify(controller, times(1)).getOwnerAttribute(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromOwner(any(SimpleAttribute.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToAddValueBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 1005;
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getOwnerAttribute(any(AppContextProvider.class), eq(ownerId))).thenThrow(new FixedValueOwnerNotFoundException(ownerId));
        trip.execute("add");
        verify(controller, times(1)).getOwnerAttribute(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromOwner(any(SimpleAttribute.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToAddValueBecauseOfNonOwner() throws Exception {
        final int ownerId = 17;
        final boolean isEditView = true;
        MockRoundtrip trip = this.prepareRoundTrip(ownerId);
        when(controller.getOwnerAttribute(any(AppContextProvider.class), eq(ownerId))).thenThrow(NotAFixedValueOwnerException.class);
        trip.execute("add");
        verify(controller, times(1)).getOwnerAttribute(any(AppContextProvider.class), eq(ownerId));
        verify(viewModelBuilder, times(0)).buildFromOwner(any(SimpleAttribute.class), eq(isEditView));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testSaveValue() throws Exception {
        final int ownerId = 17;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        trip.execute("save");
        AttributeFixedValuesActionBean bean = trip.getActionBean(AttributeFixedValuesActionBean.class);
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), eq(bean.getViewModel().getFixedValue()));
        String redirectUrl = this.composeEditPageRedirectUrl(ownerId);
        assertTrue(trip.getRedirectUrl().endsWith(redirectUrl));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfMalformedOwnerId() throws Exception {
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip("5a", value, savePayload);
        trip.execute("save");
        verify(controller, times(0)).saveFixedValue(any(AppContextProvider.class), any(Integer.class), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfAuthentication() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        doThrow(UserAuthenticationException.class).when(controller).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        trip.execute("save");
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfOwnerNotFound() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        doThrow(new FixedValueOwnerNotFoundException(ownerId)).when(controller).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        trip.execute("save");
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfNonOwner() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        doThrow(NotAFixedValueOwnerException.class).when(controller).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        trip.execute("save");
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfValueNotFound() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        doThrow(new FixedValueNotFoundException(value)).when(controller).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        trip.execute("save");
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfEmptyValue() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        doThrow(EmptyValueException.class).when(controller).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        trip.execute("save");
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    @Test
    public void testFailToSaveValueBecauseOfDuplicate() throws Exception {
        final int ownerId = 5;
        final String value = "val";
        final FixedValue savePayload = new FixedValue();
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, value, savePayload);
        doThrow(new DuplicateResourceException(value)).when(controller).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        trip.execute("save");
        verify(controller, times(1)).saveFixedValue(any(AppContextProvider.class), eq(ownerId), eq(value), any(FixedValue.class));
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }
    
    private MockRoundtrip prepareRoundTrip(int ownerId) {
        return this.prepareRoundTrip(ownerId, null);
    }
    
    private MockRoundtrip prepareRoundTrip(String ownerId) {
        return this.prepareRoundTrip(ownerId, null);
    }
    
    private MockRoundtrip prepareRoundTrip(int ownerId, String fixedValue) {
        return this.prepareRoundTrip(Integer.toString(ownerId), fixedValue);
    }
    
    private MockRoundtrip prepareRoundTrip(String ownerId, String fixedValue) {
        MockRoundtrip trip = this.createRoundtrip();
        trip.setParameter("ownerId", ownerId);
        
        if (fixedValue != null) {
            trip.setParameter("fixedValue", fixedValue);
        }
        
        return trip;
    }
    
    private MockRoundtrip prepareRoundTrip(int ownerId, String fixedValue, FixedValue savePayload) {
        return this.prepareRoundTrip(Integer.toString(ownerId), fixedValue, savePayload);
    }
    
    private MockRoundtrip prepareRoundTrip(String ownerId, String fixedValue, FixedValue savePayload) {
        MockRoundtrip trip = this.prepareRoundTrip(ownerId, fixedValue);
        trip.setParameter("viewModel.fixedValue.value", savePayload.getValue());
        trip.setParameter("viewModel.fixedValue.definition", savePayload.getDefinition());
        trip.setParameter("viewModel.fixedValue.shortDescription", savePayload.getShortDescription());
        trip.setParameter("viewModel.fixedValue.defaultValue", Boolean.toString(savePayload.isDefaultValue()));
        
        return trip;
    }
    
    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, AttributeFixedValuesActionBean.class);
        
        return trip;
    }
    
    private String composeEditPageRedirectUrl(int ownerId) {
        return String.format("/fixedvalues/attr/%d/edit", ownerId);
    }
    
}
