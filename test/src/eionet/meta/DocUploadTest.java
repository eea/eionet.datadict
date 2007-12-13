// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta;

import junit.framework.TestCase;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.database.QueryDataSet;
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


import eionet.test.Seed;
import eionet.test.TestingResources;
import eionet.util.Props;
import eionet.util.PropsIF;

import com.tee.xmlserver.*; // For AppUserIF

import eionet.meta.DocUpload;
import static org.easymock.EasyMock.*;

/*
 * An attempt to mock a ServletInputStream
 */

class MockServletInputStream extends ServletInputStream {
    private InputStream instream;

    public MockServletInputStream(String name) throws Exception {
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
	    loadedDataSet = new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream("seed-docupload.xml"));
	    return loadedDataSet;
	}

	/**
	 * This test simply uploads the seed-hlp file
	 * For some reason, the object is not completely reset at each test, so the first
	 * time the servletContext.getInitParameter("module-db_pool") once, and not again
	 * and user.isAuthentic() is called twice the first time and once on every additional test run
	 */
	private void runSimpleUpload(String title) throws Exception {

		// Create the mock objects
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		ServletConfig servletConfig = createMock(ServletConfig.class);
		ServletContext servletContext = createMock(ServletContext.class);
		HttpSession httpSession = createMock(HttpSession.class);
		AppUserIF user = createMock(AppUserIF.class);
		
		// Create the target object        
		DocUpload instance = new DocUpload();
		// Call the init of the servlet with the mock ServletConfig
		instance.init(servletConfig);

		// This is what we expect for the servletConfig object
		expect(servletConfig.getServletContext()).andReturn(servletContext);
		
		// This is what we expect for the servletContext object
		expect(servletContext.getInitParameter("module-db_pool")).andReturn("eionet.test.MockDbPool").times(0,1);
		expect(servletContext.getInitParameter(not(eq("module-db_pool")))).andStubReturn(null);
        
		// This is what we expect for the request object
		request.setCharacterEncoding("UTF-8");
		expect(request.getSession(false)).andReturn((HttpSession) httpSession);
		expect(request.getParameter("idf")).andReturn("CDDA");
		// ds_id seems to only be used for ACL check. Can easily be spoofed
		expect(request.getParameter("ds_id")).andReturn("23");
		expect(request.getParameter("title")).andReturn(title);

		String filename = this.getClass().getClassLoader().getResource(Seed.HLP).getFile();
		expect(request.getParameter("file")).andReturn(filename);
		expect(request.getContentType()).andReturn("text/xml");
		expect(request.getParameter("delete")).andReturn("");
		MockServletInputStream instream = new MockServletInputStream(filename);
		expect(request.getInputStream()).andReturn((ServletInputStream)instream);

		// this is what expect for the httpSession
		expect(httpSession.getAttribute("eionet.util.SecurityUtil.user")).andReturn((AppUserIF) user);
                expect(user.isAuthentic()).andReturn(true);
		expectLastCall().times(1,2);
		
		// THIS USER ACCOUNT MUST BE LISTED IN dd.group!
		expect(user.getUserName()).andReturn("jaanus");

		// this is what expect for the response object
		response.sendRedirect((String)anyObject());
		
		//start the replay for all mock objects
		replay(request);
		replay(response);
		replay(servletConfig);
		replay(servletContext);
		replay(httpSession);
		replay(user);

		//and call your doGet, doPost, or service methods at will.
		instance.doPost(request, response);

		//verify the responses
		verify(request);
		verify(response);
		verify(servletConfig);
		verify(servletContext);
		verify(httpSession);
		verify(user);
		// Verify that there are the expected number of rows in the DOC table
		QueryDataSet queryDataSet = new QueryDataSet(getConnection());
		queryDataSet.addTable("DOC", "SELECT * FROM DOC ORDER BY OWNER_ID");
		ITable tmpTable = queryDataSet.getTable("DOC");
		TestCase.assertEquals(tmpTable.getRowCount(), 2);
		// Verify the title survived
		TestCase.assertEquals(title, tmpTable.getValue(0,"TITLE"));
	}

	public void testUnicodeTitle() throws Exception {
	    runSimpleUpload("Test file for Dataset ¤23");
	}

	public void testQuote() throws Exception {
	    runSimpleUpload("Please don't fail!");
	}

	public void testBackslashQuote() throws Exception {
	    // This is how you escape quotes
	    runSimpleUpload("Please don\\'t fail!");
	}

	public void testBackslashO() throws Exception {
	    // \o is not a special escape sequence in MySQL
	    runSimpleUpload("Please do n\\ot fail!");
	}

	public void testBackslashT() throws Exception {
	    // \t is an escape sequence for TAB, but must be saved escaped
	    runSimpleUpload("Please do no\\t fail!");
	}

	public void testLessThan() throws Exception {
	    runSimpleUpload("2 is < ∞ (infinite)");
	}

	public void testGreek() throws Exception {
	    runSimpleUpload("Τίτλος: Ηλέκτρα");
	}

}

