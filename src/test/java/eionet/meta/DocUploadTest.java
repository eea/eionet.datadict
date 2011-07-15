// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import eionet.test.Seed;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;


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

    /*
     *  (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        ConnectionUtil.setConnectionType(ConnectionUtil.SIMPLE_CONNECTION);
        super.setUp();
    }

    /**
     * Provide a connection to the database.
     */
    protected IDatabaseConnection getConnection() throws Exception {
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
    protected IDataSet getDataSet() throws Exception {
        loadedDataSet = new FlatXmlDataSet(
                getClass().getClassLoader().getResourceAsStream(
                        "seed-docupload.xml"));
        return loadedDataSet;
    }

    /**
     * This test simply uploads the seed-hlp file
     * For some reason, the object is not completely reset at each test, so the first
     * time the servletContext.getInitParameter("module-db_pool") once, and not again
     * and user.isAuthentic() is called twice the first time and once on every additional test run
     */
    private void runSimpleUpload(String title, String ds_id) throws Exception {

        // Create the mock objects
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);
        HttpSession httpSession = createMock(HttpSession.class);
        
        // Create the target object        
        DocUpload instance = new DocUpload();

        // Call the init of the servlet with the mock ServletConfig
        instance.init(servletConfig);

        // This is what we expect for the request object
        request.setCharacterEncoding("UTF-8");
        expect(request.getSession()).andReturn((HttpSession) httpSession);
        expect(request.getParameter("idf")).andReturn("CDDA");
        // ds_id seems to only be used for ACL check. Can easily be spoofed
        expect(request.getParameter("ds_id")).andReturn(ds_id);
        expect(request.getParameter("title")).andReturn(title);

        String filename = this.getClass().getClassLoader().getResource(Seed.HLP).getFile();

        expect(request.getParameter("file")).andReturn(filename);
        expect(request.getContentType()).andReturn("text/xml");
        expect(request.getParameter("delete")).andReturn("");
        MockServletInputStream instream = new MockServletInputStream(filename);

        expect(request.getInputStream()).andReturn((ServletInputStream) instream);

        // this is what expect for the httpSession
        DDUser user = new TestUser();
        user.authenticate("heinlja", "heinlja"); // THIS USER ACCOUNT MUST BE LISTED IN dd.group!
        
        expect(httpSession.getAttribute(SecurityUtil.REMOTEUSER)).andReturn(user);
        expectLastCall().times(1, 2);
        

        // this is what expect for the response object
        response.sendRedirect((String) anyObject());
        
        // start the replay for all mock objects
        replay(request);
        replay(response);
        replay(servletConfig);
        replay(httpSession);

        // and call your doGet, doPost, or service methods at will.
        instance.doPost(request, response);

        // verify the responses
        verify(request);
        verify(response);
        verify(servletConfig);
        verify(httpSession);
        // Verify that there are the expected number of rows in the DOC table
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());

        queryDataSet.addTable("DOC", "SELECT * FROM DOC ORDER BY OWNER_ID");
        ITable tmpTable = queryDataSet.getTable("DOC");

        TestCase.assertEquals(tmpTable.getRowCount(), 2);
        // Verify the title survived
        // Ensure owner_id/ds_id is under 27 for your test record
        TestCase.assertEquals(title, tmpTable.getValue(0, "TITLE"));
    }

    /**
     * 
     * @param title
     * @param ds_id
     * @throws Exception
     */
    private void runSimpleDelete(String ds_id) throws Exception {

        // Create the mock objects
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);
        HttpSession httpSession = createMock(HttpSession.class);
        
        // Create the target object        
        DocUpload instance = new DocUpload();

        // Call the init of the servlet with the mock ServletConfig
        instance.init(servletConfig);

        // This is what we expect for the request object
        request.setCharacterEncoding("UTF-8");
        expect(request.getSession()).andReturn((HttpSession) httpSession);
        expect(request.getParameter("idf")).andReturn("CDDA");
        expect(request.getParameter("ds_id")).andReturn(ds_id);

        String absPath = DocUpload.getAbsFilePath(getClass().getClassLoader().getResource(Seed.HLP).getFile());
        String legalizedAbsPath = DocUpload.legalizePath(absPath);
        String filePathMd5 = Util.digestHexDec(legalizedAbsPath, "MD5");
        expect(request.getParameter("delete")).andReturn(filePathMd5);

        // this is what expect for the httpSession
        DDUser user = new TestUser();
        user.authenticate("heinlja", "heinlja"); // THIS USER ACCOUNT MUST BE LISTED IN dd.group!
        
        expect(httpSession.getAttribute("eionet.util.SecurityUtil.user")).andReturn(user);
        expectLastCall().times(1, 2);

        // this is what we expect for the response object
        response.sendRedirect((String) anyObject());
        
        // start the replay for all mock objects
        replay(request);
        replay(response);
        replay(servletConfig);
        replay(httpSession);

        // and call your doGet, doPost, or service methods at will.
        instance.doPost(request, response);

        // verify the responses
        verify(request);
        verify(response);
        verify(servletConfig);
        verify(httpSession);
        
        // verify that there are the expected number of rows in the DOC table
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DOC", "select * from DOC where OWNER_TYPE='dst' and OWNER_ID=" + ds_id + " and MD5_PATH='" + filePathMd5 + "'");
        ITable tmpTable = queryDataSet.getTable("DOC");
        TestCase.assertEquals(tmpTable.getRowCount(), 0);
    }

    /**
     * 
     * @param title
     * @throws Exception
     */
    private void runSimpleUpload(String title) throws Exception {
        runSimpleUpload(title, "23");
    }

    /**
     * 
     * @throws Exception
     */
    public void testUploadAndDelete() throws Exception {
        runSimpleUpload("Test file for Dataset ¤23");
        runSimpleDelete("23");
    }
}

