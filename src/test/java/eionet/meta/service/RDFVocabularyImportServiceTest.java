/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */

package eionet.meta.service;

import eionet.meta.ActionBeanUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyImportBaseHandler;
import eionet.meta.service.IVocabularyImportService.MissingConceptsAction;
import eionet.meta.service.IVocabularyImportService.UploadAction;
import eionet.meta.service.IVocabularyImportService.UploadActionBefore;

/**
 * JUnit integration test with Unitils for RDF Vocabulary Import Service.
 *
 * @author enver
 */
public class RDFVocabularyImportServiceTest extends VocabularyImportServiceTestBase {
    /**
     * Vocabulary folder RDF import service.
     */
    @SpringBeanByType
    private IRDFVocabularyImportService vocabularyImportService;

    @BeforeClass
    public static void loadData() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData("seed-emptydb.xml");
        DBUnitHelper.loadData("rdf_import/seed-vocabularyrdf-import.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("rdf_import/seed-vocabularyrdf-import.xml");
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

    /**
     * In this test, three concepts RDF is imported. Concept 1 includes updated values and DataElements (no insertion, only update).
     * Concept 2 and 3 does not include updated values.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_1.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("rdf_test_concept_label_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        element = new DataElement();
        element.setAttributeValue("et_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("et");
        elems.add(element);

        VocabularyConcept vc9 = findVocabularyConceptById(concepts, 9);
        dataElements = vc9.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);
        for (DataElement elem : elems) {
            if (vc8.getId() == elem.getRelatedConceptId()) {
                elem.setRelatedConceptLabel(vc8.getLabel());
            }
        }

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptAndElementsUpdated

    /**
     * In this test, three concepts RDF is imported with missing concepts invalid option. Concept 1 includes updated values and DataElements (no insertion, only update).
     * Concept 2 and 3 does not include updated values.
     * Since it's a 3 concepts vocabulary, elements shouldn't be changed.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsInvalid() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_1.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.invalid);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("rdf_test_concept_label_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        element = new DataElement();
        element.setAttributeValue("et_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("et");
        elems.add(element);

        VocabularyConcept vc9 = findVocabularyConceptById(concepts, 9);
        dataElements = vc9.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);
        for (DataElement elem : elems) {
            if (vc8.getId() == elem.getRelatedConceptId()) {
                elem.setRelatedConceptLabel(vc8.getLabel());
            }
        }

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getAllVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsInvalid

    /**
     * In this test, three concepts RDF is imported with missing concepts remove option. Concept 1 includes updated values and DataElements (no insertion, only update).
     * Concept 2 and 3 does not include updated values.
     * Since it's a 3 concepts vocabulary, elements shouldn't be changed.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsRemove() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_1.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.remove);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("rdf_test_concept_label_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        element = new DataElement();
        element.setAttributeValue("et_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("et");
        elems.add(element);

        VocabularyConcept vc9 = findVocabularyConceptById(concepts, 9);
        dataElements = vc9.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);
        for (DataElement elem : elems) {
            if (vc8.getId() == elem.getRelatedConceptId()) {
                elem.setRelatedConceptLabel(vc8.getLabel());
            }
        }

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsRemove

    /**
     * In this test, 0 concepts RDF is imported with missing concepts remove option.
     * Since it's a 3 concepts in the vocabulary, all should be removed.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsRemoveAllConcepts() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.remove);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Number of concepts in the vocabulary after removing missing concepts", 0, updatedConcepts.size());
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsRemoveAllConcepts

    /**
     * In this test, 0 concepts RDF is imported with missing concepts invalid option.
     * Since it's a 3 concepts in the vocabulary, all should be set to invalid.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsInvalidAllConcepts() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.invalid);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getAllVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Number of concepts in the vocabulary", concepts.size(), updatedConcepts.size());

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String todayFormatted = dateFormatter.format(today);
        for (VocabularyConcept vc : updatedConcepts) {
            Assert.assertEquals("Concept Status", StandardGenericStatus.INVALID, vc.getStatus());
            Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(vc.getStatusModified()));
            Assert.assertEquals("Not Accepted Date", todayFormatted, dateFormatter.format(vc.getNotAcceptedDate()));
        }
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsInvalidAllConcepts

    /**
     * In this test, 0 concepts RDF is imported with missing concepts deprecated option.
     * Since it's a 3 concepts in the vocabulary, all should be set to deprecated.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsDeprecatedAllConcepts() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.deprecated);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Number of concepts in the vocabulary", concepts.size(), updatedConcepts.size());

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String todayFormatted = dateFormatter.format(today);
        for (VocabularyConcept vc : updatedConcepts) {
            Assert.assertEquals("Concept Status", StandardGenericStatus.DEPRECATED, vc.getStatus());
            Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(vc.getStatusModified()));
        }
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsDeprecatedAllConcepts

    /**
     * In this test, 0 concepts RDF is imported with missing concepts retired option.
     * Since it's a 3 concepts in the vocabulary, all should be set to retired.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsRetiredAllConcepts() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.retired);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Number of concepts in the vocabulary", concepts.size(), updatedConcepts.size());

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String todayFormatted = dateFormatter.format(today);
        for (VocabularyConcept vc : updatedConcepts) {
            Assert.assertEquals("Concept Status", StandardGenericStatus.DEPRECATED_RETIRED, vc.getStatus());
            Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(vc.getStatusModified()));
        }
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsRetiredAllConcepts

    /**
     * In this test, 0 concepts RDF is imported with missing concepts superseded option.
     * Since it's a 3 concepts in the vocabulary, all should be set to superseded.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsSupersededAllConcepts() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.superseded);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Number of concepts in the vocabulary", concepts.size(), updatedConcepts.size());

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String todayFormatted = dateFormatter.format(today);
        for (VocabularyConcept vc : updatedConcepts) {
            Assert.assertEquals("Concept Status", StandardGenericStatus.DEPRECATED_SUPERSEDED, vc.getStatus());
            Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(vc.getStatusModified()));
        }
    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsSupersededAllConcepts

    /**
     * In this test, 0 concepts RDF is imported with missing concepts keep option.
     * Since it's a 3 concepts in the vocabulary, all should be same as before
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedWithMissingConceptsKeepAllConcepts() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.keep);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Number of concepts in the vocabulary", concepts.size(), updatedConcepts.size());

        // compare before and after objects
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);

    } // end of test step testIfConceptAndElementsUpdatedWithMissingConceptsKeepAllConcepts

    /**
     * In this test, two concepts RDF is imported. Concept 1 includes updated values (no insertion, only update) Concept 2 includes
     * updated values too (no insertion, only update)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_2.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setDefinition("rdf_test_concept_def_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg2_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        element = new DataElement();
        element.setAttributeValue("en_rdf_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("en");
        elems.add(element);

        // skos:related will be created as well, see #18140
        dataElemId = 7;
        elems = new ArrayList<DataElement>();
        dataElements.add(elems);

        element = new DataElement();
        element.setIdentifier("skos:related");
        element.setId(dataElemId);
        element.setRelatedConceptLabel("rdf_test_concept_label_3_updated");
        element.setRelatedConceptDefinition("rdf_test_concept_def_3_updated");
        element.setRelatedConceptId(10);
        element.setRelatedConceptIdentifier("rdf_test_concept_3");
        element.setRelatedConceptVocSet("rdf_header_vs");
        element.setRelatedConceptVocSetLabel("rdf_header_vs_test");
        element.setRelatedConceptBaseURI("http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab/");
        element.setRelatedConceptVocabulary("rdf_header_vocab");
        element.setRelatedConceptVocabularyLabel("rdf_header_vocab_test");
        element.setAttributeValue(null);

        element.setElemAttributeValues(getDatatypeElemAttrs("localref"));
        elems.add(element);

        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setLabel("rdf_test_concept_label_3_updated");
        vc10.setDefinition("rdf_test_concept_def_3_updated");

        dataElemId = 8;
        dataElements = vc10.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        element = new DataElement();
        element.setAttributeValue("bg_rdf_test_concept_3_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        dataElemId = 9;
        identifier = "skos:definition";
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        element = new DataElement();
        element.setAttributeValue("pl_rdf_test_concept_3_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("pl");
        elems.add(element);

        dataElemId = 7;
        identifier = "skos:related";
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);

        element = new DataElement();
        element.setId(dataElemId);
        element.setIdentifier(identifier);
        element.setRelatedConceptLabel("rdf_test_concept_label_1_lets_get_sure");
        element.setRelatedConceptDefinition("rdf_test_concept_def_1_updated");
        element.setRelatedConceptId(8);
        element.setRelatedConceptIdentifier("rdf_test_concept_1");
        element.setRelatedConceptVocSet("rdf_header_vs");
        element.setRelatedConceptVocSetLabel("rdf_header_vs_test");
        element.setRelatedConceptBaseURI("http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab/");
        element.setRelatedConceptVocabulary("rdf_header_vocab");
        element.setRelatedConceptVocabularyLabel("rdf_header_vocab_test");
        element.setAttributeValue(null);
        element.setElemAttributeValues(getDatatypeElemAttrs("localref"));
        elems.add(element);

        VocabularyConcept vc9 = findVocabularyConceptById(concepts, 9);
        dataElements = vc9.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", dataElements);
        for (DataElement elem : elems) {
            if (vc10.getId() == elem.getRelatedConceptId()) {
                elem.setRelatedConceptLabel(vc10.getLabel());
                elem.setRelatedConceptDefinition(vc10.getDefinition());
            }
        }
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);
        for (DataElement elem : elems) {
            if (vc8.getId() == elem.getRelatedConceptId()) {
                elem.setRelatedConceptLabel(vc8.getLabel());
                elem.setRelatedConceptDefinition(vc8.getDefinition());
            }
        }
        
        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAndElementsUpdated

    /**
     * In this test, single concept RDF is imported. concept is a non existing concept to be imported with data elements
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAdded() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_3.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("rdf_test_concept_4");
        vc11.setLabel("rdf_test_concept_label_4");
        vc11.setDefinition("rdf_test_concept_def_4");
        vc11.setStatus(StandardGenericStatus.VALID);
        vc11.setStatusModified(new Date(System.currentTimeMillis()));
        vc11.setAcceptedDate(new Date(System.currentTimeMillis()));

        // create element attributes (there is only one concept)
        List<List<DataElement>> elementAttributes = new ArrayList<List<DataElement>>();
        DataElement elem = null;
        String identifier = null;
        int dataElemId = -1;

        // skos:prefLabel
        identifier = "skos:prefLabel";
        dataElemId = 8;
        List<DataElement> elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg_rdf_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg2_rdf_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elementAttributes.add(elements);

        // skos:definition
        identifier = "skos:definition";
        dataElemId = 9;
        elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("de_rdf_test_concept_4");
        elem.setAttributeLanguage("de");
        elements.add(elem);
        elementAttributes.add(elements);

        vc11.setElementAttributes(elementAttributes);
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 4 vocabulary concepts", updatedConcepts.size(), 4);

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(3).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAdded

    /**
     * In this test, single concept RDF is imported. concept is a non existing concept to be imported with data elements.
     * Missing concepts should be removed.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedAndMissingConceptsRemoved() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_3.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.remove);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("rdf_test_concept_4");
        vc11.setLabel("rdf_test_concept_label_4");
        vc11.setDefinition("rdf_test_concept_def_4");
        vc11.setStatus(StandardGenericStatus.VALID);
        vc11.setStatusModified(new Date(System.currentTimeMillis()));
        vc11.setAcceptedDate(new Date(System.currentTimeMillis()));

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 1 vocabulary concepts", updatedConcepts.size(), 1);

        // check for other fields
        VocabularyConcept vc = updatedConcepts.get(0);
        Assert.assertEquals("New concept identifier", vc11.getIdentifier(), vc.getIdentifier());
        Assert.assertEquals("New concept label", vc11.getLabel(), vc.getLabel());
        Assert.assertEquals("New concept definition", vc11.getDefinition(), vc.getDefinition());

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String todayFormatted = dateFormatter.format(today);
        Assert.assertEquals("Concept Status", StandardGenericStatus.VALID, vc.getStatus());
        Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(vc.getStatusModified()));
        Assert.assertEquals("Accepted Date", todayFormatted, dateFormatter.format(vc.getAcceptedDate()));
    }// end of test step testIfNewConceptAddedAndMissingConceptsRemoved

    /**
     * In this test, single concept RDF is imported. concept is a non existing concept to be imported with data elements.
     * Missing concepts should be deprecated.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedAndMissingConceptsDeprecated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_3.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.deprecated);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("rdf_test_concept_4");
        vc11.setLabel("rdf_test_concept_label_4");
        vc11.setDefinition("rdf_test_concept_def_4");
        vc11.setStatus(StandardGenericStatus.VALID);
        vc11.setStatusModified(new Date(System.currentTimeMillis()));
        vc11.setAcceptedDate(new Date(System.currentTimeMillis()));

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 4 vocabulary concepts", updatedConcepts.size(), 4);

        // check for new concept other fields
        VocabularyConcept vc = findVocabularyConceptByIdentifier(updatedConcepts, vc11.getIdentifier());
        Assert.assertEquals("New concept identifier", vc11.getIdentifier(), vc.getIdentifier());
        Assert.assertEquals("New concept label", vc11.getLabel(), vc.getLabel());
        Assert.assertEquals("New concept definition", vc11.getDefinition(), vc.getDefinition());

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String todayFormatted = dateFormatter.format(today);
        Assert.assertEquals("Concept Status", StandardGenericStatus.VALID, vc.getStatus());
        Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(vc.getStatusModified()));
        Assert.assertEquals("Accepted Date", todayFormatted, dateFormatter.format(vc.getAcceptedDate()));

        // remove new object and check other objects
        updatedConcepts.remove(vc);
        for (VocabularyConcept deprecatedConcept : updatedConcepts) {
            Assert.assertEquals("Concept Status", StandardGenericStatus.DEPRECATED, deprecatedConcept.getStatus());
            Assert.assertEquals("Status Modified", todayFormatted, dateFormatter.format(deprecatedConcept.getStatusModified()));
        }
    }// end of test step testIfNewConceptAddedAndMissingConceptsRemoved

    /**
     * In this test, single concept RDF is imported. concept is a non existing concept to be imported with data elements after purge
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_3.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("rdf_test_concept_4");
        vc11.setLabel("rdf_test_concept_label_4");
        vc11.setDefinition("rdf_test_concept_def_4");
        vc11.setStatus(StandardGenericStatus.VALID);
        vc11.setStatusModified(new Date(System.currentTimeMillis()));
        vc11.setAcceptedDate(new Date(System.currentTimeMillis()));

        // create element attributes (there is only one concept)
        List<List<DataElement>> elementAttributes = new ArrayList<List<DataElement>>();
        DataElement elem = null;
        String identifier = null;
        int dataElemId = -1;

        // skos:prefLabel
        identifier = "skos:prefLabel";
        dataElemId = 8;
        List<DataElement> elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg_rdf_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg2_rdf_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elementAttributes.add(elements);

        // skos:definition
        identifier = "skos:definition";
        dataElemId = 9;
        elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("de_rdf_test_concept_4");
        elem.setAttributeLanguage("de");
        elements.add(elem);
        elementAttributes.add(elements);

        vc11.setElementAttributes(elementAttributes);
        concepts = new ArrayList<VocabularyConcept>();
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 1 vocabulary concept", updatedConcepts.size(), 1);

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(0).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAddedAfterPurge

    /**
     * In this test, two concepts RDF is imported. Concepts have updated values.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdatedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_2.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setStatus(StandardGenericStatus.VALID);
        vc8.setAcceptedDate(new Date(System.currentTimeMillis()));
        vc8.setStatusModified(new Date(System.currentTimeMillis()));
        vc8.setDefinition("rdf_test_concept_def_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element = findDataElemByAttrValue(elems, "bg2_rdf_test_concept_1");
        element.setAttributeValue("bg2_rdf_test_concept_1_updated");

        elems = VocabularyImportBaseHandler.getDataElementValuesByNameAndLang("skos:prefLabel", "en", dataElements);
        element = findDataElemByAttrValue(elems, "en_rdf_test_concept_1");
        element.setAttributeValue("en_rdf_test_concept_1_updated");

        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", dataElements);
        element = elems.get(0);
        element.setRelatedConceptLabel(null);
        element.setRelatedConceptId(null);
        element.setRelatedConceptIdentifier(null);
        element.setRelatedConceptDefinition(null);
        element.setRelatedConceptVocSet(null);
        element.setRelatedConceptVocSetLabel(null);
        element.setRelatedConceptBaseURI(null);
        element.setRelatedConceptVocabulary(null);
        element.setRelatedConceptVocabularyLabel(null);
        element.setAttributeValue(VocabularyFolder.getBaseUri(vocabularyFolder) + "rdf_test_concept_2");

        // the rdf contains a triple [concept1 skos:related concetp3] but no vice versa.
        // Both relations will be created if RDF has such error?
        elems = new ArrayList<DataElement>();
        dataElements.add(elems);

        Map<String, List<String>> elemAttrValues = new HashMap<String, List<String>>();
        List<String> aValues = new ArrayList<String>();
        aValues.add("localref");
        elemAttrValues.put("Datatype", aValues);

        element = new DataElement();
        element.setIdentifier("skos:related");
        element.setId(7);
        element.setRelatedConceptLabel("rdf_test_concept_label_3_updated");
        element.setRelatedConceptDefinition("rdf_test_concept_def_3_updated");
        element.setRelatedConceptId(null);
        element.setRelatedConceptIdentifier("rdf_test_concept_3");
        element.setRelatedConceptVocSet("rdf_header_vs");
        element.setRelatedConceptVocSetLabel("rdf_header_vs_test");
        element.setRelatedConceptBaseURI("http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab/");
        element.setRelatedConceptVocabulary("rdf_header_vocab");
        element.setRelatedConceptVocabularyLabel("rdf_header_vocab_test");
        element.setAttributeValue(null);
        element.setElemAttributeValues(elemAttrValues);
        elems.add(element);

        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setLabel("rdf_test_concept_label_3_updated");
        vc10.setDefinition("rdf_test_concept_def_3_updated");
        vc10.setStatus(StandardGenericStatus.VALID);
        vc10.setAcceptedDate(new Date(System.currentTimeMillis()));
        vc10.setStatusModified(new Date(System.currentTimeMillis()));

        dataElements = vc10.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        element = findDataElemByAttrValue(elems, "bg_rdf_test_concept_3");
        element.setAttributeValue("bg_rdf_test_concept_3_updated");

        elems = VocabularyImportBaseHandler.getDataElementValuesByNameAndLang("skos:definition", "pl", dataElements);
        element = findDataElemByAttrValue(elems, "pl_rdf_test_concept_3");
        element.setAttributeValue("pl_rdf_test_concept_3_updated");

        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", dataElements);
        element = elems.get(0);
        element.setRelatedConceptLabel(null);
        element.setRelatedConceptId(null);
        element.setRelatedConceptIdentifier(null);
        element.setRelatedConceptDefinition(null);
        element.setRelatedConceptVocSet(null);
        element.setRelatedConceptVocSetLabel(null);
        element.setRelatedConceptBaseURI(null);
        element.setRelatedConceptVocabulary(null);
        element.setRelatedConceptVocabularyLabel(null);
        element.setAttributeValue(VocabularyFolder.getBaseUri(vocabularyFolder) + "rdf_test_concept_2");

        element = new DataElement();
        element.setIdentifier("skos:related");

        element.setId(7);

        element.setElemAttributeValues(elemAttrValues);

        element.setElemAttributeValues(elemAttrValues);
        element.setRelatedConceptLabel("rdf_test_concept_label_1_lets_get_sure");
        element.setRelatedConceptDefinition("rdf_test_concept_def_1_updated");
        element.setRelatedConceptId(null);
        element.setRelatedConceptIdentifier("rdf_test_concept_1");
        element.setRelatedConceptVocSet("rdf_header_vs");
        element.setRelatedConceptVocSetLabel("rdf_header_vs_test");
        element.setRelatedConceptBaseURI("http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab/");
        element.setRelatedConceptVocabulary("rdf_header_vocab");
        element.setRelatedConceptVocabularyLabel("rdf_header_vocab_test");
        element.setAttributeValue(null);
        elems.add(element);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", 2, updatedConcepts.size());

        VocabularyConcept vc8Updated = findVocabularyConceptByIdentifier(updatedConcepts, vc8.getIdentifier());
        VocabularyConcept vc10Updated = findVocabularyConceptByIdentifier(updatedConcepts, vc10.getIdentifier());

        vc8.setId(vc8Updated.getId());
        dataElements = vc8.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", dataElements);
        elems.get(0).setRelatedConceptId(vc10Updated.getId());

        dataElements = vc10.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", dataElements);
        elems.get(1).setRelatedConceptId(vc8Updated.getId());
        vc10.setId(vc10Updated.getId());
        concepts = new ArrayList<VocabularyConcept>();
        concepts.add(vc8);
        concepts.add(vc10);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAndElementsUpdatedAfterPurge

    /**
     * In this test, two concepts RDF is imported. Concepts are derived from base RDF. Just identifiers are updated. Purge operation
     * is tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_4.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        concepts.remove(2);// remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("rdf_test_concept_1_after_purge");
        concepts.get(0).setStatus(StandardGenericStatus.VALID);
        concepts.get(0).setAcceptedDate(new Date(System.currentTimeMillis()));
        concepts.get(0).setStatusModified(new Date(System.currentTimeMillis()));
        concepts.get(1).setIdentifier("rdf_test_concept_2_after_purge");
        concepts.get(1).setStatus(StandardGenericStatus.VALID);
        concepts.get(1).setAcceptedDate(new Date(System.currentTimeMillis()));
        concepts.get(1).setStatusModified(new Date(System.currentTimeMillis()));

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        // concepts expected to be inserted in the same order as they are in rdf file, get ids from updated beans
        concepts.get(0).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(0).getIdentifier()).getId());
        concepts.get(1).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(1).getIdentifier()).getId());

        // update related concepts
        List<DataElement> elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", concepts.get(0).getElementAttributes());
        DataElement element = elems.get(0);
        element.setRelatedConceptId(concepts.get(1).getId());
        element.setRelatedConceptIdentifier(concepts.get(1).getIdentifier());

        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", concepts.get(1).getElementAttributes());
        element = elems.get(0);
        element.setRelatedConceptId(concepts.get(0).getId());
        element.setRelatedConceptIdentifier(concepts.get(0).getIdentifier());

        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", concepts.get(1).getElementAttributes());
        element = elems.get(0);
        element.setRelatedConceptLabel(null);
        element.setRelatedConceptId(null);
        element.setRelatedConceptIdentifier(null);
        element.setRelatedConceptDefinition(null);
        element.setRelatedConceptVocSet(null);
        element.setRelatedConceptVocSetLabel(null);
        element.setRelatedConceptBaseURI(null);
        element.setRelatedConceptVocabulary(null);
        element.setRelatedConceptVocabularyLabel(null);
        element.setAttributeValue(VocabularyFolder.getBaseUri(vocabularyFolder) + "rdf_test_concept_3");

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsAddedAfterPurge

    /**
     * In this test, two concepts RDF is imported. Concepts are derived from base RDF. Just identifiers are updated.
     * It's called with missing concepts remove option. Third concept should be removed.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsUpdatedAndMissingConceptsRemoved() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_4.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.remove);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        concepts.remove(2);// remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("rdf_test_concept_1_after_purge");
        concepts.get(0).setStatus(StandardGenericStatus.VALID);
        concepts.get(0).setAcceptedDate(new Date(System.currentTimeMillis()));
        concepts.get(0).setStatusModified(new Date(System.currentTimeMillis()));
        concepts.get(1).setIdentifier("rdf_test_concept_2_after_purge");
        concepts.get(1).setStatus(StandardGenericStatus.VALID);
        concepts.get(1).setAcceptedDate(new Date(System.currentTimeMillis()));
        concepts.get(1).setStatusModified(new Date(System.currentTimeMillis()));

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        // concepts expected to be inserted in the same order as they are in rdf file, get ids from updated beans
        concepts.get(0).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(0).getIdentifier()).getId());
        concepts.get(1).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(1).getIdentifier()).getId());

        // update related concepts
        List<DataElement> elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", concepts.get(0).getElementAttributes());
        DataElement element = elems.get(0);
        element.setRelatedConceptId(concepts.get(1).getId());
        element.setRelatedConceptIdentifier(concepts.get(1).getIdentifier());

        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", concepts.get(1).getElementAttributes());
        element = elems.get(0);
        element.setRelatedConceptId(concepts.get(0).getId());
        element.setRelatedConceptIdentifier(concepts.get(0).getIdentifier());

        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", concepts.get(1).getElementAttributes());
        concepts.get(1).getElementAttributes().remove(elems); //remove it since it is deleted from database

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsUpdatedAndMissingConceptsRemoved

    /**
     * In this test, two concepts RDF is imported. Concepts are derived from base RDF. Just labels are updated.
     * It's called with missing concepts invalid option. Third concept should be invalid.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsUpdatedAndMissingConceptsInvalid() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_15.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.invalid);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // there is not much object just update, no need to iterate
        concepts.get(0).setLabel("rdf_test_concept_label_1_updated");
        List<DataElement> elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", concepts.get(0).getElementAttributes());
        elems.get(0).setRelatedConceptLabel("rdf_test_concept_label_2_updated");

        concepts.get(1).setLabel("rdf_test_concept_label_2_updated");
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", concepts.get(1).getElementAttributes());
        elems.get(0).setRelatedConceptLabel("rdf_test_concept_label_1_updated");

        concepts.get(2).setStatus(StandardGenericStatus.INVALID);
        concepts.get(2).setNotAcceptedDate(new Date(System.currentTimeMillis()));
        concepts.get(2).setStatusModified(new Date(System.currentTimeMillis()));
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", concepts.get(2).getElementAttributes());
        elems.get(0).setRelatedConceptLabel("rdf_test_concept_label_2_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getAllVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 3 vocabulary concepts", 3, updatedConcepts.size());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsUpdatedAndMissingConceptsInvalid

    /**
     * In this test, two concepts RDF is imported. Concepts are derived from base RDF. Just labels are updated.
     * It's called with missing concepts retired option. Third concept should be retired.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsUpdatedAndMissingConceptsRetired() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_15.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, UploadActionBefore.keep, UploadAction.add, MissingConceptsAction.retired);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // there is not much object just update, no need to iterate
        concepts.get(0).setLabel("rdf_test_concept_label_1_updated");
        List<DataElement> elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", concepts.get(0).getElementAttributes());
        elems.get(0).setRelatedConceptLabel("rdf_test_concept_label_2_updated");

        concepts.get(1).setLabel("rdf_test_concept_label_2_updated");
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", concepts.get(1).getElementAttributes());
        elems.get(0).setRelatedConceptLabel("rdf_test_concept_label_1_updated");

        concepts.get(2).setStatus(StandardGenericStatus.DEPRECATED_RETIRED);
        concepts.get(2).setStatusModified(new Date(System.currentTimeMillis()));
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", concepts.get(2).getElementAttributes());
        elems.get(0).setRelatedConceptLabel("rdf_test_concept_label_2_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 3 vocabulary concepts", updatedConcepts.size(), 3);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsUpdatedAndMissingConceptsRetired

    /**
     * In this test, single concept RDF is imported. Purge per predicate basis is tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsUpdatedAddedAfterPerPredicatePurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_5.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, true);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 3 vocabulary concepts", updatedConcepts.size(), 3);

        // manually create values of new concept for comparison
        String[] seenPredicates = new String[] { "skos:relatedMatch", "skos:related", "skos:prefLabel" };
        // remove elements of these predicates from first and second concepts
        for (int i = 0; i < 2; i++) {
            for (String seenPredicate : seenPredicates) {
                List<List<DataElement>> elementAttributes = concepts.get(i).getElementAttributes();
                List<DataElement> elems = VocabularyImportBaseHandler.getDataElementValuesByName(seenPredicate, elementAttributes);
                if (elems != null) {
                    elementAttributes.remove(elems);
                }
            }
        }

        // build manually updated data elements values for concept (check rdf file for updated values)
        VocabularyConcept vc3 = concepts.get(2);
        vc3.setDefinition("rdf_test_concept_def_3_updated");
        List<DataElement> dataElementValuesByName = VocabularyImportBaseHandler.getDataElementValuesByName("skos:relatedMatch", vc3.getElementAttributes());
        DataElement elem = dataElementValuesByName.get(0);
        elem.setAttributeValue("http://test.tripledev.ee/datadict/vocabulary/test/test_another_source/another2");
        dataElementValuesByName = VocabularyImportBaseHandler.getDataElementValuesByName("skos:prefLabel", vc3.getElementAttributes());
        elem = dataElementValuesByName.get(0);
        elem.setAttributeValue("bg_rdf_test_concept_3_updated");
        int count = dataElementValuesByName.size();
        for (int i = 1; i < count; i++) {
            dataElementValuesByName.remove(1);
        }
        // skos:related is deleted from concept 2 so now it will be automatically be deleted from db
        // SEE: fixRelatedElements method in dao
        dataElementValuesByName = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", vc3.getElementAttributes());

        //Since we implemented fixing local ref, concept with identifier 2 should have skos:related back to concept with identifier 3
        VocabularyConcept vc2Updated = updatedConcepts.get(1);
        dataElementValuesByName = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", vc2Updated.getElementAttributes());
        VocabularyConcept vc2 = concepts.get(1);
        vc2.getElementAttributes().add(dataElementValuesByName);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsUpdatedAddedAfterPerPredicatePurge

    /**
     * In this test, RDF file contains some concepts which has / and importer should skip those lines.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAreSkipped() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_6.rdf");

        // import RDF into database
        List<String> logMessages = vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        // only vocabulary concept 3 should change
        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setDefinition("rdf_test_concept_def_3_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc10.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg_rdf_test_concept_3_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        dataElemId = 9;
        identifier = "skos:definition";
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        element = new DataElement();
        element.setAttributeValue("de_rdf_test_concept_3_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("de");
        elems.add(element);
        
        // Propagate concept 3 changes to relations
        VocabularyConcept vc9 = findVocabularyConceptById(concepts, 9);
        dataElements = vc9.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName("skos:related", dataElements);
        
        for (DataElement elem : elems) {
            if (elem.getRelatedConceptId() == vc10.getId()) {
                elem.setRelatedConceptDefinition(vc10.getDefinition());
            }
        }

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsAreSkipped

    /**
     * In this test, related concept is tested when concept is in another vocabulary.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsSetRelatedInOtherVocabularies() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_7.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        // only vocabulary concept 1 should change
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = new ArrayList<DataElement>();
        DataElement elem = new DataElement();
        elem.setId(6);
        elem.setIdentifier("skos:relatedMatch");
        elem.setAttributeValue("http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab_2/rdf_test_concept_556");
        elem.setElemAttributeValues(getDatatypeElemAttrs("reference"));
        elems.add(elem);

        elem = new DataElement();
        elem.setId(6);
        elem.setIdentifier("skos:relatedMatch");
        elem.setAttributeValue("http://test.tripledev.ee/datadict/vocabulary/test/test_another_source/another1");
        elem.setElemAttributeValues(getDatatypeElemAttrs("reference"));
        elems.add(elem);

        elem = new DataElement();
        elem.setId(6);
        elem.setIdentifier("skos:relatedMatch");
        elem.setRelatedConceptId(11);
        elem.setRelatedConceptIdentifier("rdf_test_concept_555");
        elem.setRelatedConceptLabel("rdf_test_concept_label_555");
        elem.setRelatedConceptDefinition("rdf_test_concept_def_555");
        elem.setRelatedConceptVocabulary("rdf_header_vocab_2");
        elem.setRelatedConceptVocabularyLabel("rdf_header_vocab_test_2");
        elem.setRelatedConceptVocSet("rdf_header_vs");
        elem.setRelatedConceptVocSetLabel("rdf_header_vs_test");
        elem.setRelatedConceptBaseURI("http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab_2/");
        elem.setElemAttributeValues(getDatatypeElemAttrs("reference"));
        elems.add(elem);

        dataElements.add(elems);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    } // end of test step testIfConceptsSetRelatedInOtherVocabularies

    /**
     * In this test, four concepts RDF is imported. Concepts are derived from base RDF. Labels are are updated. Labels with working
     * language tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedWithCorrectLabelsAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_9.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 3 vocabulary concepts", updatedConcepts.size(), 3);

        // manually compare updated objects values
        // Concept 1
        VocabularyConcept concept = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_1");
        concept.setStatus(StandardGenericStatus.VALID);
        concept.setAcceptedDate(new Date(System.currentTimeMillis()));
        concept.setStatusModified(new Date(System.currentTimeMillis()));
        Assert.assertEquals("Label does not match for concept.", "en_rdf_test_concept_1", concept.getLabel());
        Assert.assertEquals("skos:prefLabel should have 3 elements for concept.", 3,
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:prefLabel", concept.getElementAttributes()).size());

        // Concept 2
        concept = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_2");
        concept.setStatus(StandardGenericStatus.VALID);
        concept.setAcceptedDate(new Date(System.currentTimeMillis()));
        concept.setStatusModified(new Date(System.currentTimeMillis()));
        Assert.assertEquals("Label does not match for concept.", "rdf_test_concept_label_2", concept.getLabel());
        Assert.assertEquals("Definition does not match for concept.", "rdf_test_concept_def_2", concept.getDefinition());
        Assert.assertEquals("skos:prefLabel should have 2 elements for concept.", 2,
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:prefLabel", concept.getElementAttributes()).size());
        Assert.assertEquals("skos:definition should have 1 elements for concept.", 1,
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:definition", concept.getElementAttributes()).size());

        // Concept 3
        concept = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_3");
        concept.setStatus(StandardGenericStatus.VALID);
        concept.setAcceptedDate(new Date(System.currentTimeMillis()));
        concept.setStatusModified(new Date(System.currentTimeMillis()));
        Assert.assertEquals("Label does not match for concept.", "bg_rdf_test_concept_3", concept.getLabel());
        Assert.assertEquals("Definition does not match for concept.", "en_rdf_test_concept_3", concept.getDefinition());
        Assert.assertEquals("skos:prefLabel should have 2 elements for concept.", 2,
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:prefLabel", concept.getElementAttributes()).size());
        Assert.assertEquals("skos:definition should have 2 elements for concept.", 2,
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:definition", concept.getElementAttributes()).size());

    } // end of test step testIfConceptsAddedAfterPurge

    /**
     * In this test, vocabulary have an invalid base uri. An exception should be received.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionWhenVocabularyDoesNotHaveAValidBaseUri() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_INVALID_VOCABULARY_ID);
        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_1.csv");
        try {
            // import CSV into database
            vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct", "Vocabulary does not have a valid base URI", e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionWhenVocabularyDoesNotHaveAValidBaseUri

    /**
     * Check that no errors are generated when we send RDF with no properties.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testNoErrorIsGeneratedWhenSendingNothing() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // Create a string with just the top-level element. It has no data.
        Reader reader = new StringReader("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>");

        // import RDF into database
        List<String> logMessages = vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // Nothing seen, nothing updated, no error generated
        for (String l : logMessages) {
            if (l.startsWith("Number of predicates seen:")) {
                Assert.assertEquals("Number of predicates seen: 0", l);
            }
            if (l.startsWith("Number of updated concepts:")) {
                Assert.assertEquals("Number of updated concepts: 0", l);
            }
        }
    } // end of testNoErrorIsGeneratedWhenSendingNothing

    /**
     * In this test, a related element added with different type of base uri. i.e. base uri is NOT like:
     * http://<dd_host>/<vocabulary_folder_identifier>/<vocabulary_identifier>/
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfRelatedElementAddedCorrectly() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_10.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // query updated concept
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // manually update initial values of concepts for comparison
        VocabularyConcept vc9 = findVocabularyConceptById(concepts, 9);

        String identifier = "skos:exactMatch";
        List<List<DataElement>> dataElements = vc9.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        Assert.assertNotNull("Element's shouldn't be null", elems);
        Assert.assertEquals("Number of elements", 1, elems.size());
        DataElement element = elems.get(0);

        Assert.assertEquals("Identifier", identifier, element.getIdentifier());
        Assert.assertEquals("Id", 16, element.getId());
        Assert.assertEquals("Related Concept Id", 12, (long) element.getRelatedConceptId());
        Assert.assertEquals("Related Concept Identifier", "rdf_test_concept_777", element.getRelatedConceptIdentifier());
        Assert.assertEquals("Related Concept Label", "rdf_test_concept_label_777", element.getRelatedConceptLabel());
        Assert.assertEquals("Related Concept Vocabulary", "rdf_header_vocab_3", element.getRelatedConceptVocabulary());
        Assert.assertEquals("Related Concept Base Uri", "http://tripledev.ee/vocabulary/a_vocabulary_folder/a_vocabulary_name/",
                element.getRelatedConceptBaseURI());
        Assert.assertNull("Element value", element.getAttributeValue());
        Assert.assertNull("Element language", element.getAttributeLanguage());
    }// end of test step testIfConceptAndElementsUpdated

    /**
     * In this test, single concept RDF is imported. concept is a non existing concept to be imported with data elements (both rdf
     * and dd namespaces)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedWithDDNamespace() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_11.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("rdf_test_concept_5");
        vc11.setLabel("rdf_test_concept_label_5");
        vc11.setDefinition("rdf_test_concept_def_5");
        vc11.setStatus(StandardGenericStatus.VALID);
        vc11.setStatusModified(new Date(System.currentTimeMillis()));
        vc11.setAcceptedDate(new Date(System.currentTimeMillis()));

        // create element attributes (there is only one concept)
        List<List<DataElement>> elementAttributes = new ArrayList<List<DataElement>>();
        DataElement elem = null;
        String identifier = null;
        int dataElemId = -1;
        List<DataElement> elements = null;

        // AnotherCode
        identifier = "AnotherCode";
        dataElemId = 2;
        elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("HCO2_rdf_test_concept_5");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("2_HCO2_rdf_test_concept_5");
        elements.add(elem);
        elementAttributes.add(elements);

        // geo:lat
        identifier = "geo:lat";
        dataElemId = 5;
        elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("geo_lat_concept_5");
        elements.add(elem);
        elementAttributes.add(elements);

        // skos:prefLabel
        identifier = "skos:prefLabel";
        dataElemId = 8;
        elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg_rdf_test_concept_5");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elementAttributes.add(elements);

        // skos:definition
        identifier = "skos:definition";
        dataElemId = 9;
        elements = new ArrayList<DataElement>();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("de_rdf_test_concept_5");
        elem.setAttributeLanguage("de");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("en_rdf_test_concept_5");
        elem.setAttributeLanguage("en");
        elements.add(elem);
        elementAttributes.add(elements);

        vc11.setElementAttributes(elementAttributes);
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 4 vocabulary concepts", updatedConcepts.size(), 4);

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(3).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAddedWithDDNamespace

    private Map<String, List<String>> getDatatypeElemAttrs(String type) {
        Map<String, List<String>> elemAttrValues = new HashMap<String, List<String>>();
        List<String> aValues = new ArrayList<String>();
        aValues.add(type);
        elemAttrValues.put("Datatype", aValues);

        return elemAttrValues;
    }

    @Test
    public void testIfRelationsCreatedAfterImport() throws Exception {
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_REFERENCES_ID);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_12.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);

        vocabularyFolder = vocabularyService.getVocabularyWithConcepts("reftest", "reftest");

        Assert.assertEquals("3 concepts have to be imported", 3, vocabularyFolder.getConcepts().size());

        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        VocabularyConcept c1001 = findVocabularyConceptByIdentifier(updatedConcepts, "1001");
        VocabularyConcept c1002 = findVocabularyConceptByIdentifier(updatedConcepts, "1002");
        VocabularyConcept c1003 = findVocabularyConceptByIdentifier(updatedConcepts, "1003");

        List<List<DataElement>> c1001Attrs = c1001.getElementAttributes();
        List<List<DataElement>> c1002Attrs = c1002.getElementAttributes();
        List<List<DataElement>> c1003Attrs = c1003.getElementAttributes();

        Assert.assertEquals("concept 1001 should have 2 elements dct:replaces and skos:exactMatch", 2, c1001Attrs.size());
        List<DataElement> dctReplacesElems = VocabularyImportBaseHandler.getDataElementValuesByName("dct:replaces", c1001Attrs);

        Assert.assertEquals("concept 1001 should have references of type dct:replaces", 2, c1001Attrs.size());

        boolean foundRefTo1003 = false;
        for (DataElement de : dctReplacesElems) {
            if ("1003".equals(de.getRelatedConceptIdentifier())) {
                foundRefTo1003 = true;
            }
        }
        Assert.assertTrue("Not found dct:replaces relation from 1001 to 1003", foundRefTo1003);

        List<DataElement> dctIsReplacedByElems = VocabularyImportBaseHandler.getDataElementValuesByName("dct:isReplacedBy", c1002Attrs);
        Assert.assertEquals("concept 1002 should have 1 elements dct:isReplacedBy", 1, dctIsReplacedByElems.size());

        boolean foundRefTo1001 = c1001.getId() == dctIsReplacedByElems.get(0).getRelatedConceptId();
        Assert.assertTrue("Not found dct:isReplacedBy relation from 1002 to 1001", foundRefTo1001);
    }

    @Test
    @Rollback
    public void testIfLocalRefRelationsCreatedAfterImport() throws Exception {
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(9);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_14_geography_doctored.rdf");

        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);

        vocabularyFolder = vocabularyService.getVocabularyWithConcepts("geography", "reftest");

        Assert.assertEquals("6 concepts have to be imported", 6, vocabularyFolder.getConcepts().size());

        List<VocabularyConcept> updatedConcepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);

        VocabularyConcept world = findVocabularyConceptByIdentifier(updatedConcepts, "1");
        VocabularyConcept europe = findVocabularyConceptByIdentifier(updatedConcepts, "2");
        VocabularyConcept greece = findVocabularyConceptByIdentifier(updatedConcepts, "3");
        VocabularyConcept denmark = findVocabularyConceptByIdentifier(updatedConcepts, "4");

        VocabularyConcept africa = findVocabularyConceptByIdentifier(updatedConcepts, "5");
        VocabularyConcept egypt = findVocabularyConceptByIdentifier(updatedConcepts, "6");

        Set<Integer> worldNarrower = new HashSet<Integer>();
        worldNarrower.add(europe.getId());
        worldNarrower.add(africa.getId());

        Set<Integer> europeNarrower = new HashSet<Integer>();
        europeNarrower.add(greece.getId());
        europeNarrower.add(denmark.getId());

        {//World
            List<List<DataElement>> conceptAttributes = world.getElementAttributes();

            List<DataElement> skosNarrowersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", conceptAttributes);

            int nrOfSkosNarrowersForConcept = skosNarrowersForConcept == null ? 0 : skosNarrowersForConcept.size();
            Assert.assertEquals("World should have references of type skos:narrower", 2, nrOfSkosNarrowersForConcept);

            for (DataElement element : skosNarrowersForConcept) {
                worldNarrower.remove(element.getId());
            }
            Assert.assertEquals("World should have references of type skos:narrower to Europe and Africa", 2, worldNarrower.size());
        }

        {//Europe
            List<List<DataElement>> conceptAttributes = europe.getElementAttributes();

            List<DataElement> skosNarrowersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", conceptAttributes);
            List<DataElement> skosBroadersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", conceptAttributes);

            Assert.assertEquals("Europe should have references of type skos:narrower", 2, skosNarrowersForConcept.size());
            Assert.assertEquals("Europe should have references of type skos:broader", 1, skosBroadersForConcept.size());

            for (DataElement element : skosNarrowersForConcept) {
                europeNarrower.remove(element.getId());
            }
            Assert.assertEquals("Europe should have references of type skos:narrower to Greece and Denmark", 2, europeNarrower.size());
            Assert.assertEquals("Europe should have references of type skos:broader to World", (int) skosBroadersForConcept.get(0).getRelatedConceptId(),
                    world.getId());
        }

        {//Greece
            List<List<DataElement>> conceptAttributes = greece.getElementAttributes();

            List<DataElement> skosBroadersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", conceptAttributes);

            Assert.assertEquals("Greece should have references of type skos:broader to Europe", 1, skosBroadersForConcept.size());
            Assert.assertEquals("Greece should have references of type skos:broader to Europe", (int) skosBroadersForConcept.get(0).getRelatedConceptId(),
                    europe.getId());
        }

        {//Denmark
            List<List<DataElement>> conceptAttributes = denmark.getElementAttributes();

            List<DataElement> skosBroadersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", conceptAttributes);

            Assert.assertEquals("Denmark should have references of type skos:broader", 1, skosBroadersForConcept.size());
            Assert.assertEquals("Denmark should have references of type skos:broader to Europe", (int) skosBroadersForConcept.get(0).getRelatedConceptId(),
                    europe.getId());
        }

        {//Africa
            List<List<DataElement>> conceptAttributes = africa.getElementAttributes();

            List<DataElement> skosNarrowersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", conceptAttributes);
            List<DataElement> skosBroadersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", conceptAttributes);

            Assert.assertEquals("Africa should have references of type skos:narrower to Egypt", 1, skosNarrowersForConcept.size());
            Assert.assertEquals("Africa should have references of type skos:broader to World", 1, skosBroadersForConcept.size());

            Assert.assertEquals("Africa should have references of type skos:narrower to Egypt", (int) skosNarrowersForConcept.get(0).getRelatedConceptId(),
                    egypt.getId());
            Assert.assertEquals("Africa should have references of type skos:broader to World", (int) skosBroadersForConcept.get(0).getRelatedConceptId(),
                    world.getId());
        }

        {//Egypt
            List<List<DataElement>> conceptAttributes = egypt.getElementAttributes();

            List<DataElement> skosBroadersForConcept = VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", conceptAttributes);

            Assert.assertEquals("Egypt should have references of type skos:broader to Africa", 1, skosBroadersForConcept.size());
            Assert.assertEquals("Egypt should have references of type skos:broader to Africa", (int) skosBroadersForConcept.get(0).getRelatedConceptId(),
                    africa.getId());
        }
    }//end of test step testIfLocalRefRelationsCreatedAfterImport

    @Test
    @Rollback
    public void testImportWithEncodedIdentifier() throws Exception {
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);
        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_test_13.rdf");
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        List<VocabularyConcept> concepts = getValidVocabularyConceptsWithAttributes(vocabularyFolder);
        VocabularyConcept vc = findVocabularyConceptByIdentifier(concepts, "Cervus elaphus corsicanus");

        Assert.assertNotNull(vc);
    }
}
