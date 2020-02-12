package eionet.meta.web.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.web.DDActionBeanContext;
import eionet.web.action.JWTApiActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;


public class JWTApiActionBeanTest {

    @Mock
    JWTApiActionBean jwtApiActionBean;

    @Mock
    JWTService jwtService;

    @Mock
    DDActionBeanContext ctx;

    MockHttpServletRequest request;

    final static String expectedToken = "testToken";

    @Before
    public void setUp() throws ServiceException, JsonProcessingException {
        MockitoAnnotations.initMocks(this);
        request = new MockHttpServletRequest();
        when(jwtApiActionBean.getJwtService()).thenReturn(jwtService);
        when(jwtApiActionBean.getJwtService().generateJWTToken()).thenReturn(expectedToken);
        when(jwtApiActionBean.generateJWTToken()).thenCallRealMethod();
        when(jwtApiActionBean.isPostRequest()).thenCallRealMethod();
        when(ctx.getRequest()).thenReturn(request);
    }

    /* Test case: get method instead of post */
    @Test(expected = ServiceException.class)
    public void testGenerateJWTTokenWrongMethod() throws Exception {
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("GET");
        try
        {
            jwtApiActionBean.generateJWTToken();
        }
        catch(ServiceException se)
        {
            String expectedMessage = "generateJWTToken API - The request method was not POST.";
            Assert.assertThat(se.getMessage(), is(expectedMessage));
            throw se;
        }
        fail("Wrong request method - exception did not throw!");
    }

    /* Test case: no parameters were passed */
    @Test(expected = ServiceException.class)
    public void testGenerateJWTTokenNoParameters() throws Exception {
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");

        try
        {
            jwtApiActionBean.generateJWTToken();
        }
        catch(ServiceException se)
        {
            String expectedMessage = "generateJWTToken API - Credentials were missing.";
            Assert.assertThat(se.getMessage(), is(expectedMessage));
            throw se;
        }
        fail("No parameters in request - exception did not throw!");
    }

    /* Test case: successful generation of token */
    @Test
    public void testGenerateJWTTokenSuccessful() throws Exception {
        //String actualToken = jwtApiActionBean.generateJWTToken();
        //Assert.assertThat(actualToken, is(expectedToken));
    }



}
