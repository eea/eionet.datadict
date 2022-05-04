package eionet.meta.service;

import eionet.meta.ActionBeanUtils;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class RDFVocabularyImportServiceAllCasesTestIT extends VocabularyImportServiceTestBase {

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
     */
    @Test
    public void testDontPurgeIgnoreMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.keep);

        List<VocabularyConcept> concepts = getAllVocabularyConceptsWithAttributes(oldVocabularyFolder);
        assertThat(concepts, is(notNullValue()));
        assertThat(concepts.size(), is(5));

        //Concept 1
        VocabularyConcept concept1 = concepts.get(0);
        assertThat(concept1.getId(), is(8));
        assertThat(concept1.getIdentifier(), is("rdf_test_concept_1"));
        List<List<DataElement>> elementAttributesConcept1 = concept1.getElementAttributes();
        assertThat(elementAttributesConcept1.size(), is(2));
        List<DataElement> element1Concept1 = elementAttributesConcept1.get(0);
        assertThat(element1Concept1.size(), is(1));
        assertThat(element1Concept1.get(0).getId(), is(1));
        assertThat(element1Concept1.get(0).getAttributeValue(), is("testElement1 for rdf_test_concept_1"));
        List<DataElement> element2Concept1 = elementAttributesConcept1.get(1);
        assertThat(element2Concept1.size(), is(1));
        assertThat(element2Concept1.get(0).getId(), is(2));
        assertThat(element2Concept1.get(0).getAttributeValue(), is("testElement2 for rdf_test_concept_1"));

        //Concept 2
        VocabularyConcept concept2 = concepts.get(1);
        assertThat(concept2.getId(), is(9));
        assertThat(concept2.getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concept2.getDefinition(), is("rdf_test_concept_def_2_changed"));
        List<List<DataElement>> elementAttributesConcept2 = concept2.getElementAttributes();
        assertThat(elementAttributesConcept2.size(), is(3));
        List<DataElement> element1Concept2 = elementAttributesConcept2.get(0);
        assertThat(element1Concept2.size(), is(1));
        assertThat(element1Concept2.get(0).getId(), is(2));
        assertThat(element1Concept2.get(0).getAttributeValue(), is("testElement1 for rdf_test_concept_2"));
        assertThat(element1Concept2.get(1).getAttributeValue(), is("New testElement1 for rdf_test_concept_2"));
        List<DataElement> element2Concept2 = elementAttributesConcept2.get(1);
        assertThat(element2Concept2.size(), is(1));
        assertThat(element2Concept2.get(0).getId(), is(3));
        assertThat(element2Concept2.get(0).getAttributeValue(), is("testElement3 for rdf_test_concept_2"));
        List<DataElement> element3Concept2 = elementAttributesConcept2.get(2);
        assertThat(element3Concept2.size(), is(1));
        assertThat(element3Concept2.get(0).getId(), is(2));
        assertThat(element3Concept2.get(0).getAttributeValue(), is("testElement2 for rdf_test_concept_2"));

        //Concept 3
        VocabularyConcept concept3 = concepts.get(2);
        assertThat(concept3.getId(), is(10));
        assertThat(concept3.getIdentifier(), is("rdf_test_concept_3"));
        List<List<DataElement>> elementAttributesConcept3 = concept3.getElementAttributes();
        assertThat(elementAttributesConcept3.size(), is(0));

        //Concept 4
        VocabularyConcept concept4 = concepts.get(3);
        assertThat(concept4.getId(), is(11));
        assertThat(concept4.getIdentifier(), is("rdf_test_concept_4"));
        List<List<DataElement>> elementAttributesConcept4 = concept4.getElementAttributes();
        assertThat(elementAttributesConcept4.size(), is(0));

        //Concept 5
        VocabularyConcept concept5 = concepts.get(4);
        assertThat(concept5.getId(), is(12));
        assertThat(concept5.getIdentifier(), is("rdf_test_concept_5"));
        List<List<DataElement>> elementAttributesConcept5 = concept5.getElementAttributes();
        assertThat(elementAttributesConcept5.size(), is(1));
        List<DataElement> element1Concept5 = elementAttributesConcept5.get(0);
        assertThat(element1Concept5.size(), is(1));
        assertThat(element1Concept5.get(0).getId(), is(1));
        assertThat(element1Concept5.get(0).getAttributeValue(), is("testElement1 for rdf_test_concept_5"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf they removed.
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testDontPurgeRemoveMissingConcepts() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.remove);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(3));
        assertThat(concepts.get(0).getNotation(), is("9"));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(0).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(1).getNotation(), is("11"));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(2).getNotation(), is("12"));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_5"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to invalid
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToInvalid() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.invalid);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(5));
        assertThat(concepts.get(0).getId(), is(8));
        assertThat(concepts.get(0).getNotation(), is(nullValue()));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_1"));
        assertThat(concepts.get(0).getStatus(), is(StandardGenericStatus.INVALID));
        assertThat(concepts.get(1).getNotation(), is("9"));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(1).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(2).getId(), is(10));
        assertThat(concepts.get(2).getNotation(), is(nullValue()));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_3"));
        assertThat(concepts.get(2).getStatus(), is(StandardGenericStatus.INVALID));
        assertThat(concepts.get(3).getNotation(), is("11"));
        assertThat(concepts.get(3).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(4).getNotation(), is("12"));
        assertThat(concepts.get(4).getIdentifier(), is("rdf_test_concept_5"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecated() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.deprecated);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(5));
        assertThat(concepts.get(0).getId(), is(8));
        assertThat(concepts.get(0).getNotation(), is(nullValue()));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_1"));
        assertThat(concepts.get(0).getStatus(), is(StandardGenericStatus.DEPRECATED));
        assertThat(concepts.get(1).getNotation(), is("9"));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(1).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(2).getId(), is(10));
        assertThat(concepts.get(2).getNotation(), is(nullValue()));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_3"));
        assertThat(concepts.get(2).getStatus(), is(StandardGenericStatus.DEPRECATED));
        assertThat(concepts.get(3).getNotation(), is("11"));
        assertThat(concepts.get(3).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(4).getNotation(), is("12"));
        assertThat(concepts.get(4).getIdentifier(), is("rdf_test_concept_5"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Retired
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedRetired() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.retired);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(5));
        assertThat(concepts.get(0).getId(), is(8));
        assertThat(concepts.get(0).getNotation(), is(nullValue()));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_1"));
        assertThat(concepts.get(0).getStatus(), is(StandardGenericStatus.DEPRECATED_RETIRED));
        assertThat(concepts.get(1).getNotation(), is("9"));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(1).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(2).getId(), is(10));
        assertThat(concepts.get(2).getNotation(), is(nullValue()));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_3"));
        assertThat(concepts.get(2).getStatus(), is(StandardGenericStatus.DEPRECATED_RETIRED));
        assertThat(concepts.get(3).getNotation(), is("11"));
        assertThat(concepts.get(3).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(4).getNotation(), is("12"));
        assertThat(concepts.get(4).getIdentifier(), is("rdf_test_concept_5"));
    }

    /*
        In this case, existing vocabulary information will be updated with information from imported concepts.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Superseded
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testDontPurgeMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.superseded);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(5));
        assertThat(concepts.get(0).getId(), is(8));
        assertThat(concepts.get(0).getNotation(), is(nullValue()));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_1"));
        assertThat(concepts.get(0).getStatus(), is(StandardGenericStatus.DEPRECATED_SUPERSEDED));
        assertThat(concepts.get(1).getNotation(), is("9"));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(1).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(2).getId(), is(10));
        assertThat(concepts.get(2).getNotation(), is(nullValue()));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_3"));
        assertThat(concepts.get(2).getStatus(), is(StandardGenericStatus.DEPRECATED_SUPERSEDED));
        assertThat(concepts.get(3).getNotation(), is("11"));
        assertThat(concepts.get(3).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(4).getNotation(), is("12"));
        assertThat(concepts.get(4).getIdentifier(), is("rdf_test_concept_5"));
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf they are ignored
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testPurgePerPredicateIgnoreMissingConcepts() throws Exception {
        /*VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.keep);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(5));
        assertThat(concepts.get(0).getId(), is(8));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_1"));
        assertThat(concepts.get(1).getId(), is(9));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(1).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(2).getId(), is(10));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_3"));
        assertThat(concepts.get(3).getId(), is(11));
        assertThat(concepts.get(3).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(4).getId(), is(12));
        assertThat(concepts.get(4).getIdentifier(), is("rdf_test_concept_5"));*/
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf they removed
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testPurgePerPredicateRemoveMissingConcepts(){
        //IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.remove
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf are maintained but their status is updated to invalid
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToInvalid(){
        //IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.invalid
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        /If there are missing concepts in the rdf are maintained but their status is updated to Deprecated
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecated(){

        //IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.deprecated
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Retired
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecatedRetired(){
        //IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.retired
    }

    /*
        In this case, predicates of existing concepts will be replaced with the imported predicates.
        If there are missing concepts in the rdf are maintained but their status is updated to Deprecated-Superseded
        If there are extra concepts in the rdf, they are added.
     */
    @Test
    public void testPurgePerPredicateMaintainMissingConceptsAndChangeStatusToDeprecatedSuperseded(){
        //IVocabularyImportService.UploadActionBefore.keep, IVocabularyImportService.UploadAction.add_and_purge_per_predicate_basis, IVocabularyImportService.MissingConceptsAction.superseded
    }

    /*
        In this case, all existing concepts will be removed and the imported concepts will be added.
     */
    @Test
    public void testPurgeAllVocabularyData() throws Exception {
        VocabularyFolder oldVocabularyFolder = vocabularyService.getVocabularyFolder(4);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_rdf_header_vocab.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, oldVocabularyFolder, IVocabularyImportService.UploadActionBefore.remove, IVocabularyImportService.UploadAction.add, IVocabularyImportService.MissingConceptsAction.keep);

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(3));
        assertThat(concepts.get(0).getNotation(), is("9"));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_2"));
        assertThat(concepts.get(0).getDefinition(), is("rdf_test_concept_def_2_changed"));
        assertThat(concepts.get(1).getNotation(), is("11"));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_4"));
        assertThat(concepts.get(2).getNotation(), is("12"));
        assertThat(concepts.get(2).getIdentifier(), is("rdf_test_concept_5"));

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

        VocabularyFolder newVocabularyFolder = vocabularyService.getVocabularyWithConcepts("rdf_header_vocab", "rdf_header_vs");
        assertThat(newVocabularyFolder, is(notNullValue()));
        assertThat(newVocabularyFolder.getId(), is(4));
        List<VocabularyConcept> concepts = newVocabularyFolder.getConcepts();
        assertThat(concepts.size(), is(2));
        assertThat(concepts.get(0).getId(), is(8));
        assertThat(concepts.get(0).getIdentifier(), is("rdf_test_concept_1"));
        assertThat(concepts.get(1).getId(), is(10));
        assertThat(concepts.get(1).getIdentifier(), is("rdf_test_concept_3"));
    }

}
