package eionet.meta.service;

import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.dao.domain.DDApiKey;
import eionet.meta.service.impl.WebApiAuthServiceImpl;
import eionet.util.PropsIF;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class WebApiAuthServiceTest {

    private static final String DUMMY_DD_JWT_KEY = "DUMMY_DD_JWT_KEY";
    private static final String DUMMY_DD_JWT_AUDIENCE = "DUMMY_DD_JWT_AUDIENCE";
    private static final String DUMMY_JSON_WEB_TOKEN = "DUMMY_JSON_WEB_TOKEN";
    private static final String DUMMY_API_KEY = "DUMMY_API_KEY";
    private static final String DUMMY_JWT_ISSUER = "DUMMY_JWT_ISSUER";
    
    @Mock
    private IJWTService jwtService;
    
    @Mock 
    private IApiKeyService apiKeyService;
    
    @Mock
    private ConfigurationPropertyValueProvider configurationPropertyValueProvider;
    
    @Mock
    private HttpServletRequest request;
    
    private WebApiAuthService webApiAuthService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.webApiAuthService = new WebApiAuthServiceImpl(this.jwtService, this.apiKeyService, this.configurationPropertyValueProvider);
    }
    
    @Test
    public void testApiKeyMissing() throws ServiceException {
        this.assertUserAuthenticationException();
        
        verify(this.jwtService, times(0)).verify(any(String.class), any(String.class), any(String.class));
        verify(this.apiKeyService, times(0)).getApiKey(any(String.class));
    }
    
    @Test
    public void testDeprecatedToken() throws ServiceException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        
        JSONObject jwtVerifyResult = new JSONObject();
        jwtVerifyResult.put(WebApiAuthServiceImpl.JWT_ISSUER, DUMMY_JWT_ISSUER);
        jwtVerifyResult.put(WebApiAuthServiceImpl.TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON, 0L);
        
        when(this.jwtService.verify(any(String.class), any(String.class), same(DUMMY_JSON_WEB_TOKEN))).thenReturn(jwtVerifyResult);
        when(this.configurationPropertyValueProvider.getPropertyIntValue(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES)).thenReturn(1);
        
        this.assertUserAuthenticationException();
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(any(String.class), any(String.class), same(DUMMY_JSON_WEB_TOKEN));
        verify(this.apiKeyService, times(0)).getApiKey(any(String.class));
    }
    
    @Test
    public void testInvalidApiKey() throws ServiceException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        this.configureNonDeprecatedToken();
        
        this.assertUserAuthenticationException();
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN);
        verify(this.apiKeyService, times(1)).getApiKey(DUMMY_API_KEY);
    }
    
    @Test
    public void testExpiredApiKey() throws ServiceException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        this.configureNonDeprecatedToken();
        DDApiKey ddApiKey = Mockito.spy(new DDApiKey());
        ddApiKey.setExpires(new Date(Calendar.getInstance().getTime().getTime() - this.calculateOneDayMillis()));
        when(this.apiKeyService.getApiKey(DUMMY_API_KEY)).thenReturn(ddApiKey);
        
        this.assertUserAuthenticationException();
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN);
        verify(this.apiKeyService, times(1)).getApiKey(DUMMY_API_KEY);
        verify(ddApiKey, atLeast(1)).getExpires();
    }
    
    @Test
    public void testSuccessWithoutExpirationLimit() throws ServiceException, UserAuthenticationException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        this.configureNonDeprecatedToken();
        DDApiKey ddApiKey = Mockito.spy(new DDApiKey());
        when(this.apiKeyService.getApiKey(DUMMY_API_KEY)).thenReturn(ddApiKey);
        
        DDUser user = this.webApiAuthService.authenticate(request);
        
        assertEquals(user.getUserName(), DUMMY_JWT_ISSUER);
        assertTrue(user.isAuthentic());
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN);
        verify(this.apiKeyService, times(1)).getApiKey(DUMMY_API_KEY);
        verify(ddApiKey, atLeast(1)).getExpires();
    }
    
    @Test
    public void testSuccessWithExpirationLimit() throws ServiceException, UserAuthenticationException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        this.configureNonDeprecatedToken();
        DDApiKey ddApiKey = Mockito.spy(new DDApiKey());
        ddApiKey.setExpires(new Date(Calendar.getInstance().getTime().getTime() + this.calculateOneDayMillis()));
        when(this.apiKeyService.getApiKey(DUMMY_API_KEY)).thenReturn(ddApiKey);
        
        DDUser user = this.webApiAuthService.authenticate(request);
        
        assertEquals(user.getUserName(), DUMMY_JWT_ISSUER);
        assertTrue(user.isAuthentic());
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN);
        verify(this.apiKeyService, times(1)).getApiKey(DUMMY_API_KEY);
        verify(ddApiKey, atLeast(1)).getExpires();
    }
    
    private void assertUserAuthenticationException() {
        try {
            this.webApiAuthService.authenticate(this.request);
            fail("expected UserAuthenticationException");
        }
        catch (UserAuthenticationException ex) { }
    }
    
    private void configureExistingApiKey() {
        when(this.request.getHeader(WebApiAuthServiceImpl.JWT_API_KEY_HEADER)).thenReturn(DUMMY_JSON_WEB_TOKEN);
    }
    
    private void configureDDJwtConfiguration() {
        when(this.configurationPropertyValueProvider.getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY)).thenReturn(DUMMY_DD_JWT_KEY);
        when(this.configurationPropertyValueProvider.getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE)).thenReturn(DUMMY_DD_JWT_AUDIENCE);
        when(this.configurationPropertyValueProvider.getPropertyIntValue(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES)).thenReturn(10000);
    }
    
    private JSONObject configureNonDeprecatedToken() throws ServiceException {
        long dateCreatedInSeconds = Calendar.getInstance().getTimeInMillis() / 1000L;
        JSONObject jwtVerifyResult = new JSONObject();
        
        jwtVerifyResult.put(WebApiAuthServiceImpl.TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON, dateCreatedInSeconds);
        jwtVerifyResult.put(WebApiAuthServiceImpl.API_KEY_IDENTIFIER_IN_JSON, DUMMY_API_KEY);
        jwtVerifyResult.put(WebApiAuthServiceImpl.JWT_ISSUER, DUMMY_JWT_ISSUER);
        
        when(this.jwtService.verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN)).thenReturn(jwtVerifyResult);
        
        return jwtVerifyResult;
    }
    
    private long calculateOneDayMillis() {
        return 1000 * 60 * 60 * 24;
    }
    
}
