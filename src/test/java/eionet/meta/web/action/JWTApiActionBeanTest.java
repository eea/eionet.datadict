package eionet.meta.web.action;

import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.web.action.JWTApiActionBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;


public class JWTApiActionBeanTest {

    @Mock
    JWTApiActionBean jwtApiActionBean;

    @Mock
    JWTService jwtService;

    final static String expectedToken = "testToken";

    @Before
    public void setUp() throws ServiceException {
        MockitoAnnotations.initMocks(this);
        when(jwtApiActionBean.getJwtService()).thenReturn(jwtService);
        when(jwtApiActionBean.getJwtService().generateJWTToken("1")).thenReturn(expectedToken);
        when(jwtApiActionBean.generateJWTToken()).thenCallRealMethod();
    }

    /* Test case: successful generation of token */
    @Test
    public void testGenerateJWTTokenSuccessful() throws Exception {
        String actualToken = jwtApiActionBean.generateJWTToken();
        Assert.assertThat(actualToken, is(expectedToken));
    }



}
