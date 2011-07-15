// The doPost method of DocumentationServlet is protected
// Therefore we must be in the same package
package eionet.meta;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.sql.Connection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import eionet.test.Seed;
import eionet.util.sql.ConnectionUtil;


/**
 * This unittest tests the Documentation servlet
 * IT NEEDS the seed-hlp.xml in the classes directory
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
public class DocumentationServletTest extends DatabaseTestCase {
    
    /** */
    private FlatXmlDataSet loadedDataSet;

    /**
     * Provide a connection to the database.
     */
    protected IDatabaseConnection getConnection() throws Exception {
        Connection jdbcConn = ConnectionUtil.getSimpleConnection();         
        return new DatabaseConnection(jdbcConn);
    }

    /**
     * Load the data which will be inserted for the test
     * The tables must already exist
     */
    protected IDataSet getDataSet() throws Exception {
        loadedDataSet = new FlatXmlDataSet(
                getClass().getClassLoader().getResourceAsStream(Seed.HLP));
        return loadedDataSet;
    }

    /**
     * This test simply loads the help area 'doc1' from the database
     */
    public void testDoc1() throws Exception {

        // Create the mock objects
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);
        ServletContext servletContext = createMock(ServletContext.class);
        RequestDispatcher requestDispatcher = createMock(RequestDispatcher.class);
        
        // Create the target object        
        DocumentationServlet instance = new DocumentationServlet();

        // Call the init of the servlet with the mock ServletConfig
        instance.init(servletConfig);

        expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
        expect(servletConfig.getInitParameter("forward-jsp")).andReturn(
                "documentation.jsp");
        expect(servletContext.getRequestDispatcher("/documentation.jsp")).andReturn(
                requestDispatcher);

        // This is what we expect for the request object
        expect(request.getPathInfo()).andReturn("doc1");
        expect(request.getServletPath()).andReturn("/");
        request.setAttribute("dispatcher-path", "");

        // The next two values are retrieved from the database
        request.setAttribute("doc-heading",
                "Data Dictionary - functions (from seed-hlp.xml)");
        request.setAttribute("doc-string",
                "<h1>Data Dictionary - functions</h1>");

        // This is what we expect for the RequestDispatcher
        requestDispatcher.forward(request, response);

        // start the replay for all mock objects
        replay(request);
        replay(response);
        replay(servletConfig);
        replay(servletContext);
        replay(requestDispatcher);

        // and call your doGet, doPost, or service methods at will.
        instance.doGet(request, response);

        // verify the responses
        verify(request);
        verify(response);
        verify(servletConfig);
        verify(servletContext);
        verify(requestDispatcher);
    }

    /**
     * This test tries to load a help area that doesn't exist in the database
     * The only thing that happens is the "doc-string" attribute isn't set
     */
    public void testDoesntExist() throws Exception {

        // Create the mock objects
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);
        ServletContext servletContext = createMock(ServletContext.class);
        RequestDispatcher requestDispatcher = createMock(RequestDispatcher.class);
        
        // Create the target object        
        DocumentationServlet instance = new DocumentationServlet();

        // Call the init of the servlet with the mock ServletConfig
        instance.init(servletConfig);

        expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
        expect(servletConfig.getInitParameter("forward-jsp")).andReturn(
                "documentation.jsp");

        expect(servletContext.getRequestDispatcher("/documentation.jsp")).andReturn(
                requestDispatcher);

        expect(request.getPathInfo()).andReturn("doesntexist");
        expect(request.getServletPath()).andReturn("/");
        request.setAttribute("dispatcher-path", "");

        // This is what we expect for the RequestDispatcher
        requestDispatcher.forward(request, response);

        // start the replay for all mock objects
        replay(request);
        replay(response);
        replay(servletConfig);
        replay(servletContext);
        replay(requestDispatcher);

        // and call your doGet, doPost, or service methods at will.
        instance.doGet(request, response);

        // verify the responses
        verify(request);
        verify(response);
        verify(servletConfig);
        verify(servletContext);
        verify(requestDispatcher);
    }

}
