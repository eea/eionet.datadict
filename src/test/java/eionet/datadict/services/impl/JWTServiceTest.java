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
        when(jwtService.getJwtApiKey()).thenReturn("?C?YTwNa>jaRskCitrWw5RwsL>H<VLzxr4c5xB9Xy4Ec?pL<qdQgL=ZGMc6SaWD+>hq5U6qypL4Kgs>PvaMZTKKsVrAS>2ApnUcMuwnnzuu3xsV8HCAE>ujs");
        when(jwtService.generateJWTToken("1")).thenCallRealMethod();
    }

    /* Test case:  */
    @Test
    public void testGenerateJWTToken() throws Exception {
        String result = jwtService.generateJWTToken("1");
        System.out.println("__________________________token is: " + result);
        Assert.assertThat(result, is(nullValue()));
    }
}
