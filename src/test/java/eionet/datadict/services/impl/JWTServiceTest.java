package eionet.datadict.services.impl;

import eionet.meta.service.Auth0JWTServiceImpl;
import eionet.meta.service.IJWTService;
import eionet.meta.service.ServiceException;
import net.sf.json.JSONObject;
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

    private IJWTService jwtServiceForVerification;

    @Before
    public void setUp() throws ServiceException {
        MockitoAnnotations.initMocks(this);
        when(jwtService.getJwtAudience()).thenReturn("DataDictionary");
        when(jwtService.getJwtSubject()).thenReturn("eea");
        when(jwtService.getJwtIssuer()).thenReturn("eea");
        when(jwtService.getJwtSignatureAlgorithm()).thenReturn("HS512");
        when(jwtService.getJwtApiKey()).thenReturn("oSx01e+/ohnemTVHs2r5QqT/m8/Q0f41nVJFXHORoOcLXSVsyE37NJO5/BVM+fCXdaC9F8d0X9obFscv0lGaK6/yS5vrNoqIK+FXViDWTcYlD6sOUDdfR4lUpJaKD0lrnMtmP42X49E+qnQrQD9DY/au9peSkndWquta37JVUleg1501ShYJF4X0adxjYhHssA/S9QRHTl1eUb134abOKMJ/7Dj1V6/++4rg3A==");
        when(jwtService.getDD_URL()).thenReturn("testDomain");
        when(jwtService.generateJWTToken()).thenCallRealMethod();
        jwtServiceForVerification = new Auth0JWTServiceImpl();
    }

    /* Test case: successful creation of a not null token that can be verified */
    @Test
    public void testGenerateJWTTokenSuccessful() throws Exception {
        String token = jwtService.generateJWTToken();
        Assert.assertThat(token, is(notNullValue()));

        JSONObject jsonObject = jwtServiceForVerification.verify(jwtService.getJwtApiKey(), jwtService.getJwtAudience(), token);
        Assert.assertThat(jsonObject.get("sub"), is(jwtService.getJwtSubject()));
        Assert.assertThat(jsonObject.get("aud"), is(jwtService.getJwtAudience()));
        Assert.assertThat(jsonObject.get("iss"), is(jwtService.getJwtIssuer()));
        Assert.assertThat(jsonObject.get("domain"), is(jwtService.getDD_URL()));
        Assert.assertThat(jsonObject.get("iat"), is(notNullValue()));
    }
}
