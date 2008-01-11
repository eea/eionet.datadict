package eionet.meta;


import junit.framework.TestCase;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import eionet.meta.LogoutServlet;
import static org.easymock.EasyMock.*;


/*
 * This unittest tests the logout servlet, in an example where the user has no session
 * The response from the logout servlet should be to redirect to index.jsp
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
public class LogoutServletTest extends TestCase {

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
        expect(request.getSession(false)).andReturn(null);

        // This is what we expect to be called for the request object
        response.sendRedirect("index.jsp"); // Needs no return value
		
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
}
