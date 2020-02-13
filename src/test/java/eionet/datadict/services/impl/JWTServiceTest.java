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
        when(jwtService.getJwtApiKey()).thenReturn("oSx01e+/ohnemTVHs2r5QqT/m8/Q0f41nVJFXHORoOcLXSVsyE37NJO5/BVM+fCXdaC9F8d0X9obFscv0lGaK6/yS5vrNoqIK+FXViDWTcYlD6sOUDdfR4lUpJaKD0lrnMtmP42X49E+qnQrQD9DY/au9peSkndWquta37JVUleg1501ShYJF4X0adxjYhHssA/S9QRHTl1eUb134abOKMJ/7Dj1V6/++4rg3A==");
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
