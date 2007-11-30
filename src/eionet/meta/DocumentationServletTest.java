// The doPost method of DocumentationServlet is protected
// Therefore we must be in the same package
package eionet.meta;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import eionet.test.TestingResources;
import eionet.util.Props;
import eionet.util.PropsIF;

import eionet.meta.DocumentationServlet;
import static org.easymock.EasyMock.*;


/**
 * This unittest tests the Documentation servlet
 * IT NEEDS the seed-hlp.xml in the classes directory
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
public class DocumentationServletTest extends DatabaseTestCase {
	
	/** */
	public static final String SEED_HELP_RESOURCE = "seed-hlp.xml";

	/** */
	private FlatXmlDataSet loadedDataSet;

	/**
	 * Provide a connection to the database.
	 */
	protected IDatabaseConnection getConnection() throws Exception
	{
	    Class.forName(Props.getProperty(PropsIF.DBDRV));
		    Connection jdbcConn = DriverManager.getConnection(
				    Props.getProperty(PropsIF.DBURL),
				    Props.getProperty(PropsIF.DBUSR),
				    Props.getProperty(PropsIF.DBPSW));
		    
	    return new DatabaseConnection(jdbcConn);
	}

	/**
	 * Load the data which will be inserted for the test
	 * The tables must already exist
	 */
	protected IDataSet getDataSet() throws Exception
	{
	    loadedDataSet = new FlatXmlDataSet(TestingResources.getResourceAsStream(TestingResources.class, SEED_HELP_RESOURCE));
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
		
		//Create the target object        
		DocumentationServlet instance = new DocumentationServlet();
		//Call the init of the servlet with the mock ServletConfig
		instance.init(servletConfig);

                expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
                expect(servletConfig.getInitParameter("forward-jsp")).andReturn("documentation.jsp");

                expect(servletContext.getRequestDispatcher("/documentation.jsp")).andReturn(requestDispatcher);

		// This is what we expect for the request object
                expect(request.getPathInfo()).andReturn("doc1");
                expect(request.getServletPath()).andReturn("/");
                request.setAttribute("dispatcher-path", "");

                // The next two values are retrieved from the database
                request.setAttribute("doc-heading", "Data Dictionary - functions (from seed-hlp.xml)");
                request.setAttribute("doc-string", "<h1>Data Dictionary - functions</h1>");

                // This is what we expect for the RequestDispatcher
                requestDispatcher.forward(request, response);

		//start the replay for all mock objects
		replay(request);
		replay(response);
		replay(servletConfig);
		replay(servletContext);
                replay(requestDispatcher);

		//and call your doGet, doPost, or service methods at will.
		instance.doGet(request, response);

		//verify the responses
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
		
		//Create the target object        
		DocumentationServlet instance = new DocumentationServlet();
		//Call the init of the servlet with the mock ServletConfig
		instance.init(servletConfig);

                expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
                expect(servletConfig.getInitParameter("forward-jsp")).andReturn("documentation.jsp");

                expect(servletContext.getRequestDispatcher("/documentation.jsp")).andReturn(requestDispatcher);

                expect(request.getPathInfo()).andReturn("doesntexist");
                expect(request.getServletPath()).andReturn("/");
                request.setAttribute("dispatcher-path", "");

                // This is what we expect for the RequestDispatcher
                requestDispatcher.forward(request, response);

		//start the replay for all mock objects
		replay(request);
		replay(response);
		replay(servletConfig);
		replay(servletContext);
                replay(requestDispatcher);

		//and call your doGet, doPost, or service methods at will.
		instance.doGet(request, response);

		//verify the responses
		verify(request);
		verify(response);
		verify(servletConfig);
		verify(servletContext);
                verify(requestDispatcher);
	}

}
