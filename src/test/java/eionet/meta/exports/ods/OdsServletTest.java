package eionet.meta.exports.ods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.meta.InitializeRequiredStartupFiles;
import eionet.meta.spring.SpringApplicationContext;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * Tests for OdsServlet.
 *
 * @author enver
 */
public class OdsServletTest extends DDDatabaseTestCase {

    private static String SESSION_ID = "1122335566";

    private HttpServletRequest requestMock = null;
    private HttpServletResponse responseMock = null;
    private ServletConfig servletConfigMock = null;
    private ServletContext servletContextMock = null;
    private HttpSession sessionMock = null;
    private OdsServlet odsServletUnderTest = null;
    private ArrayList<Object> allMocks = null;
    private ApplicationContext springContext;

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {

        super.setUp();
        initializeForTests();
        /**
         * Manually Invoke InitializeRequiredStartupFiles to initialize needed
         * files for OdsServlet class 
        *
         */
        ActionBeanUtils.getServletContext();
        ApplicationContext appCtx = SpringApplicationContext.getContext();
        AutowireCapableBeanFactory beanFactory = appCtx.getAutowireCapableBeanFactory();
        beanFactory.autowireBeanProperties(InitializeRequiredStartupFiles.class, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        InitializeRequiredStartupFiles initializeRequiredStartupFiles = beanFactory.getBean(InitializeRequiredStartupFiles.class);
        initializeRequiredStartupFiles.initialize();
    }

    /**
     * Initialize variables and mocks before test suite start running.
     */
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
        sessionMock = EasyMock.createMock(HttpSession.class);
        allMocks.add(sessionMock);
    }

    /**
     * Reset mocks and instantiate and initialize class under test (OdsServlet).
     *
     * @throws Exception if initialization fails
     */
    private void initializeForStep() throws Exception {
        // reset all mock
        resetAllMocks();
        // create an instance of class under test
        odsServletUnderTest = new OdsServlet();
        odsServletUnderTest.init(servletConfigMock);
    }

    /**
     * Reset all mock objects. Not: Can't extend EasyMockSupport and it does not
     * work as a member field. Otherwise, resetAll method could be used instead
     * of this
     */
    private void resetAllMocks() {
        for (Object mock : allMocks) {
            EasyMock.reset(mock);
        }
    }

    /**
     * Replay all mock objects. Not: Can't extend EasyMockSupport and it does
     * not work as a member field. Otherwise, replayAll method could be used
     * instead of this
     */
    private void replyAllMocks() {
        for (Object mock : allMocks) {
            EasyMock.replay(mock);
        }
    }

    /**
     * Verify all mock objects. Not: Can't extend EasyMockSupport and it does
     * not work as a member field. Otherwise, verifyAll method could be used
     * instead of this
     */
    private void verifyAllMocks() {
        for (Object mock : allMocks) {
            EasyMock.verify(mock);
        }
    }

    /**
     * Verifies zip (only check files (entries), not the contents of the files)
     */
    private void verifyZip(byte[] bytes) throws Exception {

        final String[] entryNames = {"mimetype", "content.xml", "styles.xml", "meta.xml", "settings.xml", "META-INF/manifest.xml"};

        ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry entry = null;
        int i = 0;
        while ((entry = zipStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            Assert.assertEquals("Entry name does not match", entryNames[i++], entryName);
            zipStream.closeEntry();
        }
        zipStream.close();
    }

    /**
     * test if ODS output is responded for a valid dataset.
     *
     * @throws Exception if test fails
     */
    public void testIfOdsReturnedForADataset() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("type")).andReturn("dst");
        EasyMock.expect(requestMock.getSession()).andReturn(sessionMock);
        EasyMock.expect(sessionMock.getId()).andReturn(SESSION_ID);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream(true);
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/vnd.oasis.opendocument.spreadsheet");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW.ods\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        odsServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
        verifyZip(os.baos.toByteArray());
        os.baos.close();
    }// end of test step testIfOdsReturnedForADataset

    /**
     * test if ODS output is responded for a valid table.
     *
     * @throws Exception if test fails
     */
    public void testIfOdsReturnedForATable() throws Exception {
        // initialize for step
        initializeForStep();

        // record for request & context
        EasyMock.expect(requestMock.getParameter("id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("type")).andReturn("tbl");
        EasyMock.expect(requestMock.getSession()).andReturn(sessionMock);
        EasyMock.expect(sessionMock.getId()).andReturn(SESSION_ID);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream(true);
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType("application/vnd.oasis.opendocument.spreadsheet");
        Capture<Integer> contentLength = new Capture<Integer>();
        responseMock.setContentLength(EasyMock.capture(contentLength));
        responseMock.setHeader("Content-Disposition", "attachment; filename=\"NiD_testW_NiD_GW_Conc.ods\"");

        // replay all mocks
        replyAllMocks();

        // call method to be tested
        odsServletUnderTest.service(requestMock, responseMock);

        // perform checks and verify mocks
        Assert.assertTrue("Nothing is written to os", os.numberOfBytesWritten > 0);
        Assert.assertTrue("Content-Length is not correct", os.numberOfBytesWritten == contentLength.getValue());
        verifyAllMocks();
        verifyZip(os.baos.toByteArray());
        os.baos.close();
    }// end of test step testIfOdsReturnedForATable

    /**
     * test if an exception thrown when object type is null
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjTypeIsNull() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing request parameter: type";

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("type")).andReturn(null);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjTypeIsNull

    /**
     * test if an exception thrown when object type is empty
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjTypeIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing request parameter: type";

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn("4");
        EasyMock.expect(requestMock.getParameter("type")).andReturn("");
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjTypeIsEmpty

    /**
     * test if an exception thrown when object id is empty
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjIdIsEmpty() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing request parameter: id";

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn("");
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdIsEmpty

    /**
     * test if an exception thrown when object id is null
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjIdIsNull() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Missing request parameter: id";

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn(null);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdIsNull

    /**
     * test if an exception thrown when object id is not valid
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjIdNotValid() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Dataset not found: 300500";

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn("300500");
        EasyMock.expect(requestMock.getParameter("type")).andReturn("dst");
        EasyMock.expect(requestMock.getSession()).andReturn(sessionMock);
        EasyMock.expect(sessionMock.getId()).andReturn(SESSION_ID);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdNotValid

    /**
     * test if an exception thrown when object id is not valid
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjIdNotValidTableId() throws Exception {
        // initialize for step
        initializeForStep();
        String exceptionMessage = "Table not found: 300500";

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn("300500");
        EasyMock.expect(requestMock.getParameter("type")).andReturn("tbl");
        EasyMock.expect(requestMock.getSession()).andReturn(sessionMock);
        EasyMock.expect(sessionMock.getId()).andReturn(SESSION_ID);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjIdNotValidTableId

    /**
     * test if an exception thrown when first object type is invalid
     *
     * @throws Exception if test fails
     */
    public void testIfExceptionThrownWhenObjTypeIsInvalid() throws Exception {
        // initialize for step
        initializeForStep();
        String objType = "TBLX";
        String exceptionMessage = "Unknown object type: " + objType;

        // record for request
        EasyMock.expect(requestMock.getParameter("id")).andReturn("5");
        EasyMock.expect(requestMock.getParameter("type")).andReturn(objType);
        EasyMock.expect(requestMock.getSession()).andReturn(sessionMock);
        EasyMock.expect(sessionMock.getId()).andReturn(SESSION_ID);
        EasyMock.expect(requestMock.getParameter("keep_working_folder")).andReturn(null);

        // record for response
        EnvServletOutputStream os = new EnvServletOutputStream();
        EasyMock.expect(responseMock.getOutputStream()).andReturn(os);
        responseMock.setContentType(null);
        responseMock.sendError(500, exceptionMessage);

        // replay all mocks
        replyAllMocks();

        try {
            // call method to be tested
            odsServletUnderTest.service(requestMock, responseMock);
            Assert.fail("Exception is not received");
        } catch (ServletException e) {
            // perform exception check
            Assert.assertEquals("Incorrect exception message", exceptionMessage, e.getMessage());
        }

        // perform checks and verify mocks
        verifyAllMocks();
    }// end of test step testIfExceptionThrownWhenObjTypeIsInvalid

    @Override
    protected String getSeedFilename() {
        return "seed-two-datasets.xml";
    }

    private static class EnvServletOutputStream extends ServletOutputStream {

        private int numberOfBytesWritten = 0;
        private ByteArrayOutputStream baos = null;
        private boolean saveInput = false;

        private EnvServletOutputStream() {
        }

        private EnvServletOutputStream(boolean saveInput) {
            this.saveInput = saveInput;
            if (this.saveInput) {
                baos = new ByteArrayOutputStream();
            }
        }

        @Override
        public void write(int b) throws IOException {
            this.numberOfBytesWritten++;
            if (this.saveInput) {
                this.baos.write(b);
            }
        }
    } // end of inner class EnvServletOutputStream

} // end of JUnit test class OdsServletTest
