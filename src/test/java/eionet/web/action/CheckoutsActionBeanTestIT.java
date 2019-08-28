package eionet.web.action;

import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.datadict.services.data.CheckoutsService;
import eionet.util.SecurityUtil;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import java.util.Collections;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


public class CheckoutsActionBeanTestIT {

    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final CheckoutsService checkoutsService;
        private final ErrorPageService errorPageService;

        public DependencyInjector(CheckoutsService checkoutsService, ErrorPageService errorPageService) {
            this.checkoutsService = checkoutsService;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof CheckoutsActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            CheckoutsActionBean actionBean = (CheckoutsActionBean) bean;
            actionBean.setCheckoutsService(checkoutsService);
            actionBean.setErrorPageService(errorPageService);
        }

    }

    @Spy
    private ErrorPageServiceImpl errorPageService;

    @Mock
    private CheckoutsService checkoutsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new CheckoutsActionBeanTestIT.DependencyInjector(checkoutsService, errorPageService);
    }

    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }

    @Test
    public void testFailToViewBecauseOfNonAuthenticatedUser() throws Exception {
        MockRoundtrip trip = this.createRoundtrip();
        trip.execute("view");
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test 
    public  void testView() throws Exception {
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");

        when(checkoutsService.getDataSetsWorkingCopies(user.getUserName())).thenReturn(Collections.EMPTY_LIST);
        when(checkoutsService.getCommonDataElementsWorkingCopies(user.getUserName())).thenReturn(Collections.EMPTY_LIST);
        when(checkoutsService.getSchemaSetsWorkingCopies(user.getUserName())).thenReturn(Collections.EMPTY_LIST);
        when(checkoutsService.getSchemasWorkingCopies(user.getUserName())).thenReturn(Collections.EMPTY_LIST);
        when(checkoutsService.getVocabulariesWorkingCopies(user.getUserName())).thenReturn(Collections.EMPTY_LIST);

        MockRoundtrip trip = this.createRoundtrip();
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        trip.execute("view");

        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        verify(checkoutsService, times(1)).getDataSetsWorkingCopies(user.getUserName());
        verify(checkoutsService, times(1)).getCommonDataElementsWorkingCopies(user.getUserName());
        verify(checkoutsService, times(1)).getSchemaSetsWorkingCopies(user.getUserName());
        verify(checkoutsService, times(1)).getSchemasWorkingCopies(user.getUserName());
        verify(checkoutsService, times(1)).getVocabulariesWorkingCopies(user.getUserName());
        assertEquals(CheckoutsActionBean.CHECKOUTS_PAGE, trip.getForwardUrl());
    }

    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, CheckoutsActionBean.class);

        return trip;
    }

}
