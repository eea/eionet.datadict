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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;


import eionet.test.TestingResources;
import eionet.util.Props;
import eionet.util.PropsIF;

import com.tee.xmlserver.*; // For AppUserIF

import eionet.meta.DocUpload;
import static org.easymock.EasyMock.*;

/*
 * An attempt to mock a ServletInputStream
 */

class mockServletInputStream extends ServletInputStream {
    private InputStream instream;

    public mockServletInputStream(String name) throws Exception {
        instream = new FileInputStream(name);
    }

    public int read() throws IOException {
        return instream.read();
    }
        
}

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
         * This test simply uploads the seed-hlp file
         */
	public void testSimpleUpload() throws Exception {

		// Create the mock objects
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		ServletConfig servletConfig = createMock(ServletConfig.class);
		HttpSession httpSession = createMock(HttpSession.class);
		AppUserIF user = createMock(AppUserIF.class);
		ServletContext servletContext = createMock(ServletContext.class);
		
		//Create the target object        
		DocUpload instance = new DocUpload();
		//Call the init of the servlet with the mock ServletConfig
		instance.init(servletConfig);


		// This is what we expect for the servletConfig object
		expect(servletConfig.getServletContext()).andReturn(servletContext);
                expect(servletConfig.getInitParameter("cache-size")).andReturn("1024");


		// This is what we expect for the request object
		request.setCharacterEncoding("UTF-8");
		expect(request.getSession(false)).andReturn((HttpSession) httpSession);
		// ds_id seems to only be used for ACL check. Can easily be spoofed
		expect(request.getParameter("idf")).andReturn("CDDA");
		expect(request.getParameter("ds_id")).andReturn("23");
		expect(request.getParameter("title")).andReturn("Test file for Dataset #23");


		String filename = TestingResources.getResource(TestingResources.class, SEED_HELP_RESOURCE).getFile();
		expect(request.getParameter("file")).andReturn(filename);
		expect(request.getContentType()).andReturn("text/xml");
		expect(request.getParameter("delete")).andReturn("");
		mockServletInputStream instream = new mockServletInputStream(filename);
		expect(request.getInputStream()).andReturn((ServletInputStream)instream);

		expect(httpSession.getAttribute("eionet.util.SecurityUtil.user")).andReturn((AppUserIF) user);
                expect(user.isAuthentic()).andReturn(true);
		expectLastCall().times(2);
		expect(user.getUserName()).andReturn("enriko");


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
