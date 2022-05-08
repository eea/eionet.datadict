package eionet.meta.service;

import eionet.datadict.util.StringUtils;
import eionet.meta.ActionBeanUtils;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.web.action.VocabularyFolderActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RDFVocabularyImportServiceAllCasesRdfExportTestIT extends VocabularyImportServiceTestBase{

    /**
     * Vocabulary folder RDF import service.
     */
    @SpringBeanByType
    private IRDFVocabularyImportService vocabularyImportService;

    @BeforeClass
    public static void loadData() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData("seed-emptydb.xml");
        DBUnitHelper.loadData("rdf_import/seed-vocabularyrdf-import-without-working-copy.xml");
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
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "rdf_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "rdf_header_vocab");
        trip.execute("rdf");
        String actualResult = trip.getOutputString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeIgnoreMissingConcepts.rdf"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //we should only have 1 difference for the date modified attribute
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));

    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf they removed.
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    //@Test
    public void testDontPurgeRemoveMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.remove);

        //export rdf file and compare it to rdf file from rdfExport folder
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "rdf_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "rdf_header_vocab");
        trip.execute("rdf");
        String actualResult = trip.getOutputString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeRemoveMissingConcepts.rdf"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //we should only have 1 difference for the date modified attribute
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to invalid
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    //@Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToInvalid() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.invalid);

        //export rdf file and compare it to rdf file from rdfExport folder
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "rdf_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "rdf_header_vocab");
        trip.execute("rdf");
        String actualResult = trip.getOutputString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToInvalid.rdf"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //we should only have 1 difference for the date modified attribute
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    //@Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecated() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.deprecated);

        //export rdf file and compare it to rdf file from rdfExport folder
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "rdf_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "rdf_header_vocab");
        trip.execute("rdf");
        String actualResult = trip.getOutputString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecated.rdf"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //we should only have 1 difference for the date modified attribute
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Retired
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    //@Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedRetired() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.retired);

        //export rdf file and compare it to rdf file from rdfExport folder
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "rdf_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "rdf_header_vocab");
        trip.execute("rdf");
        String actualResult = trip.getOutputString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedRetired.rdf"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //we should only have 1 difference for the date modified attribute
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Superseded
        If there are extra concepts in the rdf, they are added.
        If there is an existing data element for a non missing concept in the rdf, the concept will have both the old value for the element, as well as the new one.
     */
    //@Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.superseded);

        //export rdf file and compare it to rdf file from rdfExport folder
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.folderName", "rdf_header_vs");
        trip.addParameter("vocabularyFolder.identifier", "rdf_header_vocab");
        trip.execute("rdf");
        String actualResult = trip.getOutputString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedRDFResultString = IOUtils.toString(classLoader.getResourceAsStream("rdfExport/exportForTestDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded.rdf"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedRDFResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualResult));
        assertFalse(diff.similar());
        DetailedDiff detDiff = new DetailedDiff(diff);
        //we should only have 1 difference for the date modified attribute
        List differences = detDiff.getAllDifferences();
        assertThat(differences.size(), is(1));
        assertTrue(differences.get(0).toString().contains("dateModified"));
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


    }

}
