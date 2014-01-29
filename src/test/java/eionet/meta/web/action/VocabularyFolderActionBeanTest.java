package eionet.meta.web.action;

import java.util.HashMap;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

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
                StringUtils.contains(output, "\"http://url1.com\",\"http://url2.com\",\"\""));
        Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                StringUtils.contains(output, "\"http://url3.com\",\"http://url4.com\",\"http://url0.com\""));//ordered by concept id

        Assert.assertTrue("Output does not contain correct geo:lat", StringUtils.contains(output, "\"2.2\",\"3\",\"4.5\",\"1\""));
        Assert.assertTrue("Incorrect size of binded elements",
                StringUtils.contains(output, "\"geo:lat\",\"geo:lat\",\"geo:lat\",\"geo:lat\""));
        
        Assert.assertTrue("Output does not contain correct geo:long", StringUtils.contains(output, "\"1.1\""));
        Assert.assertTrue("Incorrect size of binded elements",
                StringUtils.contains(output, "\"geo:long\""));

        Assert.assertTrue("Incorrect related element url", StringUtils.contains(output, expectedRelatedInternal));

    }

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
