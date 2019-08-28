package eionet.meta;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.filters.CASFilterConfig;
import eionet.meta.filters.CASInitParam;
import eionet.util.Props;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/*
 * This unittest tests the logout servlet, in an example where the user has no session
 * The response from the logout servlet should be to redirect to index.jsp
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class LogoutServletTestIT {

    private static final String CONTEXT_PATH = "testContext";
    private static final String REQUEST_SCHEME = "http";

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testService() throws Exception {
        
        // Create the mock objects
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);
        ServletContext servletContext = createMock(ServletContext.class);
        
        // Create the target object        
        LogoutServlet instance = new LogoutServlet();

        // Call the init of the servlet with the mock ServletConfig
        instance.init(servletConfig);

        // This is what we expect for the request object
        request.setCharacterEncoding("UTF-8"); // Needs no return value
        
        // We simulate that the user has no session variable by returning NULL
        expect(request.getSession()).andReturn(null);
        
        // a couple of more expected calls to request
        expect(request.getScheme()).andReturn(REQUEST_SCHEME);
        expect(request.getContextPath()).andReturn(CONTEXT_PATH).atLeastOnce();
        String expectedLogoutUrl = getExpectedLogoutUrl();
        
        // CASFilterConfig must be initialized
        CASFilterConfig.init(null);

        // This is what we expect to be called for the request object
        response.sendRedirect(expectedLogoutUrl); // Needs no return value
        
        // start the replay for all mock objects
        replay(request);
        replay(response);
        replay(servletConfig);
        replay(servletContext);

        // and call your doGet, doPost, or service
        // methods at will.
        instance.service(request, response);

        // verify the responses
        verify(request);
        verify(response);
        verify(servletConfig);
        verify(servletContext);
    }
    
    /**
     * 
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getExpectedLogoutUrl() throws UnsupportedEncodingException{
        
        String casLoginUrl = Props.getRequiredProperty(CASInitParam.CAS_LOGIN_URL.toString());
        String casServerName = Props.getRequiredProperty(CASInitParam.CAS_SERVER_NAME.toString());
        
        return casLoginUrl.replaceFirst("/login", "/logout") + "?url=" + URLEncoder.encode(
                REQUEST_SCHEME + "://" + casServerName + CONTEXT_PATH, "UTF-8");
    }
}
