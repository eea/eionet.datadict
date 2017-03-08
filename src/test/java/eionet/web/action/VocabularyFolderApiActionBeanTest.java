package eionet.web.action;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.services.auth.WebApiAuthInfoService;
import eionet.datadict.services.auth.WebApiAuthService;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.After;
import static org.junit.Assert.assertEquals;
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
public class VocabularyFolderApiActionBeanTest {

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
            return bean instanceof VocabularyFolderApiActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            VocabularyFolderApiActionBean actionBean = (VocabularyFolderApiActionBean) bean;
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
    public void testFailToCreateVocabularyBecauseOfFaultyAuthentication() throws Exception {
        MockRoundtrip trip = this.createRoundtrip();
        when(this.webApiAuthService.authenticate(this.webApiAuthInfoService.getAuthenticationInfo(trip.getRequest()))).thenThrow(new UserAuthenticationException());
        trip.execute("createVocabulary");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testFailToCreateVocabularyBecauseOfEmptyParameter() throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException, Exception {
        final String vocabularySetIdentifier = "vocabularySetIdentifier";
        final String folderName = "someFolder";
        final boolean numericConceptIdentifier = true;
        final boolean NotationsEqualIdentifiers = true;
        MockRoundtrip trip = this.prepareRoundtrip(folderName, vocabularySetIdentifier, numericConceptIdentifier, NotationsEqualIdentifiers);
        when(this.vocabularyDataService.createVocabulary(any(String.class), any(VocabularyFolder.class), any(DDUser.class))).thenThrow(EmptyParameterException.class);
        trip.execute("createVocabulary");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testFailToCreateVocabularyBecauseOfResourceNotFound() throws EmptyParameterException, Exception {
        final String vocabularySetIdentifier = "vocabularySetIdentifier";
        final String folderName = "someFolder";
        final boolean numericConceptIdentifier = true;
        final boolean NotationsEqualIdentifiers = true;
        MockRoundtrip trip = this.prepareRoundtrip(folderName, vocabularySetIdentifier, numericConceptIdentifier, NotationsEqualIdentifiers);
        when(this.vocabularyDataService.createVocabulary(any(String.class), any(VocabularyFolder.class), any(DDUser.class))).thenThrow(ResourceNotFoundException.class);
        trip.execute("createVocabulary");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testFailToCreateVocabularyBecauseOfDouplicateResource() throws EmptyParameterException, Exception {
        final String vocabularySetIdentifier = "vocabularySetIdentifier";
        final String folderName = "someFolder";
        final boolean numericConceptIdentifier = true;
        final boolean NotationsEqualIdentifiers = true;
        MockRoundtrip trip = this.prepareRoundtrip(folderName, vocabularySetIdentifier, numericConceptIdentifier, NotationsEqualIdentifiers);
        when(this.vocabularyDataService.createVocabulary(any(String.class), any(VocabularyFolder.class), any(DDUser.class))).thenThrow(DuplicateResourceException.class);
        trip.execute("createVocabulary");
        verify(errorPageService, times(1)).createErrorResolutionWithoutRedirect(eq(ErrorActionBean.ErrorType.CONFLICT), any(String.class), eq(ErrorActionBean.RETURN_ERROR_EVENT));
    }

    @Test
    public void testCreateVocabularyResult() throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException, Exception {
        //vocabulary.FolderName = vocabularySetIdentifier
        final String Identifier = "Identifier";
        final String folderName = "someFolder";
        final boolean NumericConceptIdentifiers = true;
        final boolean NotationsEqualIdentifiers = true;
        final String label = "someLabel";
        final String baseUri = "/uri";
        VocabularyFolder vocabularyToBeInserted = new VocabularyFolder();
        vocabularyToBeInserted.setNotationsEqualIdentifiers(NotationsEqualIdentifiers);
        vocabularyToBeInserted.setNumericConceptIdentifiers(NumericConceptIdentifiers);
        // vocabularyToBeInserted.setFolderName(folderName);
        vocabularyToBeInserted.setIdentifier(Identifier);
        vocabularyToBeInserted.setLabel(label);
        vocabularyToBeInserted.setBaseUri(baseUri);
        MockRoundtrip trip = this.prepareRoundtrip(folderName, label, baseUri, Identifier, NumericConceptIdentifiers, NotationsEqualIdentifiers);
        when(this.vocabularyDataService.createVocabulary(any(String.class), any(VocabularyFolder.class), any(DDUser.class))).thenReturn(vocabularyToBeInserted);
        trip.execute("createVocabulary");
        ArgumentCaptor<VocabularyFolder> vocabularyCaptor = ArgumentCaptor.forClass(VocabularyFolder.class);
        ArgumentCaptor<String> vocabularySetIdentifierCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.vocabularyDataService, times(1)).createVocabulary(vocabularySetIdentifierCaptor.capture(), vocabularyCaptor.capture(), any(DDUser.class));
        VocabularyFolder vocabularyCaptured = vocabularyCaptor.getValue();
        String vocabularySetIdentifierCaptured = vocabularySetIdentifierCaptor.getValue();
        assertEquals(folderName, vocabularySetIdentifierCaptured);
        assertTrue(EqualsBuilder.reflectionEquals(vocabularyToBeInserted, vocabularyCaptured));
    }

    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        return trip;
    }

    private MockRoundtrip prepareRoundtrip(String folderName, String vocabularySetIdentifier, boolean numericConceptIdentifier, boolean NotationsEqualIdentifiers) {
        MockRoundtrip trip = this.createRoundtrip();
        trip.setParameter("vocabularyFolder.folderName", folderName);
        trip.setParameter("vocabularyFolder.identifier", vocabularySetIdentifier);
        trip.setParameter("numericConceptidentifiers", Boolean.toString(numericConceptIdentifier));
        trip.setParameter("notationsEqualIdentifiers", Boolean.toString(NotationsEqualIdentifiers));
        return trip;
    }

    private MockRoundtrip prepareRoundtrip(String folderName, String label, String baseuri, String Identifier, boolean numericConceptIdentifier, boolean NotationsEqualIdentifiers) {
        MockRoundtrip trip = this.createRoundtrip();
        trip.setParameter("vocabularyFolder.folderName", folderName);
        trip.setParameter("vocabularyFolder.identifier", Identifier);
        trip.setParameter("label", label);
        trip.setParameter("baseUri", baseuri);
        trip.setParameter("numericConceptidentifiers", Boolean.toString(numericConceptIdentifier));
        trip.setParameter("notationsEqualIdentifiers", Boolean.toString(NotationsEqualIdentifiers));
        return trip;
    }
}
