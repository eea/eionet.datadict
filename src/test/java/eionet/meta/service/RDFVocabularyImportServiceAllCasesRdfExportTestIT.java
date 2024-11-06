package eionet.meta.service;

import eionet.datadict.util.StringUtils;
import eionet.meta.ActionBeanUtils;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.web.action.VocabularyFolderActionBean;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.unitils.spring.annotation.SpringBeanByType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class RDFVocabularyImportServiceAllCasesRdfExportTestIT extends VocabularyImportServiceTestBase{

    /**
     * Vocabulary folder RDF import service.
     */
    @SpringBeanByType
    private IRDFVocabularyImportService vocabularyImportService;

    @Mock
    VocabularyFolderActionBean vocabularyFolderActionBean;

    MockHttpServletRequest request;

    MockHttpServletResponse response;

    @BeforeClass
    public static void loadData() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData("seed-emptydb.xml");
        DBUnitHelper.loadData("rdf_import/seed-vocabularyrdf-import-without-working-copy.xml");
    }

    @Before
    public void setUpMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(vocabularyFolderActionBean.rdf()).thenCallRealMethod();
        when(vocabularyFolderActionBean.getVocabularyService()).thenReturn(vocabularyService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("rdf_import/seed-vocabularyrdf-import-without-working-copy.xml");
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected Reader getReaderFromResource(String resourceLoc) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceLoc);
        InputStreamReader reader = new InputStreamReader(is);

        return reader;
    }// end of method getReaderFromResource

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf they are ignored.
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    @Test
    public void testDontPurgeIgnoreMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.keep);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeIgnoreMissingConcepts.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 5 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(5));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));

    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf they removed.
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    @Test
    public void testDontPurgeRemoveMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.remove);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeRemoveMissingConcepts.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 5 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(5));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to invalid
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToInvalid() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.invalid);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToInvalid.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("notAcceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("notAcceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecated() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.deprecated);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecated.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Retired
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedRetired() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.retired);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedRetired.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Superseded
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.superseded);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf they are ignored.
        If there are extra concepts in the rdf, they are added.
        If an existing concept has a bound element that is not included in the rdf, then that element is maintained for the concept.
     */
    @Test
    public void testPurgePerPredicateIgnoreMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.keep);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgePerPredicateIgnoreMissingConcepts.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 5 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(5));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf they removed
        If there are extra concepts in the rdf, they are added.
        If an existing concept has a bound element that is not included in the rdf, then that element is maintained for the concept.
     */
    @Test
    public void testPurgePerPredicateRemoveMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.remove);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgePerPredicateRemoveMissingConcepts.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 5 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(5));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf are maintained but their status is updated to invalid
        If there are extra concepts in the rdf, they are added.
        If an existing concept has a bound element that is not included in the rdf, then that element is maintained for the concept.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToInvalid() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.invalid);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgePerPredicateMaintainMissingConceptsAndChangeStatusToInvalid.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("notAcceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("notAcceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        /If there are missing concepts in the rdf are maintained but their status is updated to Deprecated
        If there are extra concepts in the rdf, they are added.
        If an existing concept has a bound element that is not included in the rdf, then that element is maintained for the concept.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecated() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.deprecated);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecated.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Retired
        If there are extra concepts in the rdf, they are added.
        If an existing concept has a bound element that is not included in the rdf, then that element is maintained for the concept.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecatedRetired() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.retired);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecatedRetired.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Superseded
        If there are extra concepts in the rdf, they are added.
        If an existing concept has a bound element that is not included in the rdf, then that element is maintained for the concept.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.superseded);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 9 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(9));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
        assertTrue(differences.get(7).toString().contains("statusModified"));
        assertTrue(differences.get(8).toString().contains("acceptedDate"));
    }

    /*
        In this case, all existing concepts will be removed and the imported concepts will be added.
        If there is an existing data element for a non missing concept in the rdf, only the imported value will remain
     */
    @Test
    public void testPurgeAllVocabularyData() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.remove, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.keep);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestPurgeAllVocabularyData.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 7 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(7));
        assertTrue(differences.get(0).toString().contains("dateModified"));
        assertTrue(differences.get(1).toString().contains("statusModified"));
        assertTrue(differences.get(2).toString().contains("acceptedDate"));
        assertTrue(differences.get(3).toString().contains("statusModified"));
        assertTrue(differences.get(4).toString().contains("acceptedDate"));
        assertTrue(differences.get(5).toString().contains("statusModified"));
        assertTrue(differences.get(6).toString().contains("acceptedDate"));
    }

    /*
        In this case, all imported concepts will be removed from the vocabulary.
        If there are extra concepts in the rdf, they are not added.
     */
    @Test
    public void testDeleteAllVocabularyData() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.delete, IVocabularyImportService.MissingConceptsAction.keep);

        //export rdf file and compare it to rdf file from rdfExport folder
        when(vocabularyFolderActionBean.getVocabularyFolder()).thenReturn(oldVocabularyFolder);
        StreamingResolution streamingResolution = (StreamingResolution) vocabularyFolderActionBean.rdf();
        streamingResolution.execute(request, response);
        String actualResult = response.getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream(
                "rdfExport/exportForTestDeleteAllVocabularyData.rdf"), StandardCharsets.UTF_8);
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //there should be the following 5 differences
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));
    }

}
