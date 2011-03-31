package eionet.meta;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import eionet.util.sql.ConnectionUtil;

/**
 * Unit tests for <code>eionet.meta.DocDownload</code>.
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
public class DocDownloadTest extends TestCase {

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		ConnectionUtil.setConnectionType(ConnectionUtil.SIMPLE_CONNECTION);
	}

	/**
	 * This one tests the private method <code>eionet.meta.DocDownload.getAbsPath(HttpServletRequest)</code>.
	 * We call the servlet's <code>doGet</code> and as a request parameter, we give it a false <code>DocDownload.REQPAR_FILE</code>
	 * which should make <code>getAbsPath(HttpServletRequest)</code> throw an exception with a specific message that we expect.
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	public void test_getAbsPath() throws ServletException, IOException{

		// create mocks
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);

        // create DocDownload servlet object  
        DocDownload docDownloadInstance = new DocDownload();
        docDownloadInstance.init(servletConfig);

        // what we expect for the servletConfig object
        //expect(servletConfig.getServletContext()).andReturn(servletContext);
		
        // what we expect for the request object
        request.setCharacterEncoding("UTF-8");
		expect(request.getParameter(DocDownload.REQPAR_FILE)).andReturn("...");
		
        // replay mocks
        replay(request);
        replay(servletConfig);

        String message = null;
        try{
        	docDownloadInstance.doGet(request, response);
        }
        catch (ServletException e){
        	message = e.getMessage();
        }
        if (message==null || !message.startsWith("java.lang.Exception: Failed to get the file path from db"))
        	fail("Was expecting exception with message 'Failed to get the file path from db'");

        // verify the responses
        verify(request);
        verify(servletConfig);
	}
}
