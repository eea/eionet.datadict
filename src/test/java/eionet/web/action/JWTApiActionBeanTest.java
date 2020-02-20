package eionet.web.action;

import eionet.datadict.services.JWTService;
import eionet.web.DDActionBeanContext;
import net.sourceforge.stripes.action.StreamingResolution;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;


public class JWTApiActionBeanTest {

    @Mock
    JWTApiActionBean jwtApiActionBean;

    @Mock
    JWTService jwtService;

    @Mock
    DDActionBeanContext ctx;

    MockHttpServletRequest request;

    MockHttpServletResponse response;

    final static String expectedToken = "testToken";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        when(jwtApiActionBean.getJwtService()).thenReturn(jwtService);
        when(jwtApiActionBean.getJwtService().generateJWTToken()).thenReturn(expectedToken);
        when(jwtApiActionBean.generateJWTToken()).thenCallRealMethod();
        when(jwtApiActionBean.isPostRequest()).thenCallRealMethod();
        when(jwtApiActionBean.getExecutionValueFromSSOPage()).thenCallRealMethod();
        when(jwtApiActionBean.authenticateUser(argThat(not("adminUsername")), argThat(not("adminPassword")))).thenCallRealMethod();
        when(jwtApiActionBean.getContext()).thenReturn(ctx);
        when(jwtApiActionBean.getSSO_LOGIN_PAGE_URI()).thenReturn("https://sso.eionet.europa.eu/login");
        when(ctx.getRequest()).thenReturn(request);
        when(ctx.getResponse()).thenReturn(response);
    }

    /* Test case: get method instead of post */
    @Test
    public void testGenerateJWTTokenWrongMethod() throws Exception {
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("GET");

        StreamingResolution resolution = (StreamingResolution) jwtApiActionBean.generateJWTToken();
        Assert.assertThat(resolution, is(notNullValue()));

        resolution.execute(ctx.getRequest(), ctx.getResponse());
        Assert.assertThat(ctx.getResponse().getContentType(), is("application/json"));
        Assert.assertThat(response.getContentAsString(), is("{\"Error\":\"The request method was not POST.\"}"));
    }

    /* Test case: no parameters were passed */
    @Test
    public void testGenerateJWTTokenNoParameters() throws Exception {
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");

        StreamingResolution resolution = (StreamingResolution) jwtApiActionBean.generateJWTToken();
        Assert.assertThat(resolution, is(notNullValue()));

        resolution.execute(ctx.getRequest(), ctx.getResponse());
        Assert.assertThat(ctx.getResponse().getContentType(), is("application/json"));
        Assert.assertThat(response.getContentAsString(), is("{\"Error\":\"Credentials were missing.\"}"));
    }

    /* Test case: the given credentials do not belong to an eionet user */
    @Test
    public void testGenerateJWTTokenUserDoesNotExist() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "falseUsername");
        credentials.put("password", "falsePassword");
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");
        request.setParameters(credentials);

        StreamingResolution resolution = (StreamingResolution) jwtApiActionBean.generateJWTToken();
        Assert.assertThat(resolution, is(notNullValue()));

        resolution.execute(ctx.getRequest(), ctx.getResponse());
        Assert.assertThat(ctx.getResponse().getContentType(), is("application/json"));
        Assert.assertThat(response.getContentAsString(), is("{\"Error\":\"Wrong credentials were retrieved.\"}"));
    }

    //TODO complete following tests after finding way to authorize user

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
        when(jwtApiActionBean.authenticateUser("adminUsername","adminPassword")).thenReturn(true);
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "adminUsername");
        credentials.put("password", "adminPassword");
        request.setRequestURI("/api/jwt/generateJWTToken");
        request.setMethod("POST");
        request.setParameters(credentials);

        StreamingResolution resolution = (StreamingResolution) jwtApiActionBean.generateJWTToken();
        Assert.assertThat(resolution, is(notNullValue()));

        resolution.execute(ctx.getRequest(), ctx.getResponse());
        Assert.assertThat(ctx.getResponse().getContentType(), is("application/json"));
        Assert.assertThat(response.getContentAsString(), is("{\"token\":\"testToken\"}"));
    }

    /* Test case: get execution value from SSO page successful*/
    @Test
    public void testGetExecutionValueFromSSOPageSuccessful() throws Exception {
        String execution = jwtApiActionBean.getExecutionValueFromSSOPage();
        Assert.assertThat(execution, is(notNullValue()));
    }

    /* Test case: get execution value from wrong page*/
    @Test(expected = Exception.class)
    public void testGetExecutionValueFromSSOPageWrongURI() throws Exception {
        when(jwtApiActionBean.getSSO_LOGIN_PAGE_URI()).thenReturn("https://www.google.com/");
        try
        {
            String execution = jwtApiActionBean.getExecutionValueFromSSOPage();
        }
        catch(Exception e)
        {
            String expectedMessage = "The execution input type from the https://www.google.com/ page does not exist.";
            Assert.assertThat(e.getMessage(), is(expectedMessage));
            throw e;
        }
        fail("Wrong URI - exception did not throw!");
    }

    /* Test case: authenticate user wrong credentials*/
    @Test
    public void testAuthenticateUserWrongCredentials() throws Exception {
        Boolean result = jwtApiActionBean.authenticateUser("wrongUsername", "WrongPassword");
        Assert.assertThat(result, is(false));
    }

    /* Test case: authenticate user correct credentials
    * The following test has been commented out since a valid user's credentials should not be existing in the code.
    * However, it has been tested and it works
    * */
    /*@Test
    public void testAuthenticateUserCorrectCredentials() throws Exception {
        Boolean result = jwtApiActionBean.authenticateUser("", "");
        Assert.assertThat(result, is(true));
    }*/

    /* Test case: get execution value from wrong page*/
    @Test(expected = Exception.class)
    public void testAuthenticateUserWrongURI() throws Exception {
        when(jwtApiActionBean.getSSO_LOGIN_PAGE_URI()).thenReturn("https://www.google.com/");
        try
        {
            Boolean result = jwtApiActionBean.authenticateUser("wrongUsername", "WrongPassword");
        }
        catch(Exception e)
        {
            String expectedMessage = "The execution input type from the https://www.google.com/ page does not exist.";
            Assert.assertThat(e.getMessage(), is(expectedMessage));
            throw e;
        }
        fail("Wrong URI - exception did not throw!");
    }
}
