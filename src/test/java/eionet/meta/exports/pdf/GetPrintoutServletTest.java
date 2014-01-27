package eionet.meta.exports.pdf;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;

import eionet.DDDatabaseTestCase;
import eionet.meta.GetPrintout;

/**
 * 
 * Tests for XlsServlet.
 * 
 * @author enver
 */
public class GetPrintoutServletTest extends DDDatabaseTestCase {

    private static String USER_AGENT = "unit-test";
    private static String REQUESTED_SESSION_ID = "1122334455";

    private HttpServletRequest requestMock = null;
    private HttpServletResponse responseMock = null;
    private ServletConfig servletConfigMock = null;
    private ServletContext servletContextMock = null;
    private GetPrintout getPrintoutServletUnderTest = null;
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
        servletContextMock = EasyMock.createMock(ServletContext.class);
        allMocks.add(servletContextMock);
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
        // these are the first lines of servlet
        EasyMock.expect(servletConfigMock.getServletContext()).andReturn(servletContextMock);
        EasyMock.expect(requestMock.getHeader("User-Agent")).andReturn(GetPrintoutServletTest.USER_AGENT);
        servletContextMock.log("User-Agent= " + GetPrintoutServletTest.USER_AGENT);

        // create an instance of class under test
        getPrintoutServletUnderTest = new GetPrintout();
        getPrintoutServletUnderTest.init(servletConfigMock);
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
     * test if PDF output is responded for a valid dataset.
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADataset() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADataset

    /**
     * test if PDF output is responded for a valid dataset when format is empty (then default PDF should be given).
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADatasetWhenFormatIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADatasetWhenFormatIsEmpty

    /**
     * test if PDF output is responded for a valid dataset when format is null (then default PDF should be given).
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADatasetWhenFormatIsNull() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn(null);
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADatasetWhenFormatIsNull
    
    /**
     * test if PDF output is responded for a valid dataset when out type is empty (then default GDLN should be given).
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADatasetWhenOutTypeIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADatasetWhenOutTypeIsEmpty

    /**
     * test if PDF output is responded for a valid dataset when out type is null (then default GDLN should be given).
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADatasetWhenOutTypeIsNull() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn(null);
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADatasetWhenOutTypeIsNull

    /**
     * test if PDF output is responded for a valid dataset when there is an extra column (:) appended
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADatasetWithAnExtraColumn() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4:");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADatasetWithAnExtraColumn

    /**
     * test if PDF output is responded for a valid dataset when there are two extra columns (::) appended
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForADatasetWithMoreThanOneExtraColumn() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("5::");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW2.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForADatasetWithMoreThanOneExtraColumn

    /**
     * test if PDF output is responded for two valid datasets
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForTwoDatasets() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4:5");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW-NiD_testW2.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForTwoDatasets

    /**
     * test if PDF output is responded for two valid datasets when there is an extra column (:) appended
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForTwoDatasetsWithAnExtraColumn() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("5:4:");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW2-NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForTwoDatasetsWithAnExtraColumn

    /**
     * test if PDF output is responded for two valid datasets when there are two extra columns (::) appended
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForTwoDatasetsWithMoreThanOneExtraColumn() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("5:4::");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW2-NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForTwoDatasetsWithMoreThanOneExtraColumn

    /**
     * test if PDF output is responded for two valid datasets when there many extra columns appended
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForTwoDatasetsWithManyExtraColumn() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4:::::5::");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW-NiD_testW2.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForTwoDatasetsWithManyExtraColumn

    /**
     * test if PDF output is responded for three valid datasets when object id is spiced with a lot of extra columns
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfPdfReturnedForThreeDatasetsWithManyExtraColumn() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4:::5:4::");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/pdf");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW-NiD_testW2-NiD_testW.pdf\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        getPrintoutServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
    }// end of test step testIfPdfReturnedForThreeDatasetsWithManyExtraColumn

    /**
     * test if an exception thrown when invalid format is applied
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForInvalidFormat() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Unknown format requested!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("XLS");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownForInvalidFormat
    
    /**
     * test if an exception thrown when format is RTF
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownForRtfFormat() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "RTF not supported right now!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("RTF");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownForRtfFormat
    
    /**
     * test if an exception thrown when object type is null
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenObjTypeIsNull() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Object type not specified!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjTypeIsNull
    
    /**
     * test if an exception thrown when object type is empty
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenObjTypeIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Object type not specified!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjTypeIsEmpty
    
    /**
     * test if an exception thrown when object id is empty
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenObjIdIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Object ID not specified!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdIsEmpty
    
    /**
     * test if an exception thrown when object id is null
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenObjIdIsNull() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Object ID not specified!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdIsNull
    
    /**
     * test if an exception thrown when object id not containing any id 
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenObjIdNotContainAnyId() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Object ID not specified!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn(":");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdNotContainAnyId
    
    /**
     * test if an exception thrown when first object id is empty 
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenFirstObjIdIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Object ID not specified!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn(":4");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenFirstObjIdIsEmpty
    
    
    /**
     * test if an exception thrown when first object id is empty 
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenOutTypeIsInvalid() throws Exception {
        // initialize for step
        initializeForStep();
        String outType = "FCTS";
        String exceptionMessage = "Unknown handout type- " + outType;

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn(outType);
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("4");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenOutTypeIsInvalid
    
    
    /**
     * test if an exception thrown when first object id is empty 
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhenObjTypeIsInvalid() throws Exception {
        // initialize for step
        initializeForStep();
        String objType = "TBL";
        String exceptionMessage = "Unknown object type- " + objType + "- for this handout type!";

        // record for request
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn(objType);
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("5");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenOutTypeIsInvalid
    
    /**
     * test if an exception thrown when object id is invalid 
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhentObjIdIsInvalid() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Dataset not found!";

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("300");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhentObjIdIsInvalid
    
    /**
     * test if an exception thrown when object id is invalid 
     * 
     * @throws Exception
     *             if test fails
     */
    // @Test
    public void testIfExceptionThrownWhentObjIdIsInvalidInOtherPosition() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Dataset not found!";

        // record for request & context
        EasyMock.expect(requestMock.getParameter("format")).andReturn("PDF");
        EasyMock.expect(requestMock.getParameter("obj_type")).andReturn("DST");
        EasyMock.expect(requestMock.getParameter("out_type")).andReturn("GDLN");
        EasyMock.expect(requestMock.getParameter("obj_id")).andReturn("5:300");
        EasyMock.expect(requestMock.getRequestedSessionId()).andReturn(GetPrintoutServletTest.REQUESTED_SESSION_ID);
        EasyMock.expect(servletContextMock.getRealPath(GetPrintout.PDF_LOGO_PATH)).andReturn("");

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            getPrintoutServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

     // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhentObjIdIsInvalidInOtherPosition

   
    @Override
    protected String getSeedFilename() {
        return "seed-two-datasets.xml";
    }

    private class EnvServletOutputStream extends ServletOutputStream {
        private int numberOfBytesWritten = 0;

        @Override
        public void write(int b) throws IOException {            
            this.numberOfBytesWritten++;
        }
    }// end of inner class EnvServletOutputStream

}// end of JUnit test class XlsServletTest

