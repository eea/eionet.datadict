package eionet.meta.web.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.meta.service.ServiceException;
import eionet.util.SecurityUtil;
import eionet.web.action.ErrorActionBean;
import eionet.web.action.VocabularyFolderActionBean;

/**
 * Tests for VocabularyConteptActionBean.
 *
 * @author Kaido Laine
 */
public class VocabularyFolderActionBeanTest extends DDDatabaseTestCase {
    /**
     * Used instead of site prefix.
     */
    private static final String BASE_URL = "http://test.tripledev.ee/datadict";

    /**
     * Name for the request attribute via which we inject rich-type (e.g. file bean) request parameters for the action bean.
     */
    public static final String RICH_TYPE_REQUEST_PARAMS_ATTR_NAME = "RICH_TYPE_REQUEST_PARAMS";

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
                StringUtils.contains(output, "\"skos:relatedMatch\",\"skos:relatedMatch\",\"skos:relatedMatch\""));
        Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                StringUtils.contains(output, "\"http://url1.com\",\"http://url2.com\""));
        Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                StringUtils.contains(output, "\"http://url3.com\",\"http://url4.com\",\"http://url0.com\""));// ordered by concept
        // id

        Assert.assertTrue("Output does not contain correct geo:lat", StringUtils.contains(output, "\"2.2\",\"3\",\"4.5\",\"1\""));
        Assert.assertTrue("Incorrect size of binded elements",
                StringUtils.contains(output, "\"geo:lat\",\"geo:lat\",\"geo:lat\",\"geo:lat\""));

        Assert.assertTrue("Output does not contain correct geo:long", StringUtils.contains(output, "\"1.1\""));
        Assert.assertTrue("Incorrect size of binded elements", StringUtils.contains(output, "\"geo:long\""));

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
        header.add("StartDate");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        concept1.add(date);
        concept2.add(date);
        concept3.add(date);

        // 6. column
        header.add("EndDate");
        concept1.add("");
        concept2.add("");
        concept3.add("");

        // 7. column
        header.add("AnotherCode");
        concept1.add("");
        concept2.add("");
        concept3.add("HCO2_csv_test_concept_3");

        // 8. column
        header.add("AnotherCode");
        concept1.add("");
        concept2.add("");
        concept3.add("HCO2_2_csv_test_concept_3");

        // 9. column
        header.add("geo:lat");
        concept1.add("");
        concept2.add("");
        concept3.add("HCO3_csv_test_concept_3");

        // 10. column
        header.add("skos:definition@de");
        concept1.add("");
        concept2.add("de_csv_test_concept_2");
        concept3.add("de_csv_test_concept_3");

        // 11. column
        header.add("skos:definition@de");
        concept1.add("");
        concept2.add("de2_csv_test_concept_2");
        concept3.add("");

        // 12. column
        header.add("skos:definition@en");
        concept1.add("");
        concept2.add("");
        concept3.add("en_csv_test_concept_3");

        // 13. column
        header.add("skos:definition@pl");
        concept1.add("");
        concept2.add("");
        concept3.add("pl_csv_test_concept_3");

        // 14. column
        header.add("skos:prefLabel@bg");
        concept1.add("bg_csv_test_concept_1");
        concept2.add("");
        concept3.add("bg_csv_test_concept_3");

        // 15. column
        header.add("skos:prefLabel@bg");
        concept1.add("bg2_csv_test_concept_1");
        concept2.add("");
        concept3.add("");

        // 16. column
        header.add("skos:prefLabel@en");
        concept1.add("en_csv_test_concept_1");
        concept2.add("");
        concept3.add("en_csv_test_concept_3");

        // 17. column
        header.add("skos:prefLabel@et");
        concept1.add("et_csv_test_concept_1");
        concept2.add("");
        concept3.add("");

        // 18. column
        header.add("skos:prefLabel@pl");
        concept1.add("");
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
        header.add("StartDate");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        concept1.add(date);
        concept2.add(date);

        // 6. column
        header.add("EndDate");
        concept1.add("");
        concept2.add("");

        // 7. column
        header.add("skos:relatedMatch");
        concept1.add("http://url0.com");
        concept2.add(expectedRelatedInternal + "destination");

        // 8. column
        header.add("skos:relatedMatch");
        concept1.add(BASE_URL + "/vocabulary/wise/BWClosed/YP");
        concept2.add("");

        // 9. column
        header.add("skos:relatedMatch");
        concept1.add("");
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

    /**
     * test when a non rdf extension file is uploaded for RDF import.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testUploadRdfWithNotRdfExtension() throws Exception {
        try {
            uploadFileWithEnvExtension("uploadRdf");
            Assert.fail("Exception not received for not working copy folder bulk edit.");
        } catch (StripesServletException e) {
            Assert.assertTrue("Incorrect cause of StripesServletException", e.getCause() instanceof ServiceException);
            ServiceException se = (ServiceException) e.getCause();
            Assert.assertEquals("Exception Message is not correct", "File should be a RDF file", se.getMessage());
        }
    }// end of test step testUploadRdfWithNotRdfExtension

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
        MockServletContext ctx = new MockServletContext("test");

        Map<String, String> filterParams = new HashMap<String, String>();

        filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
        filterParams.put("ActionResolver.Packages", "eionet.web.action");

        filterParams.put("ActionBeanContext.Class", "eionet.web.DDActionBeanContext");
        filterParams.put("ActionBeanPropertyBinder.Class",
                "eionet.meta.web.action.VocabularyFolderActionBeanTest$MyActionBeanPropertyBinder");

        ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        ctx.addInitParameter("contextConfigLocation", "classpath:mock-spring-context.xml");

        ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

        ContextLoaderListener springContextLoader = new ContextLoaderListener();
        springContextLoader.contextInitialized(new ServletContextEvent(ctx));
        return ctx;
    }// end of method getServletContextWithProperyBinder

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
