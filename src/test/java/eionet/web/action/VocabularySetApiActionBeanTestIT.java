package eionet.web.action;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.model.VocabularySet;
import eionet.datadict.services.auth.WebApiAuthInfoService;
import eionet.datadict.services.auth.WebApiAuthService;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.meta.ActionBeanUtils;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class VocabularySetApiActionBeanTestIT {

    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final WebApiAuthInfoService webApiAuthInfoService;
        private final WebApiAuthService webApiAuthService;
        private final VocabularyDataService vocabularyDataService;
        private final ErrorPageService errorPageService;

        public DependencyInjector(WebApiAuthInfoService webApiAuthInfoService, WebApiAuthService webApiAuthService, VocabularyDataService vocabularyDataService, ErrorPageService errorPageService) {
            this.webApiAuthInfoService = webApiAuthInfoService;
            this.webApiAuthService = webApiAuthService;
            this.vocabularyDataService = vocabularyDataService;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof VocabularySetApiActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            VocabularySetApiActionBean actionBean = (VocabularySetApiActionBean) bean;
            actionBean.setVocabularyDataService(vocabularyDataService);
            actionBean.setWebApiAuthInfoService(webApiAuthInfoService);
            actionBean.setWebApiAuthService(webApiAuthService);
            actionBean.setErrorPageService(errorPageService);
        }
        
    }

    @Mock
    private WebApiAuthInfoService webApiAuthInfoService;

    @Mock
    private WebApiAuthService webApiAuthService;

    @Mock
    private VocabularyDataService vocabularyDataService;

    @Spy
    private ErrorPageServiceImpl errorPageService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new DependencyInjector(webApiAuthInfoService, webApiAuthService, vocabularyDataService, errorPageService);
    }

    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }

    @Test
    public void testFailToCreateVocabularySetBecauseOfFaultyAuthentication() throws Exception {
        MockRoundtrip trip = this.createRoundtrip();
        when(this.webApiAuthService.authenticate(this.webApiAuthInfoService.getAuthenticationInfo(trip.getRequest()))).thenThrow(new UserAuthenticationException());
        trip.execute("createVocabularySet");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testFailToCreateVocabularySetBecauseOfEmptyParameter() throws EmptyParameterException, DuplicateResourceException, Exception {
        MockRoundtrip trip = this.createRoundtrip();
        when(this.vocabularyDataService.createVocabularySet(any(VocabularySet.class))).thenThrow(EmptyParameterException.class);
        trip.execute("createVocabularySet");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testFailToCreateVocabularySetBecauseOfDuplicateResource() throws EmptyParameterException, DuplicateResourceException, Exception {
        MockRoundtrip trip = this.createRoundtrip();
        when(this.vocabularyDataService.createVocabularySet(any(VocabularySet.class))).thenThrow(DuplicateResourceException.class);
        trip.execute("createVocabularySet");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.CONFLICT), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testCreateVocabularySetResult() throws EmptyParameterException, DuplicateResourceException, Exception {

        MockRoundtrip trip = this.createRoundtrip();
        VocabularySet vocabularySet = new VocabularySet();
        vocabularySet.setIdentifier("identifier");
        vocabularySet.setLabel("label");
        trip.setParameter("identifier", "identifier");
        trip.setParameter("label", "label");
        when(this.vocabularyDataService.createVocabularySet(any(VocabularySet.class))).thenReturn(vocabularySet);
        trip.execute("createVocabularySet");
        ArgumentCaptor<VocabularySet> toInsertCaptor = ArgumentCaptor.forClass(VocabularySet.class);
        verify(this.vocabularyDataService, times(1)).createVocabularySet(toInsertCaptor.capture());
        VocabularySet toBeInserted = toInsertCaptor.getValue();
        assertTrue(EqualsBuilder.reflectionEquals(vocabularySet, toBeInserted));
    }

    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularySetApiActionBean.class);
        return trip;
    }

}
