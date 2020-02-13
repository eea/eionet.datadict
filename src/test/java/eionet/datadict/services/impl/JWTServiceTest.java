package eionet.datadict.services.impl;

import eionet.meta.service.ServiceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.when;

public class JWTServiceTest {

    @Mock
    JWTServiceImpl jwtService;

    @Before
    public void setUp() throws ServiceException {
        MockitoAnnotations.initMocks(this);
        when(jwtService.getJwtAudience()).thenReturn("DataDictionary");
        when(jwtService.getJwtSubject()).thenReturn("eea");
        when(jwtService.getJwtIssuer()).thenReturn("eea");
        when(jwtService.getJwtSignatureAlgorithm()).thenReturn("HS512");
        when(jwtService.getJwtApiKey()).thenReturn("11BA6CE73F3AD9F98A75A2F8F5A287E993563B0A1A8F02B25210C208872A734C");
        when(jwtService.generateJWTToken()).thenCallRealMethod();
    }

    /* Test case:  */
    @Test
    public void testGenerateJWTToken() throws Exception {
        String result = jwtService.generateJWTToken();
        System.out.println("__________________________token is: " + result);
        Assert.assertThat(result, is(nullValue()));
    }
}
