package eionet.datadict.services.auth.impl;

import eionet.datadict.services.auth.WebApiAuthInfo;
import eionet.datadict.services.auth.WebApiAuthInfoService;
import javax.servlet.http.HttpServletRequest;
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
public class WebApiAuthInfoServiceTest {

    @Mock
    private HttpServletRequest request;
    
    private WebApiAuthInfoService webApiAuthInfoService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.webApiAuthInfoService = new WebApiAuthInfoServiceImpl();
    }
    
    @Test
    public void testInfoExtraction() {
        String token = "token";
        String remoteHost = "www.somehost.com";
        String remoteAddress = "172.168.1.1";
        when(this.request.getHeader(WebApiAuthInfoServiceImpl.JWT_API_KEY_HEADER)).thenReturn(token);
        when(this.request.getRemoteHost()).thenReturn(remoteHost);
        when(this.request.getRemoteAddr()).thenReturn(remoteAddress);
        WebApiAuthInfo authInfo = this.webApiAuthInfoService.getAuthenticationInfo(this.request);
        
        assertEquals(token, authInfo.getAuthenticationToken());
        assertEquals(remoteHost, authInfo.getRemoteHost());
        assertEquals(remoteAddress, authInfo.getRemoteAddress());
    }
    
}
