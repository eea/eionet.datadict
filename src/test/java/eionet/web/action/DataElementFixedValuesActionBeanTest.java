package eionet.web.action;

import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.meta.ActionBeanUtils;
import eionet.meta.application.AppContextProvider;
import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.util.CompoundDataObject;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.fixedvalues.DataElementFixedValuesViewModelBuilder;
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
public class DataElementFixedValuesActionBeanTest {
    
    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final DataElementFixedValuesController controller;
        private final DataElementFixedValuesViewModelBuilder viewModelBuilder;
        private final ErrorPageService errorPageService;

        public DependencyInjector(DataElementFixedValuesController controller, 
                DataElementFixedValuesViewModelBuilder viewModelBuilder, ErrorPageService errorPageService) {
            this.controller = controller;
            this.viewModelBuilder = viewModelBuilder;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof DataElementFixedValuesActionBean;
        }
        
        @Override
        public void injectDependencies(ActionBean bean) {
            DataElementFixedValuesActionBean actionBean = (DataElementFixedValuesActionBean) bean;
            actionBean.setController(controller);
            actionBean.setViewModelBuilder(viewModelBuilder);
            actionBean.setErrorPageService(errorPageService);
        }
        
    }
    
    @Spy
    private ErrorPageServiceImpl errorPageService;
    
    @Mock
    private DataElementFixedValuesController controller;
    
    @Spy
    private DataElementFixedValuesViewModelBuilder viewModelBuilder;
    
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
        when(controller.getAllValuesModel(any(AppContextProvider.class), eq(ownerId), eq(isEditView))).thenReturn(controllerResult);
        doReturn(viewModel).when(viewModelBuilder).buildFromAllValuesModel(controllerResult, isEditView);
        trip.execute("view");
        verify(controller, times(1)).getAllValuesModel(any(AppContextProvider.class), eq(ownerId), eq(isEditView));
        verify(viewModelBuilder, times(1)).buildFromAllValuesModel(controllerResult, isEditView);
        DataElementFixedValuesActionBean actionBean = trip.getActionBean(DataElementFixedValuesActionBean.class);
        assertEquals(viewModel, actionBean.getViewModel());
        assertEquals(DataElementFixedValuesActionBean.PAGE_FIXED_VALUES_VIEW, trip.getForwardUrl());
    }
    
    @Test
    public void testFailToViewAllValuesBecauseOfMalformedOwnerId() throws Exception {
        MockRoundtrip trip = this.prepareRoundTrip("5a");
        trip.execute("view");
        verify(controller, times(0)).getAllValuesModel(any(AppContextProvider.class), any(Integer.class), any(Boolean.class));
        DataElementFixedValuesActionBean actionBean = trip.getActionBean(DataElementFixedValuesActionBean.class);
        assertNull(actionBean.getViewModel());
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
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
    
    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, DataElementFixedValuesActionBean.class);
        
        return trip;
    }
}
