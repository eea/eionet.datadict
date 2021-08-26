package eionet.meta.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.exports.VocabularyOutputHelper;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.service.ServiceException;
import eionet.util.SecurityUtil;
import eionet.web.action.ErrorActionBean;
import eionet.web.action.VocabularyFolderActionBean;

/**
 * Tests for VocabularyFolderActionBean.
 *
 * @author Kaido Laine
 */
public class VocabularyFolderActionBeanTestIT extends DDDatabaseTestCase {
    /**
     * Used instead of site prefix.
     */
    private static final String BASE_URL = "http://test.tripledev.ee/datadict";

    /**
     * Name for the request attribute via which we inject rich-type (e.g. file bean) request parameters for the action bean.
     */
    public static final String RICH_TYPE_REQUEST_PARAMS_ATTR_NAME = "RICH_TYPE_REQUEST_PARAMS";

    private static MockServletContext ctxWithProperyBinder;
    
    /**
     * test if CSV output contains collection resource for a folder.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testCsvContainsElements() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "wise");
        trip.addParameter("vocabularyFolder.identifier", "BWClosed");
        trip.execute("csv");

        String output = trip.getOutputString();

        String expectedRelatedInternal = BASE_URL + "/vocabulary/wise/BWClosed/YP";
        // escapeIRI(contextRoot + elem.getRelatedConceptIdentifier()

        Assert.assertTrue("Incorrect size of bound elements",
                StringUtils.contains(output, "\"skos:relatedMatch@lang\",\"skos:relatedMatch@lang\",\"skos:relatedMatch@lang\""));
        Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                StringUtils.contains(output, "\"http://url1.com\",\"http://url2.com\""));
        Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                StringUtils.contains(output, "\"http://url3.com\",\"http://url4.com\",\"http://url0.com\""));// ordered by concept
        // id

        Assert.assertTrue("Output does not contain correct geo:lat", StringUtils.contains(output, "\"2.2\",\"3\",\"4.5\",\"1\""));
        Assert.assertTrue("Incorrect size of bound elements",
                StringUtils.contains(output, "\"geo:lat@lang\",\"geo:lat@lang\",\"geo:lat@lang\",\"geo:lat@lang\""));

        Assert.assertTrue("Output does not contain correct geo:long", StringUtils.contains(output, "\"1.1\""));
        Assert.assertTrue("Incorrect size of bound elements", StringUtils.contains(output, "\"geo:long\""));

        Assert.assertTrue("Incorrect related element url", StringUtils.contains(output, expectedRelatedInternal));
    }

    /**
     * test if CSV output contains attribute elements Assumption: This test will work for only cases when content does not include
     * comma (,). For simplicity of test case, this is preferred.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testCsvContainsAttributeElementsCorreclty() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.execute("csv");

        byte[] outputBytes = trip.getOutputString().getBytes();
        // remove bom
        String output = new String(outputBytes, 3, outputBytes.length - 3, CharEncoding.UTF_8);

        String expectedRelatedInternal = BASE_URL + "/vocabulary/csv_header_vs/csv_header_vocab/";

        // Construct each rows comma seperated items (columns)
        ArrayList<ArrayList<String>> allRows = new ArrayList<ArrayList<String>>();
        ArrayList<String> header = new ArrayList<String>();
        allRows.add(header);
        ArrayList<String> concept1 = new ArrayList<String>();
        allRows.add(concept1);
        ArrayList<String> concept2 = new ArrayList<String>();
        allRows.add(concept2);
        ArrayList<String> concept3 = new ArrayList<String>();
        allRows.add(concept3);

        // 1. column
        header.add("URI");
        concept1.add(expectedRelatedInternal + "csv_test_concept_1");
        concept2.add(expectedRelatedInternal + "csv_test_concept_2");
        concept3.add(expectedRelatedInternal + "csv_test_concept_3");

        // 2. column
        header.add("Label");
        concept1.add("csv_test_concept_label_1");
        concept2.add("csv_test_concept_label_2");
        concept3.add("csv_test_concept_label_3");

        // 3. column
        header.add("Definition");
        concept1.add("");
        concept2.add("");
        concept3.add("csv_test_concept_def_3");

        // 4. column
        header.add("Notation");
        concept1.add("");
        concept2.add("");
        concept3.add("");

        // 5. column
        header.add("Status");
        concept1.add(StandardGenericStatus.VALID.getNotation());
        concept2.add(StandardGenericStatus.VALID.getNotation());
        concept3.add(StandardGenericStatus.VALID.getNotation());

        // 6. column
        header.add("AcceptedDate");
        concept1.add("");
        concept2.add("");
        concept3.add("");

        // 7. column
        header.add("skos:prefLabel@et");
        concept1.add("et_csv_test_concept_1");
        concept2.add("");
        concept3.add("");

        // 8. column
        header.add("skos:prefLabel@bg");
        concept1.add("bg_csv_test_concept_1");
        concept2.add("");
        concept3.add("bg_csv_test_concept_3");

        // 9. column
        header.add("skos:prefLabel@bg");
        concept1.add("bg2_csv_test_concept_1");
        concept2.add("");
        concept3.add("");

        // 10. column
        header.add("skos:prefLabel@en");
        concept1.add("en_csv_test_concept_1");
        concept2.add("");
        concept3.add("en_csv_test_concept_3");

        // 11. column
        header.add("skos:definition@de");
        concept2.add("de_csv_test_concept_2");
        concept3.add("de_csv_test_concept_3");

        // 12. column
        header.add("skos:definition@de");
        concept2.add("de2_csv_test_concept_2");
        concept3.add("");

        // 13. column
        header.add("AnotherCode@lang");
        concept2.add("");
        concept3.add("HCO2_csv_test_concept_3");

        // 14. column
        header.add("AnotherCode@lang");
        concept2.add("");
        concept3.add("HCO2_2_csv_test_concept_3");

        // 15. column
        header.add("skos:definition@pl");
        concept2.add("");
        concept3.add("pl_csv_test_concept_3");

        // 16. column
        header.add("skos:definition@en");
        concept2.add("");
        concept3.add("en_csv_test_concept_3");

        // 17. column
        header.add("geo:lat@lang");
        concept2.add("");
        concept3.add("HCO3_csv_test_concept_3");

        // 18. column
        header.add("skos:prefLabel@pl");
        concept2.add("");
        concept3.add("pl_csv_test_concept_3");

        String outputCleared = output.replaceAll("[\"]", "");

        String lines[] = outputCleared.split("\\r?\\n");

        for (int i = 0; i < allRows.size(); i++) {
            ArrayList<String> row = allRows.get(i);
            String[] elementsInRow = lines[i].split("[,]");
            for (int j = 0; j < elementsInRow.length; j++) {
                Assert.assertTrue("[" + (i + 1) + ", " + (j + 1) + "] does not match. Expected: " + row.get(j) + ", Actual: "
                        + elementsInRow[j], StringUtils.equals(row.get(j), elementsInRow[j]));
            }
            if (elementsInRow.length < row.size()) {
                for (int j = elementsInRow.length; j < row.size(); j++) {
                    Assert.assertTrue("[" + (i + 1) + ", " + (j + 1) + "] does not match for empty.",
                            StringUtils.isEmpty(row.get(j)));
                }
            }
        }
    }// end of test step testCsvContainsAttributeElementsCorreclty

    /**
     * test if CSV output contains attribute elements 2 Assumption: This test will work for only cases when content does not include
     * comma (,). For simplicity of test case, this is preferred.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testCsvContainsAttributeElementsCorrecltyWithBase() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "another");
        trip.execute("csv");

        byte[] outputBytes = trip.getOutputString().getBytes();
        // remove bom
        String output = new String(outputBytes, 3, outputBytes.length - 3, CharEncoding.UTF_8);

        String expectedRelatedInternal = "http://test.tripledev.ee/";

        // Construct each rows comma seperated items (columns)
        ArrayList<ArrayList<String>> allRows = new ArrayList<ArrayList<String>>();
        ArrayList<String> header = new ArrayList<String>();
        allRows.add(header);
        ArrayList<String> concept1 = new ArrayList<String>();
        allRows.add(concept1);
        ArrayList<String> concept2 = new ArrayList<String>();
        allRows.add(concept2);

        // 1. column
        header.add("URI");
        concept1.add(expectedRelatedInternal + "destination");
        concept2.add(expectedRelatedInternal + "base");

        // 2. column
        header.add("Label");
        concept1.add("referenced");
        concept2.add("referring");

        // 3. column
        header.add("Definition");
        concept1.add("referenced concept");
        concept2.add("referring concept");

        // 4. column
        header.add("Notation");
        concept1.add("");
        concept2.add("");

        // 5. column
        header.add("Status");
        concept1.add(StandardGenericStatus.VALID.getNotation());
        concept2.add(StandardGenericStatus.VALID.getNotation());

        // 6. column
        header.add("AcceptedDate");
        concept1.add("");
        concept2.add("");

        // 7. column
        header.add("skos:relatedMatch@lang");
        concept1.add("http://url0.com");
        concept2.add(expectedRelatedInternal + "destination");

        // 8. column
        header.add("skos:relatedMatch@lang");
        concept1.add(BASE_URL + "/vocabulary/wise/BWClosed/YP");
        concept2.add("");

 

        String outputCleared = output.replaceAll("[\"]", "");
        String lines[] = outputCleared.split("\\r?\\n");
        for (int i = 0; i < allRows.size(); i++) {
            ArrayList<String> row = allRows.get(i);
            String[] elementsInRow = lines[i].split("[,]");
            for (int j = 0; j < elementsInRow.length; j++) {
                Assert.assertTrue("[" + (i + 1) + ", " + (j + 1) + "] does not match. Expected: " + row.get(j) + ", Actual: "
                        + elementsInRow[j], StringUtils.equals(row.get(j), elementsInRow[j]));
            }
            if (elementsInRow.length < row.size()) {
                for (int j = elementsInRow.length; j < row.size(); j++) {
                    Assert.assertTrue("[" + (i + 1) + ", " + (j + 1) + "] does not match for empty.",
                            StringUtils.isEmpty(row.get(j)));
                }
            }
        }
    }// end of test step testCsvContainsAttributeElementsCorrecltyWithBase

    /**
     * test when found vocabulary identifier is requested.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testFoundVocabularySetAndIdentifier() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "wise");
        trip.addParameter("vocabularyFolder.identifier", "BWaterCat");
        trip.execute("view");
        Assert.assertTrue("Incorrect forward BASE_URL",
                StringUtils.equals(trip.getForwardUrl(), "/pages/vocabularies/viewVocabularyFolder.jsp"));
    }

    /**
     * test when found vocabulary concept is requested.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testFoundVocabularyConcept() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "wise");
        trip.addParameter("vocabularyFolder.identifier", "BWaterCat");
        trip.addParameter("vocabularyConcept.identifier", "1");
        trip.execute("view");
        Assert.assertTrue("Incorrect forward BASE_URL",
                StringUtils.equals(trip.getForwardUrl(), "/pages/vocabularies/viewVocabularyFolder.jsp"));
    }

    /**
     * test when vocabulary identifier is null.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testNullVocabularyIdentifier() throws Exception {
        try {
            MockServletContext ctx = ActionBeanUtils.getServletContext();
            MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
            trip.addParameter("vocabularyFolder.folderName", "wise");
            trip.execute("view");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            HashMap<String, Object> errorParameters = se.getErrorParameters();
            Assert.assertNotNull("Error parameters of ServiceException is null", errorParameters);
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Incorrect error type", ((ErrorActionBean.ErrorType) errorParameters
                    .get(ErrorActionBean.ERROR_TYPE_KEY)).equals(ErrorActionBean.ErrorType.NOT_FOUND_404));
        }
    }

    /**
     * test if not found vocabulary identifier is requested.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testNotFoundVocabularyIdentifier() throws Exception {
        try {
            MockServletContext ctx = ActionBeanUtils.getServletContext();
            MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
            trip.addParameter("vocabularyFolder.folderName", "wise");
            trip.addParameter("vocabularyFolder.identifier", "no-such-vocabulary");
            trip.execute("view");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            HashMap<String, Object> errorParameters = se.getErrorParameters();
            Assert.assertNotNull("Error parameters of ServiceException is null", errorParameters);
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Incorrect error type", ((ErrorActionBean.ErrorType) errorParameters
                    .get(ErrorActionBean.ERROR_TYPE_KEY)).equals(ErrorActionBean.ErrorType.NOT_FOUND_404));
        }
    }

    /**
     * test if not found vocabulary set is requested.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testNotFoundVocabularySet() throws Exception {
        try {
            MockServletContext ctx = ActionBeanUtils.getServletContext();
            MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
            trip.addParameter("vocabularyFolder.folderName", "no-such-vocab-set");
            trip.addParameter("vocabularyFolder.identifier", "BWaterCat");
            trip.execute("view");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            HashMap<String, Object> errorParameters = se.getErrorParameters();
            Assert.assertNotNull("Error parameters of ServiceException is null", errorParameters);
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Incorrect error type", ((ErrorActionBean.ErrorType) errorParameters
                    .get(ErrorActionBean.ERROR_TYPE_KEY)).equals(ErrorActionBean.ErrorType.NOT_FOUND_404));
        }
    }

    /**
     * test if not found vocabulary concept is requested.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testNotFoundVocabularyConcept() throws Exception {
        try {
            MockServletContext ctx = ActionBeanUtils.getServletContext();
            MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
            trip.addParameter("vocabularyFolder.folderName", "wise");
            trip.addParameter("vocabularyFolder.identifier", "BWaterCat");
            trip.addParameter("vocabularyConcept.identifier", "no-such-thing");
            trip.execute("view");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            HashMap<String, Object> errorParameters = se.getErrorParameters();
            Assert.assertNotNull("Error parameters of ServiceException is null", errorParameters);
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Error parameters does not include error type",
                    errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY));
            Assert.assertTrue("Incorrect error type", ((ErrorActionBean.ErrorType) errorParameters
                    .get(ErrorActionBean.ERROR_TYPE_KEY)).equals(ErrorActionBean.ErrorType.NOT_FOUND_404));
        }
    }// end of test step testNotFoundVocabularyConcept

    /**
     * test when an CSV file is uploaded for non-working copy folder Note: all success conditions are tested in service test, steps
     * are not repeated here. See: CSVVocabularyImportServiceTest
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadCsvToNotWorkingCopy() throws Exception {
        try {
            uploadFileToNotWorkingCopy("uploadCsv");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "Vocabulary should be in working copy status", se.getMessage());
        }
    }// end of test step testUploadCsvToNotWorkingCopy

    /**
     * test when an CSV file is uploaded for non-authenticated user copy folder
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadCsvForNotAuthenticatedUser() throws Exception {
        try {
            uploadFileForNotAuthenticatedUser("uploadCsv");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "User must be logged in", se.getMessage());
        }
    }// end of test step testUploadCsvForNotAuthenticatedUser

    /**
     * test when an CSV file is uploaded for not-owned user copy folder
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadCsvForNotOwnedUser() throws Exception {
        try {
            uploadFileForNotOwnedUser("uploadCsv");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "Illegal user for viewing this working copy", se.getMessage());
        }
    }// end of test step testUploadCsvForNotOwnedUser

    /**
     * test when a null CSV file is uploaded
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadNullCsv() throws Exception {
        try {
            uploadNullFile("uploadCsv");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "You should upload a file", se.getMessage());
        }
    }// end of test step testUploadNullCsv

    /**
     * test when a empty name file us uploaded for CSV import.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadCsvWithEmptyName() throws Exception {
        try {
            uploadEmptyNameFile("uploadCsv");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "File should be a CSV file", se.getMessage());
        }
    } // end of test step testUploadCsvWithEmptyName

    /**
     * test when a null CSV file is uploaded
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadCsvWithNotCsvExtension() throws Exception {
        try {
            uploadFileWithEnvExtension("uploadCsv");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "File should be a CSV file", se.getMessage());
        }
    }// end of test step testUploadCsvWithNotCsvExtension

    /**
     * test when an RDF file is uploaded for non-working copy folder Note: all success conditions are tested in service test, steps
     * are not repeated here. See: RDFVocabularyImportServiceTest
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadRdfToNotWorkingCopy() throws Exception {
        try {
            uploadFileToNotWorkingCopy("uploadRdf");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "Vocabulary should be in working copy status", se.getMessage());
        }
    }// end of test step testUploadRdfToNotWorkingCopy

    /**
     * test when an RDF file is uploaded for non-authenticated user copy folder
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadRdfForNotAuthenticatedUser() throws Exception {
        try {
            uploadFileForNotAuthenticatedUser("uploadRdf");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "User must be logged in", se.getMessage());
        }
    }// end of test step testUploadRdfForNotAuthenticatedUser

    /**
     * test when an RDF file is uploaded for not-owned user copy folder
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadRdfForNotOwnedUser() throws Exception {
        try {
            uploadFileForNotOwnedUser("uploadRdf");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "Illegal user for viewing this working copy", se.getMessage());
        }
    }// end of test step testUploadRdfForNotOwnedUser

    /**
     * test when a null RDF file is uploaded
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadNullRdf() throws Exception {
        try {
            uploadNullFile("uploadRdf");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "You should upload a file", se.getMessage());
        }
    }// end of test step testUploadNullRdf

    /**
     * test when an empty name file is uploaded for RDF import.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadRdfWithEmptyName() throws Exception {
        try {
            uploadEmptyNameFile("uploadRdf");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "File should be a RDF file", se.getMessage());
        }
    } // end of test step testUploadRdfWithEmptyName

    /*
     * Upload a file with env extension.
     */
    private void uploadFileWithEnvExtension(String execute) throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("vocabularyFolder.workingCopy", "1");
        Map<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        FileBean uploadedRdfFile = new FileBean(null, "something", "uploadedfile.env");
        richTypeRequestParams.put("uploadedFileToImport", uploadedRdfFile);
        trip.getRequest().setAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);
        // call test method
        trip.execute(execute);
    }// end of method uploadFileWithEnvExtension

    /*
     * upload a file with no name
     */
    private void uploadEmptyNameFile(String execute) throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("vocabularyFolder.workingCopy", "1");
        // Prepare rich-type (e.g. file bean) request parameters. These will be picked up by MyActionBeanPropertyBinder
        // that has already been injected into the servlet context mock obtained above.
        Map<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        FileBean uploadedRdfFile = new FileBean(null, "something", "");
        richTypeRequestParams.put("uploadedFileToImport", uploadedRdfFile);
        trip.getRequest().setAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);
        // call test method
        trip.execute(execute);
    } // end of method uploadEmptyNameFile

    /*
     * uploads null file.
     */
    private void uploadNullFile(String execute) throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("vocabularyFolder.workingCopy", "1");
        // call test method
        trip.execute(execute);
    }// end of method uploadNullFile

    /*
     * uploads a file for not-owned user copy folder
     */
    private void uploadFileForNotOwnedUser(String execute) throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser2", "testUser2");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("vocabularyFolder.workingCopy", "1");
        // call test method
        trip.execute(execute);
    }// end of method uploadFileForNotOwnedUser

    /*
     * uploads file to non-working copy folder.
     */
    private void uploadFileToNotWorkingCopy(String execute) throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("vocabularyFolder.workingCopy", "0");
        // call test method
        trip.execute(execute);
    }// end of method uploadFileToNotWorkingCopy

    /*
     * upload a file for non-authenticated user copy folder
     */
    private void uploadFileForNotAuthenticatedUser(String execute) throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("vocabularyFolder.workingCopy", "1");
        // call test method
        trip.execute(execute);
    }// end of method uploadFileForNotAuthenticatedUser

    @Override
    protected String getSeedFilename() {
        return "seed-vocabularycsv.xml";
    }

    /**
     * This method creates and returns a mock servlet context with a property finder to be used with file bean
     *
     * @return
     */
    private MockServletContext getServletContextWithProperyBinder() {
        if (VocabularyFolderActionBeanTestIT.ctxWithProperyBinder == null) {
            MockServletContext ctx = new MockServletContext("test");

            Map<String, String> filterParams = new HashMap<String, String>();

            filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
            filterParams.put("ActionResolver.Packages", "eionet.web.action");

            filterParams.put("ActionBeanContext.Class", "eionet.web.DDActionBeanContext");
            filterParams.put("ActionBeanPropertyBinder.Class",
                    "eionet.meta.web.action.VocabularyFolderActionBeanTestIT$MyActionBeanPropertyBinder");

            ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
            ctx.addInitParameter("contextConfigLocation", "classpath:mock-spring-context.xml");

            ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

            ContextLoaderListener springContextLoader = new ContextLoaderListener();
            springContextLoader.contextInitialized(new ServletContextEvent(ctx));
            VocabularyFolderActionBeanTestIT.ctxWithProperyBinder = ctx;
        }
        
        return ctxWithProperyBinder;
    }// end of method getServletContextWithProperyBinder

    /**
     * test if JSON output format is correct. Main purpose of this step is to test output format of json.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputFormat() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "wise");
        trip.addParameter("vocabularyFolder.identifier", "BWClosed");
        trip.execute("json");

        String output = trip.getOutputString();

        String[] conceptIdentifiers = new String[] {"N", "YP", "YT"};
        String[] conceptLabels = new String[] {"Not Closed", "Yes - permanently", "Yes - temporarily"};

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/wise/BWClosed/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);


                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "2", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "123", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "BWClosed", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "Bathing Water Closed status", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Public draft", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "10", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "wise", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "10, wise, WISE - Water Information System for Europe", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            // iterate on concepts
            for (int i = 0; i < 3; i++) {
                Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                // move to field id
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Id Value", conceptIdentifiers[i], val);
                // move to field type
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Type", VocabularyJSONOutputHelper.JSON_LD_TYPE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Type Value", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // pref labels array
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                {
                    Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                    // move to field value
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Value", VocabularyJSONOutputHelper.JSON_LD_VALUE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Value Value", conceptLabels[i], val);
                    // move to field language
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);
                    Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                // After changes in #110776
                // move to VocabularyConceptId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyConceptId", "VocabularyConceptId", val);
                parser.nextToken(); // move to value
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId", "2", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                // move to Definition
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Definition", "Definition", val);
                parser.nextToken(); // move to value
                // move to Notation
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Notation", "Notation", val);
                parser.nextToken(); // move to value
                // move to Status
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Status", "Status", val);
                parser.nextToken(); // move to value

                //Changes for #136471
                //in seed-vocabularycsv.xml which is the used xml there are no StatusModifiedDate and AcceptedDate values for all concepts so we move on to data elements

                // move to data elements
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 1", "CountryCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 2", "AnotherCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 3", "skos:definition", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 4", "geo:long", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0 || i==2){ //vocabularyConceptId = 5 or vocabularyConceptId = 7
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 5", "geo:lat", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
               if(i==0){ //vocabularyConceptId = 5
                   parser.nextToken(); // move to value
                   parser.nextToken(); // move to next value
                   parser.nextToken(); // move to next value
                   parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 6", "skos:relatedMatch", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0){ //vocabularyConceptId = 5
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                else if(i==1){ //vocabularyConceptId = 6
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 7", "skos:related", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==2){ //vocabularyConceptId = 7
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
                Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            }
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
    } // end of test step testJsonOutputFormat

    /**
     * test when an unsupported json output format requested.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonUnsupportedFormat() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "wise");
        trip.addParameter("vocabularyFolder.identifier", "BWClosed");
        trip.addParameter("format", "unsupportedformat");
        trip.execute("json");
        String outputString = trip.getOutputString();
        Assert.assertTrue("Output string is not empty", StringUtils.isBlank(outputString));
    }// end of test step testJsonUnsupportedFormat

    /**
     * test JSON output with language.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputWithLang() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("lang", "bg");
        trip.execute("json");

        String output = trip.getOutputString();

        String[] conceptIdentifiers =
                new String[] {"csv_test_concept_1", "csv_test_concept_2", "csv_test_concept_3", "csv_test_concept_15"};
        String[] conceptLabels =
                new String[] {"bg_csv_test_concept_1", "csv_test_concept_label_2", "bg_csv_test_concept_3",
                        "Ecsv_test_concept_label_15"};
        String[] conceptLanguages =
                new String[] {"bg", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, "bg", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE};

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/csv_header_vs/csv_header_vocab/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", "bg", val);

                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "4", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "124", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "csv_header_vocab", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "csv_header_vocab_test", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Released", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "5", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "csv_header_vs", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "csv_header_vs_test", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            // iterate on concepts
            for (int i = 0; i < 4; i++) {
                Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                // move to field id
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Id Value", conceptIdentifiers[i], val);
                // move to field type
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Type", VocabularyJSONOutputHelper.JSON_LD_TYPE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Type Value", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // pref labels array
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                {
                    Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                    // move to field value
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Value", VocabularyJSONOutputHelper.JSON_LD_VALUE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Value Value", conceptLabels[i], val);
                    // move to field language
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Language Value", conceptLanguages[i], val);


                    Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
                // After changes in #110776
                // move to VocabularyConceptId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyConceptId", "VocabularyConceptId", val);
                parser.nextToken(); // move to value
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId", "4", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                // move to Definition
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Definition", "Definition", val);
                parser.nextToken(); // move to value
                // move to Notation
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Notation", "Notation", val);
                parser.nextToken(); // move to value
                // move to Status
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Status", "Status", val);
                parser.nextToken(); // move to value

                //Changes for #136471
                //in seed-vocabularycsv.xml which is the used xml there are no StatusModifiedDate and AcceptedDate values for all concepts so we move on to data elements

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 2", "AnotherCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==2){ //vocabularyConceptId = 10
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 5", "geo:lat", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==2){ //vocabularyConceptId = 10
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 8", "skos:prefLabel", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0){ //vocabularyConceptId = 8
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                else if(i==2){ //vocabularyConceptId = 10
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 9", "skos:definition", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){ //vocabularyConceptId = 9
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                else if(i==2){ //vocabularyConceptId = 10
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY


                Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            }
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
    } // end of test step testJsonOutputWithLang

    /**
     * test JSON output with concept identifier.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputWithIdentifier() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "wise");
        trip.addParameter("vocabularyFolder.identifier", "BWClosed");
        trip.addParameter("id", "y");
        trip.execute("json");

        String output = trip.getOutputString();

        String[] conceptIdentifiers = new String[] {"YP", "YT"};
        String[] conceptLabels = new String[] {"Yes - permanently", "Yes - temporarily"};

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/wise/BWClosed/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);

                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "2", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "123", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "BWClosed", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "Bathing Water Closed status", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Public draft", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "10", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "wise", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "10, wise, WISE - Water Information System for Europe", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            // iterate on concepts
            for (int i = 0; i < 2; i++) {
                Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                // move to field id
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Id Value", conceptIdentifiers[i], val);
                // move to field type
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Type", VocabularyJSONOutputHelper.JSON_LD_TYPE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Type Value", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // pref labels array
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                {
                    Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                    // move to field value
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Value", VocabularyJSONOutputHelper.JSON_LD_VALUE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Value Value", conceptLabels[i], val);
                    // move to field language
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);
                    Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                // After changes in #110776
                // move to VocabularyConceptId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyConceptId", "VocabularyConceptId", val);
                parser.nextToken(); // move to value
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId", "2", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                // move to Definition
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Definition", "Definition", val);
                parser.nextToken(); // move to value
                // move to Notation
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Notation", "Notation", val);
                parser.nextToken(); // move to value
                // move to Status
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Status", "Status", val);
                parser.nextToken(); // move to value

                //Changes for #136471
                //in seed-vocabularycsv.xml which is the used xml there are no StatusModifiedDate and AcceptedDate values for all concepts so we move on to data elements

                // move to data elements
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 1", "CountryCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 2", "AnotherCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 3", "skos:definition", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 4", "geo:long", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 5", "geo:lat", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 6", "skos:relatedMatch", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 7", "skos:related", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
                Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            }
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
    } // end of test step testJsonOutputWithIdentifier

    /**
     * test JSON output with label.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputWithLabel() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("label", "E");
        trip.execute("json");

        String output = trip.getOutputString();

        String[] conceptIdentifiers = new String[] {"csv_test_concept_1", "csv_test_concept_3", "csv_test_concept_15"};
        String[][] conceptLabels =
                new String[][] { {"csv_test_concept_label_1", "en_csv_test_concept_1", "et_csv_test_concept_1"},
                        {"csv_test_concept_label_3", "en_csv_test_concept_3"}, {"Ecsv_test_concept_label_15"}};
        String[][] conceptLanguages =
                new String[][] { {VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, "en", "et"},
                        {VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, "en"}, {VocabularyJSONOutputHelper.DEFAULT_LANGUAGE}};

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/csv_header_vs/csv_header_vocab/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);

                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "4", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "124", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "csv_header_vocab", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "csv_header_vocab_test", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Released", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "5", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "csv_header_vs", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "csv_header_vs_test", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            // iterate on concepts
            for (int i = 0; i < 3; i++) {
                Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                // move to field id
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Id Value", conceptIdentifiers[i], val);
                // move to field type
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Type", VocabularyJSONOutputHelper.JSON_LD_TYPE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Type Value", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // pref labels array
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                {
                    for (int j = 0; j < conceptLabels[i].length; j++) {
                        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                        // move to field value
                        parser.nextToken();
                        val = parser.getCurrentName();
                        Assert.assertEquals("Value", VocabularyJSONOutputHelper.JSON_LD_VALUE, val);
                        parser.nextToken(); // move to value
                        val = parser.getText();
                        Assert.assertEquals("Value Value", conceptLabels[i][j], val);
                        // move to field language
                        parser.nextToken();
                        val = parser.getCurrentName();
                        Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                        parser.nextToken(); // move to value
                        val = parser.getText();
                        Assert.assertEquals("Language Value", conceptLanguages[i][j], val);
                        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                    }
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                // After changes in #110776
                // move to VocabularyConceptId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyConceptId", "VocabularyConceptId", val);
                parser.nextToken(); // move to value
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId", "4", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                // move to Definition
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Definition", "Definition", val);
                parser.nextToken(); // move to value
                // move to Notation
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Notation", "Notation", val);
                parser.nextToken(); // move to value
                // move to Status
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Status", "Status", val);
                parser.nextToken(); // move to value

                //Changes for #136471
                //in seed-vocabularycsv.xml which is the used xml there are no StatusModifiedDate and AcceptedDate values for all concepts so we move on to data elements

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 2", "AnotherCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 5", "geo:lat", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 8", "skos:prefLabel", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                else if(i==1){
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 9", "skos:definition", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            }
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
    } // end of test step testJsonOutputWithLabel

    /**
     * test JSON output with non-existing label.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputWithNonExistingLabel() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("label", "de");
        trip.execute("json");

        String output = trip.getOutputString();

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/csv_header_vs/csv_header_vocab/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);

                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "4", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "124", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "csv_header_vocab", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "csv_header_vocab_test", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Released", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "5", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "csv_header_vs", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "csv_header_vs_test", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
    } // end of test step testJsonOutputWithNonExistingLabel

    /**
     * test JSON output with label and language.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputWithLabelAndLang() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "csv_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "csv_header_vocab");
        trip.addParameter("label", "e");
        trip.addParameter("lang", "et");
        trip.execute("json");

        String output = trip.getOutputString();

        String[] conceptIdentifiers = new String[] {"csv_test_concept_1", "csv_test_concept_3", "csv_test_concept_15"};
        String[] conceptLabels = new String[] {"et_csv_test_concept_1", "en_csv_test_concept_3", "Ecsv_test_concept_label_15"};
        String[] conceptLanguages =
                new String[] {"et", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, VocabularyJSONOutputHelper.DEFAULT_LANGUAGE};

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/csv_header_vs/csv_header_vocab/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", "et", val);

                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "4", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "124", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "csv_header_vocab", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "csv_header_vocab_test", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Released", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "5", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "csv_header_vs", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "csv_header_vs_test", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            // iterate on concepts
            for (int i = 0; i < 3; i++) {
                Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                // move to field id
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Id Value", conceptIdentifiers[i], val);
                // move to field type
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Type", VocabularyJSONOutputHelper.JSON_LD_TYPE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Type Value", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // pref labels array
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                {
                    Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                    // move to field value
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Value", VocabularyJSONOutputHelper.JSON_LD_VALUE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Value Value", conceptLabels[i], val);
                    // move to field language
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Language Value", conceptLanguages[i], val);
                    Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                // After changes in #110776
                // move to VocabularyConceptId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyConceptId", "VocabularyConceptId", val);
                parser.nextToken(); // move to value
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId", "4", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                // move to Definition
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Definition", "Definition", val);
                parser.nextToken(); // move to value
                // move to Notation
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Notation", "Notation", val);
                parser.nextToken(); // move to value
                // move to Status
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Status", "Status", val);
                parser.nextToken(); // move to value

                //Changes for #136471
                //in seed-vocabularycsv.xml which is the used xml there are no StatusModifiedDate and AcceptedDate values for all concepts so we move on to data elements

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 2", "AnotherCode", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 5", "geo:lat", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 8", "skos:prefLabel", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                else if(i==1){
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 9", "skos:definition", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1){
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                    parser.nextToken(); // move to next value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            }
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
    } // end of test step testJsonOutputWithLabelAndLang

    /**
     * test if JSON output format is correct with hierarchy.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testJsonOutputFormatWithNarrowerAndBroader() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "json_hierarchical");
        trip.addParameter("vocabularyFolder.identifier", "hierarchical");
        trip.execute("json");

        String output = trip.getOutputString();

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(output);

        final String LABEL_PREFIX = "Label_";

        String[] conceptIdentifiers = new String[] {"A", "A.1", "A.1.1", "A.2", "B", "C", "C.1"};

        String[][] broaders = new String[][] { {}, {"A"}, {"A.1"}, {"A"}, {}, {}, {"C"}};
        String[][] narrowers = new String[][] { {"A.1", "A.2"}, {"A.1.1"}, {}, {}, {}, {"C.1"}, {}};

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // first value is : JsonToken.START_OBJECT
        {
            parser.nextToken(); // context item start
            String val = parser.getCurrentName();
            Assert.assertEquals("Context key", VocabularyJSONOutputHelper.JSON_LD_CONTEXT, val);
            Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
            {
                // move to base
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Base", VocabularyJSONOutputHelper.JSON_LD_BASE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                String baseUri = VocabularyFolderActionBeanTestIT.BASE_URL;
                String expectedRelatedInternal = baseUri + "/vocabulary/json_hierarchical/hierarchical/";
                Assert.assertEquals("Base Uri", expectedRelatedInternal, val);
                // move to skos namespace def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Skos", VocabularyOutputHelper.LinkedDataNamespaces.SKOS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Ns", VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, val);
                // move to concept def
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Concepts", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Concept", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // move to pref Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Pref Label", VocabularyJSONOutputHelper.SKOS_PREF_LABEL, val);
                // move to data element identifier, broader first
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Broader", VocabularyJSONOutputHelper.SKOS_BROADER, val);
                // move to data element identifier, narrower
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Skos Narrower", VocabularyJSONOutputHelper.SKOS_NARROWER, val);
                // move to language
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);

                // After changes in #110776
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId value", "10", val);
                // move to ContinuityId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("ContinuityId", "ContinuityId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("ContinuityId value", "126", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Identifier value", "hierarchical", val);
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Label value", "hierarchical vocabulary", val);
                // move to RegistrationStatus
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("RegistrationStatus", "RegistrationStatus", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("RegistrationStatus value", "Released", val);
                // move to IsWorkingCopy
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsWorkingCopy", "IsWorkingCopy", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsWorkingCopy value", "false", val);
                // move to CheckedOutCopyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("CheckedOutCopyId", "CheckedOutCopyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("CheckedOutCopyId value", "0", val);
                // move to WorkingUser
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("WorkingUser", "WorkingUser", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("WorkingUser value", "testUser", val);
                // move to DateModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("DateModified", "DateModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                // move to UserModified
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("UserModified", "UserModified", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("UserModified value", "null", val);
                // move to IsNumericConceptIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNumericConceptIdentifiers", "IsNumericConceptIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNumericConceptIdentifiers value", "false", val);
                // move to VocabularyType
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyType", "VocabularyType", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyType value", "Common", val);
                // move to FolderId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderId", "FolderId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderId value", "15", val);
                // move to FolderName
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderName", "FolderName", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderName value", "json_hierarchical", val);
                // move to FolderLabel
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("FolderLabel", "FolderLabel", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("FolderLabel value", "json_hierarchical_test", val);
                // move to IsNotationsEqualIdentifiers
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("IsNotationsEqualIdentifiers", "IsNotationsEqualIdentifiers", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("IsNotationsEqualIdentifiers value", "false", val);
            }
            Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            // move to concepts array
            parser.nextToken();
            val = parser.getCurrentName();
            Assert.assertEquals("Concepts array", VocabularyJSONOutputHelper.JSON_LD_CONCEPTS, val);
            Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
            // iterate on concepts
            for (int i = 0; i < 7; i++) {
                Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                // move to field id
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Id Value", conceptIdentifiers[i], val);
                // move to field type
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Type", VocabularyJSONOutputHelper.JSON_LD_TYPE, val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("Type Value", VocabularyJSONOutputHelper.SKOS_CONCEPT, val);
                // pref labels array
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Pref Label", VocabularyJSONOutputHelper.PREF_LABEL, val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                {
                    Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                    // move to field value
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Value", VocabularyJSONOutputHelper.JSON_LD_VALUE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Value Value", LABEL_PREFIX + conceptIdentifiers[i], val);
                    // move to field language
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Language", VocabularyJSONOutputHelper.JSON_LD_LANGUAGE, val);
                    parser.nextToken(); // move to value
                    val = parser.getText();
                    Assert.assertEquals("Language Value", VocabularyJSONOutputHelper.DEFAULT_LANGUAGE, val);
                    Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                // After changes in #110776
                // move to VocabularyConceptId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyConceptId", "VocabularyConceptId", val);
                parser.nextToken(); // move to value
                // move to VocabularyId
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("VocabularyId", "VocabularyId", val);
                parser.nextToken(); // move to value
                val = parser.getText();
                Assert.assertEquals("VocabularyId", "10", val);
                // move to Identifier
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Identifier", "Identifier", val);
                parser.nextToken(); // move to value
                // move to Label
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Label", "Label", val);
                parser.nextToken(); // move to value
                // move to Definition
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Definition", "Definition", val);
                parser.nextToken(); // move to value
                // move to Notation
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Notation", "Notation", val);
                parser.nextToken(); // move to value
                // move to Status
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Status", "Status", val);
                parser.nextToken(); // move to value

                if (broaders[i].length > 0) {
                    // broaders array
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Broader", VocabularyJSONOutputHelper.BROADER, val);
                    Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                    for (int j = 0; j < broaders[i].length; j++) {
                        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                        // move to field id
                        parser.nextToken();
                        val = parser.getCurrentName();
                        Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                        parser.nextToken(); // move to value
                        val = parser.getText();
                        Assert.assertEquals("Id Value", broaders[i][j], val);
                        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                    }
                    Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
                }

                if (narrowers[i].length > 0) {
                    // narrowers array
                    parser.nextToken();
                    val = parser.getCurrentName();
                    Assert.assertEquals("Narrower", VocabularyJSONOutputHelper.NARROWER, val);
                    Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                    for (int j = 0; j < narrowers[i].length; j++) {
                        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken()); // JsonToken.START_OBJECT
                        // move to field id
                        parser.nextToken();
                        val = parser.getCurrentName();
                        Assert.assertEquals("Id", VocabularyJSONOutputHelper.JSON_LD_ID, val);
                        parser.nextToken(); // move to value
                        val = parser.getText();
                        Assert.assertEquals("Id Value", narrowers[i][j], val);
                        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
                    }
                    Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
                }

                //Changes for #136471
                //in seed-vocabularycsv.xml which is the used xml there are no StatusModifiedDate and AcceptedDate values for all concepts so we move on to data elements

                // move to data elements
                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 10", "skos:narrower", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==0){ //vocabularyConceptId = 20
                    parser.nextToken(); // move to value
                    parser.nextToken(); // move to next value
                }
                else if(i==1 || i==5){ //vocabularyConceptId = 21 or vocabularyConceptId = 25
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                parser.nextToken();
                val = parser.getCurrentName();
                Assert.assertEquals("Data Element 11", "skos:broader", val);
                Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken()); // JsonToken.START_ARRAY
                if(i==1 || i==2 || i==3 || i==6){ //vocabularyConceptId = 21 or vocabularyConceptId = 22 or vocabularyConceptId = 23 or vocabularyConceptId = 26
                    parser.nextToken(); // move to value
                }
                Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY

                Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT
            }
            Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken()); // JsonToken.END_ARRAY
        }
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken()); // JsonToken.END_OBJECT

    } // end of test step testJsonOutputFormatWithNarrowerAndBroader

    /**
     * Extension of {@link DefaultActionBeanPropertyBinder} in order to directly inject the proper file bean.
     *
     * @author Jaanus
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class MyActionBeanPropertyBinder extends DefaultActionBeanPropertyBinder {

        /**
         * Default constructor.
         */
        public MyActionBeanPropertyBinder() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder#bind(net.sourceforge.stripes.action.ActionBean,
         * net.sourceforge.stripes.action.ActionBeanContext, boolean)
         */
        @Override
        public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {

            ValidationErrors validationErrors = super.bind(bean, context, validate);

            if (bean != null && context != null) {
                HttpServletRequest request = context.getRequest();
                if (request != null) {
                    Object o = request.getAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME);
                    if (o instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> richTypeRequestParams = (Map<String, Object>) o;
                        for (Entry<String, Object> entry : richTypeRequestParams.entrySet()) {
                            String paramName = entry.getKey();
                            Object paramValue = entry.getValue();
                            BeanUtil.setPropertyValue(paramName, bean, paramValue);
                        }
                    }
                }
            }
            return validationErrors;
        }
    }// end of inner class MyActionBeanPropertyBinder

}// end of test VocabularyFolderActionBean
