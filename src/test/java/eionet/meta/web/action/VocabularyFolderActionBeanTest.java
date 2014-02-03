package eionet.meta.web.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.web.action.ErrorActionBean;
import eionet.web.action.VocabularyFolderActionBean;

/**
 * Tests for VocabularyConteptActionBean.
 * 
 * @author Kaido Laine
 */
public class VocabularyFolderActionBeanTest extends DDDatabaseTestCase {

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

        String baseUri = Props.getRequiredProperty(PropsIF.DD_URL);

        String expectedRelatedInternal = baseUri + "/vocabulary/wise/BWClosed/YP";
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

        String baseUri = Props.getRequiredProperty(PropsIF.DD_URL);

        String expectedRelatedInternal = baseUri + "/vocabulary/csv_header_vs/csv_header_vocab/";

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
        Assert.assertTrue("Incorrect forward URL",
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
        Assert.assertTrue("Incorrect forward URL",
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
    }

    @Override
    protected String getSeedFilename() {
        return "seed-vocabularycsv.xml";
    }

}
