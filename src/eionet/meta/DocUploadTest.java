// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;


import eionet.test.TestingResources;
import eionet.util.Props;
import eionet.util.PropsIF;

import com.tee.xmlserver.*; // For AppUserIF

import eionet.meta.DocUpload;
import static org.easymock.EasyMock.*;


/**
 * This unittest tests the DocUpload servlet
 * 
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
public class DocUploadTest extends DatabaseTestCase {
	
	/** */
	// Needs to load an XML file containint a dataset with id=23
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
		HttpSession httpSession = createMock(HttpSession.class);
		AppUserIF user = createMock(AppUserIF.class);
		
		//Create the target object        
		DocUpload instance = new DocUpload();
		//Call the init of the servlet with the mock ServletConfig
		instance.init(servletConfig);

//                expect(servletConfig.getInitParameter("forward-jsp")).andReturn("documentation.jsp");


		// This is what we expect for the response object

		// This is what we expect for the request object
		request.setCharacterEncoding("UTF-8");
		expect(request.getSession(false)).andReturn((HttpSession) httpSession);
		expect(request.getParameter("idf")).andReturn("23");

		expect(httpSession.getAttribute("eionet.util.SecurityUtil.user")).andReturn((AppUserIF) user);
//                expect(request.getPathInfo()).andReturn("doc1");
//                expect(request.getServletPath()).andReturn("/");
//                request.setAttribute("dispatcher-path", "");
                expect(user.isAuthentic()).andReturn(true);
		expectLastCall().times(2);
		expect(user.getUserName()).andReturn("roug");


		//start the replay for all mock objects
		replay(request);
		replay(response);
		replay(servletConfig);
		replay(httpSession);
		replay(user);

		//and call your doGet, doPost, or service methods at will.
		instance.doPost(request, response);

		//verify the responses
		verify(request);
		verify(response);
		verify(servletConfig);
		verify(httpSession);
		verify(user);
	}
}
