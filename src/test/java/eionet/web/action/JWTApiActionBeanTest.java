package eionet.web.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.web.DDActionBeanContext;
import eionet.web.action.JWTApiActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
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
        when(jwtApiActionBean.getContext()).thenReturn(ctx);
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

    //TODO complete following tests after finding way to authenticate user

    /* Test case: the given credentials do not belong to an eionet user */
  /*  @Test(expected = ServiceException.class)
    public void testGenerateJWTTokenUserDoesNotExist() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "falseUsername");
        credentials.put("password", "falsePassword");
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");
        request.setParameters(credentials);

        try
        {
            jwtApiActionBean.generateJWTToken();
        }
        catch(ServiceException se)
        {
            String expectedMessage = "generateJWTToken API - Wrong credentials were retrieved.";
            Assert.assertThat(se.getMessage(), is(expectedMessage));
            throw se;
        }
        fail("Wrong credentials - exception did not throw!");
    }
    */


    /* Test case: the user does not have admin rights */
   /* @Test(expected = ServiceException.class)
    public void testGenerateJWTTokenUserIsNotAdmin() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "notAdminUsername");
        credentials.put("password", "notAdminPassword");
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");
        request.setParameters(credentials);

        try
        {
            jwtApiActionBean.generateJWTToken();
        }
        catch(ServiceException se)
        {
            String expectedMessage = "generateJWTToken API - Wrong credentials were retrieved.";
            Assert.assertThat(se.getMessage(), is(expectedMessage));
            throw se;
        }
        fail("User is not admin - exception did not throw!");
    }*/

    /* Test case: successful generation of token */
    @Test
    public void testGenerateJWTTokenSuccessful() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "adminUsername");
        credentials.put("password", "adminPassword");
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");
        request.setParameters(credentials);

        Resolution result = jwtApiActionBean.generateJWTToken();

        Assert.assertThat(result, is(notNullValue()));
        assertEquals(result.getClass(), StreamingResolution.class);

    }

}
