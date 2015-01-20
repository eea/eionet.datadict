package eionet.meta.service;


import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import eionet.rpcserver.servlets.XmlRpcRouter;
import eionet.DDDatabaseTestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * Tests if RPC service is working.
 */
public class RpcServiceTest extends DDDatabaseTestCase { //HttpUserAgentTest {

    /**
     * test if RPC service is alive and getDSTables() method is working
     *
     * @throws Exception if not working
     */
    @Test
    public void testGetDSTables() throws Exception {
        InputStream is = null;
        try {
            ServletRunner runner = new ServletRunner();
            runner.registerServlet("rpcrouter", XmlRpcRouter.class.getName());

            ServletUnitClient client = runner.newClient();
            String urlString = "http://host.ignored.anyway/rpcrouter";

            // convert String into InputStream

            String input = "<?xml version=\"1.0\"?><methodCall><methodName>DataDictService.getDSTables</methodName></methodCall>";
            is = new ByteArrayInputStream(input.getBytes());
            WebRequest webRequest = new PostMethodWebRequest(urlString, is, "UTF-8");

            webRequest.setHeaderField("Content-Type", "text/xml");

            InvocationContext invocationContext = client.newInvocation(webRequest);
            Assert.assertNotNull("Invocation context should not be null", invocationContext);

            invocationContext.service();
            WebResponse servletResponse = invocationContext.getServletResponse();
            Assert.assertNotNull("Servlet response should not be null", servletResponse);

            String response = new String(servletResponse.getBytes(), "UTF-8");

            //response must contain dataset fragment:
            String expectedResponseFragment = "<methodResponse><params><param><value><array><data><value><struct>"
                    + "<member><name>shortName</name><value>GW_NO3_concentrations</value></member><member><name>identifier</name>"
                    + "<value>NiD_GW_Conc</value></member><member><name>dataSet</name><value>NiD_water</value></member><member>"
                    + "<name>tblId</name><value>4</value></member><member><name>dateReleased</name><value>010170</value></member>"
                    + "</struct></value>";

            Assert.assertTrue(StringUtils.contains(response, expectedResponseFragment));

            int responseCode = servletResponse.getResponseCode();
            Assert.assertEquals("Was expecting HTTP OK from response", HttpServletResponse.SC_OK, responseCode);
        } finally {
            IOUtils.closeQuietly(is);
        }

    }

    /**
     * test if RPC service is alive and getDSTables() method is working
     *
     * @throws Exception if not working
     */
    @Test
    public void testgetDatasetWithReleaseInfo() throws Exception {
        InputStream is = null;
        try {
            ServletRunner runner = new ServletRunner();
            runner.registerServlet("rpcrouter", XmlRpcRouter.class.getName());

            ServletUnitClient client = runner.newClient();
            String urlString = "http://host.ignored.anyway/rpcrouter";

            // convert String into InputStream

            String input = "<?xml version=\"1.0\"?><methodCall><methodName>DataDictService.getDatasetWithReleaseInfo</methodName><params>"
                    + "<param><value><string>tbl</string></value></param>"
                    + "<param><value><string>3</string></value></param></params></methodCall>";
            is = new ByteArrayInputStream(input.getBytes());
            WebRequest webRequest = new PostMethodWebRequest(urlString, is, "UTF-8");

            webRequest.setHeaderField("Content-Type", "text/xml");

            InvocationContext invocationContext = client.newInvocation(webRequest);
            Assert.assertNotNull("Invocation context should not be null", invocationContext);

            invocationContext.service();
            WebResponse servletResponse = invocationContext.getServletResponse();
            Assert.assertNotNull("Servlet response should not be null", servletResponse);

            String response = new String(servletResponse.getBytes(), "UTF-8");

            //response must contain dataset:
            String expectedResponse = "<?xml version=\"1.0\"?><methodResponse><params><param><value><struct><member>"
                    + "<name>version</name><value>2007</value></member><member><name>status</name><value>Released</value></member>"
                    + "<member><name>isLatestReleased</name><value>true</value></member><member><name>date</name><value>0</value>"
                    + "</member><member><name>shortname</name><value>NiD_water</value></member><member><name>identifier</name>"
                    + "<value>NiD_test</value></member><member><name>tableIds</name><value><array><data><value>4</value>"
                    + "<value>3</value><value>6</value><value>7</value><value>8</value><value>5</value></data></array></value>"
                    + "</member><member><name>id</name><value>4</value></member></struct></value></param></params>"
                    + "</methodResponse>";
            Assert.assertEquals(expectedResponse, response);

        } finally {
            IOUtils.closeQuietly(is);
        }

    }

    @Override
    protected String getSeedFilename() {
        return "seed-rpcservice.xml";
    }
}
