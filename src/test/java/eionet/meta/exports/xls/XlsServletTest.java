package eionet.meta.exports.xls;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Assert;

import eionet.DDDatabaseTestCase;

/**
 * 
 * Tests for XlsServlet.
 * 
 * @author enver
 */
public class XlsServletTest extends DDDatabaseTestCase {

    private HttpServletRequest requestMock = null;
    private HttpServletResponse responseMock = null;
    private ServletConfig servletConfigMock = null;
    private XlsServlet xlsServletUnderTest = null;
    private ArrayList<Object> allMocks = null;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initializeForTests();
    }

    /**
     * Initialize variables and mocks before test suite start running. This method can not be called automatically because DbUnit
     * test extends TestCase class (JUnit v3 style).
     */
    // @BeforeClass
    private void initializeForTests() {
        allMocks = new ArrayList<Object>();
        // initialize all mocks
        requestMock = EasyMock.createMock(HttpServletRequest.class);
        allMocks.add(requestMock);
        responseMock = EasyMock.createMock(HttpServletResponse.class);
        allMocks.add(responseMock);
        servletConfigMock = EasyMock.createMock(ServletConfig.class);
        allMocks.add(servletConfigMock);
    }

    /**
     * Reset mocks and instantiate and initialize class under test (XlsServlet). This method can not be called automatically because
     * DbUnit test extends TestCase class (JUnit v3 style).
     * 
     * @throws Exception
     *             if initialization fails
     */
    // @Before
    private void initializeForStep() throws Exception {
        // reset all mock
        resetAllMocks();
        // create an instance of class under test
        xlsServletUnderTest = new XlsServlet();
        xlsServletUnderTest.init(servletConfigMock);
    }

    /**
     * Reset all mock objects. Not: Can't extend EasyMockSupport and it does not work as a member field. Otherwise, resetAll method
     * could be used instead of this
     */
    private void resetAllMocks() {
        for (Object mock : allMocks) {
            EasyMock.reset(mock);
        }
    }

    /**
     * Replay all mock objects. Not: Can't extend EasyMockSupport and it does not work as a member field. Otherwise, replayAll
     * method could be used instead of this
     */
    private void replyAllMocks() {
        for (Object mock : allMocks) {
            EasyMock.replay(mock);
        }
    }

    /**
     * Verify all mock objects. Not: Can't extend EasyMockSupport and it does not work as a member field. Otherwise, verifyAll
     * method could be used instead of this
     */
    private void verifyAllMocks() {
        for (Object mock : allMocks) {
            EasyMock.verify(mock);
        }
    }

    /**
     * test if Xls output is responded for a valid dataset.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfXlsReturnedForDataset() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("dst");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/vnd.ms-excel");
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_test.xls\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        xlsServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.somethingWritten);
        verifyAllMocks();
    }

    /**
     * test if Xls output is responded for a valid table.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfXlsReturnedForTable() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("tbl");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("5");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/vnd.ms-excel");
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_test_NiD_SW_Stat.xls\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        xlsServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.somethingWritten);
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_id is missing.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForMissingObjectId() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing object id!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("");

        // record for response
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // verify mocks
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_id is null.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForNullObjectId() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing object id!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn(null);

        // record for response
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // verify mocks
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_type is null.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForNullObjectType() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing object type or object type invalid!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn(null);

        // record for response
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // verify mocks
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_type is missing.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForMissingObjectType() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing object type or object type invalid!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("");

        // record for response
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // verify mocks
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_id is an invalid dataset id.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForInvalidDstId() throws Exception {
        // initialize for step
        initializeForStep();
        String objId = "300";
        String exceptionMessage = "Dataset " + objId + " not found!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn(objId);
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("dst");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        Assert.assertTrue("Something is written to os", !os.somethingWritten);
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_id is an invalid table id.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForInvalidTblId() throws Exception {
        // initialize for step
        initializeForStep();
        String objId = "500";
        String exceptionMessage = "Table " + objId + " not found!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn(objId);
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("tbl");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", "Table " + "500" + " not found!", e.getMessage());
        }

        // perform checks and verify mocks
        Assert.assertTrue("Something is written to os", !os.somethingWritten);
        verifyAllMocks();
    }

    /**
     * test if exception thrown when obj_type is invalid.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForInvalidObjectType() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing object type or object type invalid!";

        // record for request
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("invalid");

        // record for response
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            xlsServletUnderTest.service(requestMock, responseMock);
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // verify mocks
        verifyAllMocks();
    }   

    @Override
    protected String getSeedFilename() {
        return "seed-dataset.xml";
    }

    private class EnvServletOutputStream extends ServletOutputStream {
        private boolean somethingWritten = false;

        @Override
        public void write(int b) throws IOException {
            this.somethingWritten = true;
        }
    }// end of inner class EnvServletOutputStream

}// end of JUnit test class XlsServletTest
