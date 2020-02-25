package eionet.datadict.services.auth.impl;

import eionet.datadict.services.auth.WebApiAuthInfo;
import eionet.datadict.services.auth.WebApiAuthService;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.service.ConfigurationPropertyValueProvider;
import eionet.meta.service.IJWTService;
import eionet.meta.service.ServiceException;
import eionet.util.PropsIF;
import java.util.Calendar;
import net.sf.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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
    private static final String DUMMY_JWT_ISSUER = "DUMMY_JWT_ISSUER";
    
    @Mock
    private IJWTService jwtService;
    
    @Mock
    private ConfigurationPropertyValueProvider configurationPropertyValueProvider;
    
    private WebApiAuthService webApiAuthService;
    private WebApiAuthInfo request;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.webApiAuthService = new WebApiAuthServiceImpl(this.jwtService, this.configurationPropertyValueProvider);
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
    }

    
    @Test
    public void testSuccessWithoutExpirationLimit() throws ServiceException, UserAuthenticationException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        this.configureNonDeprecatedToken();

        DDUser user = this.webApiAuthService.authenticate(request);
        
        assertEquals(user.getUserName(), DUMMY_JWT_ISSUER);
        assertTrue(user.isAuthentic());
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN);
    }
    
    @Test
    public void testSuccessWithExpirationLimit() throws ServiceException, UserAuthenticationException {
        this.configureExistingApiKey();
        this.configureDDJwtConfiguration();
        this.configureNonDeprecatedToken();

        DDUser user = this.webApiAuthService.authenticate(request);
        
        assertEquals(user.getUserName(), DUMMY_JWT_ISSUER);
        assertTrue(user.isAuthentic());
        
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
        verify(this.configurationPropertyValueProvider, times(1)).getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
        verify(this.jwtService, times(1)).verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN);
    }
    
    private void assertUserAuthenticationException() {
        try {
            this.webApiAuthService.authenticate(this.request);
            fail("expected UserAuthenticationException");
        }
        catch (UserAuthenticationException ex) { }
    }

    private void configureExistingApiKey() {
        this.request = new WebApiAuthInfo(DUMMY_JSON_WEB_TOKEN);
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
        jwtVerifyResult.put(WebApiAuthServiceImpl.JWT_ISSUER, DUMMY_JWT_ISSUER);
        
        when(this.jwtService.verify(DUMMY_DD_JWT_KEY, DUMMY_DD_JWT_AUDIENCE, DUMMY_JSON_WEB_TOKEN)).thenReturn(jwtVerifyResult);
        
        return jwtVerifyResult;
    }

}
