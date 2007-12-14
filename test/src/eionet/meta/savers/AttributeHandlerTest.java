// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta.savers;

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

import eionet.meta.savers.AttributeHandler;
import static org.easymock.EasyMock.*;



/**
 * This unittest tests the DocUpload servlet
 * 
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
public class AttributeHandlerTest extends DatabaseTestCase {
	
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
	 */
	protected IDataSet getDataSet() throws Exception
	{
	    loadedDataSet = new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream("seed-attributes.xml"));
	    return loadedDataSet;
	}

	/**
	 * Tests that a simple attribute with M_ATTRIBUTE_ID=18 is deleted
	 */
	public void testDelete() throws Exception {

		QueryDataSet queryDataSet = new QueryDataSet(getConnection());
		queryDataSet.addTable("ATTRIBUTE", "SELECT count(*) as C FROM ATTRIBUTE");
		queryDataSet.addTable("M_ATTRIBUTE", "SELECT count(*) as C FROM M_ATTRIBUTE");
		// Verify that there are the expected number of rows in the ATTRIBUTE table
		ITable tmpTable = queryDataSet.getTable("ATTRIBUTE");
		TestCase.assertEquals("451", tmpTable.getValue(0,"C").toString());
		// Verify that there are the expected number of rows in the M_ATTRIBUTE table
		tmpTable = queryDataSet.getTable("M_ATTRIBUTE");
		TestCase.assertEquals("2", tmpTable.getValue(0,"C").toString());

		// Create the mock objects
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		ServletConfig servletConfig = createMock(ServletConfig.class);
		ServletContext servletContext = createMock(ServletContext.class);
		
		Connection jdbcConn = getConnection().getConnection();
	
		// This is what we expect for the servletContext object
		expect(servletContext.getInitParameter("visuals-path")).andReturn("HERE-IS-VISUALS-PATH");
		expect(servletContext.getInitParameter("versioning")).andReturn("HERE-IS-VISUALS-PATH");
		//expect(servletContext.getInitParameter(not(eq("module-db_pool")))).andStubReturn(null);

		// This is what we expect for the request object
		//request.setCharacterEncoding("UTF-8");
		expect(request.getRequestedSessionId()).andReturn("92834kejwh89");
		expect(request.getParameter("mode")).andReturn("delete");
		expect(request.getParameter("type")).andReturn("SIMPLE");
		expect(request.getParameter("attr_id")).andReturn("18");
		expect(request.getParameter("name")).andReturn("Keywords");
		expect(request.getParameter("short_name")).andReturn("Keyword");
		expect(request.getParameter("definition")).andReturn("One or more significant words.");
		expect(request.getParameter("obligation")).andReturn("M");
		expect(request.getParameter("ns")).andReturn("basens");
		String[] vs = new String[1];
		vs[0] = "18";
		expect(request.getParameterValues("simple_attr_id")).andReturn(vs);
		expect(request.getParameterValues("complex_attr_id")).andReturn(null);
		
		
		//start the replay for all mock objects
		replay(request);
		replay(response);
		replay(servletConfig);
		replay(servletContext);

		AttributeHandler instance = new AttributeHandler(jdbcConn, request, servletContext);
		instance.execute_();

		//verify the responses
		verify(request);
		verify(response);
		verify(servletConfig);
		verify(servletContext);

		// Verify that there are the expected number of rows in the ATTRIBUTE table
		tmpTable = queryDataSet.getTable("ATTRIBUTE");
		TestCase.assertEquals("142", tmpTable.getValue(0,"C").toString());
		// Verify that there are the expected number of rows in the M_ATTRIBUTE table
		tmpTable = queryDataSet.getTable("M_ATTRIBUTE");
		TestCase.assertEquals("1", tmpTable.getValue(0,"C").toString());
	}

}

