package eionet.meta;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.ITable;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import eionet.DDDatabaseTestCase;
import eionet.test.Seed;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.Util;

/**
 * A test for the {@link DocUpload#doPost(javax.servlet.http.HttpServletRequest, HttpServletResponse)}
 * that does exactly the same as {@link DocUploadTest#testUploadAndDelete()}, but uses ServletUnit to mock the
 * servlet invocation. See http://httpunit.sourceforge.net/doc/servletunit-intro.html.
 * This is a showcase of what can be done with ServletUnit.
 *
 * @author jaanus
 */
public class DocUploadServletUnitTest extends DDDatabaseTestCase {

    /** */
    private static final String UTF_8 = "UTF-8";

    /**
     * Convenience method: runs a simple upload with the given file title and dataset id.
     * @throws Exception
     */
    private void runSimpleUpload(String dstId, String fileTitle) throws Exception {

        String dstIdentifier = "CDDA";
        String fileName = getClass().getClassLoader().getResource(Seed.HLP).getFile();
        String fileContentType = "text/xml";
        String delete = "";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ds_id", dstId);
        params.put("idf", dstIdentifier);
        params.put("title", fileTitle);
        params.put("file", fileName);
        params.put("delete", delete);

        ServletRunner runner = new ServletRunner();
        runner.registerServlet("DocUpload", DocUpload.class.getName());

        ServletUnitClient client = runner.newClient();
        String urlString = "http://host.ignored.anyway/DocUpload?" + toQueryString(params);
        WebRequest webRequest = new PostMethodWebRequest(urlString, new ServletInputStreamMock(fileName), fileContentType);

        InvocationContext invocationContext = client.newInvocation(webRequest);
        assertNotNull("Invocation context should not be null", invocationContext);

        DDUser user = new FakeUser();
        user.authenticate("heinlja", "heinlja");
        invocationContext.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        invocationContext.service();

        WebResponse servletResponse = invocationContext.getServletResponse();
        assertNotNull("Servlet response should not be null", servletResponse);

        int responseCode = servletResponse.getResponseCode();
        assertEquals("Was expecting a redirect from response", HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        File uploadedFile = new File(Props.getRequiredProperty(PropsIF.DOC_PATH), Seed.HLP);
        assertTrue("Didn't find uploaded file were expected", uploadedFile.exists() && uploadedFile.isFile());

        // Verify that there are the expected number of rows in the DOC table now.
        // After DBUnit seed-file was loaded, there was only one row. Now must be two.
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DOC", "SELECT * FROM DOC ORDER BY OWNER_ID");
        ITable tmpTable = queryDataSet.getTable("DOC");
        assertEquals(tmpTable.getRowCount(), 2);

        // Verify that the file's title survived.
        assertEquals(fileTitle, tmpTable.getValue(0, "TITLE"));
    }

    /**
     * Convenience method: runs a simple deletion with the given file title and dataset id
     * @param dstId
     * @throws Exception
     */
    private void runSimpleDelete(String dstId) throws Exception {

        String absPath = DocUpload.getAbsFilePath(getClass().getClassLoader().getResource(Seed.HLP).getFile());
        String legalizedAbsPath = DocUpload.legalizePath(absPath);
        String filePathMd5 = Util.digestHexDec(legalizedAbsPath, "MD5");

        ServletRunner runner = new ServletRunner();
        runner.registerServlet("DocUpload", DocUpload.class.getName());

        ServletUnitClient client = runner.newClient();
        String urlString = "http://host.ignored.anyway/DocUpload";
        WebRequest webRequest = new PostMethodWebRequest(urlString);
        webRequest.setParameter("ds_id", dstId);
        webRequest.setParameter("idf", "CDDA");
        webRequest.setParameter("delete", filePathMd5);

        InvocationContext invocationContext = client.newInvocation(webRequest);
        assertNotNull("Invocation context should not be null", invocationContext);

        DDUser user = new FakeUser();
        user.authenticate("heinlja", "heinlja");
        invocationContext.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        invocationContext.service();

        WebResponse servletResponse = invocationContext.getServletResponse();
        assertNotNull("Servlet response should not be null", servletResponse);

        int responseCode = servletResponse.getResponseCode();
        assertEquals("Was expecting a redirect from response", HttpServletResponse.SC_MOVED_TEMPORARILY, responseCode);

        // Verify that there are the expected number of rows in the DOC table.
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("DOC", "select * from DOC where OWNER_TYPE='dst' and OWNER_ID=" + dstId + " and MD5_PATH='"
                + filePathMd5 + "'");
        ITable tmpTable = queryDataSet.getTable("DOC");
        TestCase.assertEquals(tmpTable.getRowCount(), 0);
    }

    /**
     * A unit test that runs {@link #runSimpleUpload(String, String)} and {@link #runSimpleDelete(String)}.
     *
     * @throws Exception
     */
    public void testUploadAndDelete() throws Exception {
        String dstId = "23";
        runSimpleUpload(dstId, "File title");
        runSimpleDelete(dstId);
    }

    /**
     * Converts given hash-map into HTTP request's query string.
     *
     * @param map
     * @return
     * @throws UnsupportedEncodingException
     */
    private String toQueryString(Map<String, String> map) throws UnsupportedEncodingException {

        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), UTF_8));
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.DDDatabaseTestCase#getSeedFilename()
     */
    @Override
    protected String getSeedFilename() {
        return "seed-docupload.xml";
    }
}
