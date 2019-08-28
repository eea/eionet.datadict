package eionet.web.action;

import eionet.datadict.services.data.CleanupService;
import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.util.SecurityUtil;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class CleanupActionBeanTestIT {
    
    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final CleanupService cleanupService;
        private final ErrorPageService errorPageService;

        public DependencyInjector(CleanupService cleanupService, ErrorPageService errorPageService) {
            this.cleanupService = cleanupService;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof CleanupActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            CleanupActionBean actionBean = (CleanupActionBean) bean;
            actionBean.setCleanupService(cleanupService);
            actionBean.setErrorPageService(errorPageService);
        }

    }

    @Spy
    private ErrorPageServiceImpl errorPageService;

    @Mock
    private CleanupService cleanupService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new CleanupActionBeanTestIT.DependencyInjector(cleanupService, errorPageService);
    }

    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }

    @Test
    public void testFailToViewForNonAuthenticatedUser() throws Exception {
        testFailForNonAuthenticatedUser("view");
    }

    @Test
    public void testFailToCleanupForNonAuthenticatedUser() throws Exception {
        testFailForNonAuthenticatedUser("cleanup");
    }

    private void testFailForNonAuthenticatedUser(String eventName) throws Exception {
        MockRoundtrip trip = this.createRoundtrip();
        trip.execute(eventName);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test
    public void testFailToViewForNonAuthorizedUser() throws Exception {
        testFailForNonAuthorizedUser("view");
    }

    @Test
    public void testFailToCleanupForNonAuthorizedUser() throws Exception {
        testFailForNonAuthorizedUser("cleanup");
    }

    private void testFailForNonAuthorizedUser(String eventName) throws Exception {
        MockRoundtrip trip = this.createAuthenticatedRoundtrip("nonAuthorizedUser");
        trip.execute(eventName);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.FORBIDDEN_403), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test 
    public  void testView() throws Exception {
        MockRoundtrip trip = this.createAuthenticatedRoundtrip("testUser");
        trip.execute("view");

        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        assertEquals(CleanupActionBean.CLEANUP_PAGE, trip.getForwardUrl());
    }

    @Test 
    public  void testCleanup() throws Exception {
        MockRoundtrip trip = this.createAuthenticatedRoundtrip("testUser");
        trip.execute("cleanup");

        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        verify(cleanupService, times(1)).deleteBrokenDatasetToTableRelations();
        verify(cleanupService, times(1)).deleteOrphanTables();
        verify(cleanupService, times(1)).deleteBrokenTableToElementRelations();
        verify(cleanupService, times(1)).deleteOrphanNonCommonDataElements();
        verify(cleanupService, times(1)).deleteOrphanNamespaces();
        verify(cleanupService, times(1)).deleteOrphanAcls();
        assertTrue(trip.getRedirectUrl().contains("cleanup"));
    }

    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, CleanupActionBean.class);

        return trip;
    }

    private MockRoundtrip createAuthenticatedRoundtrip(String userNameAndPassword) {
        MockRoundtrip trip = this.createRoundtrip();
        DDUser user = new FakeUser();
        user.authenticate(userNameAndPassword, userNameAndPassword);
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        return trip;
    }

}
