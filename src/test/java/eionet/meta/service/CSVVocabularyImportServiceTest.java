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
 * Agency.  Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */

package eionet.meta.service;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.VocabularyOutputHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.spring.annotation.SpringBeanByType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JUnit integration test with Unitils for CSV Vocabulary Import Service.
 *
 * @author enver
 */
public class CSVVocabularyImportServiceTest extends VocabularyImportServiceTestBase {
    /**
     * Vocabulary folder CSV import service.
     */
    @SpringBeanByType
    private ICSVVocabularyImportService vocabularyImportService;

    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-emptydb.xml");
        DBUnitHelper.loadData("csv_import/seed-vocabularycsv-import.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("csv_import/seed-vocabularycsv-import.xml");
    }

    /**
     * {@inheritDoc} This method skips BOM character
     */
    protected Reader getReaderFromResource(String resourceLoc) throws Exception {

        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceLoc);
        byte[] firstThreeBytes = new byte[3];
        is.read(firstThreeBytes);

        if (!Arrays.equals(firstThreeBytes, VocabularyOutputHelper.getBomByteArray())) {
            is.close();
            is = getClass().getClassLoader().getResourceAsStream(resourceLoc);
        }
        InputStreamReader reader = new InputStreamReader(is);

        return reader;
    } // end of method getReaderFromResource

    /**
     * In this test, three line CSV is imported. Row 1 includes updated values for concept and DataElements (no insertion, only
     * update) Row 2 is a commented out line, it has updated values but importer should ignore this line. Row 3 is not updated (it
     * has same values in database, there should be no update for this vocabulary concept)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_1.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        // transactionManager.getTransaction(null).flush();
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("csv_test_concept_label_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg_csv_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        element = new DataElement();
        element.setAttributeValue("et_csv_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("et");
        elems.add(element);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptAndElementsUpdated

    /**
     * In this test, two line CSV is imported. Row 1 includes updated values for concept and DataElements (no insertion, only
     * update) Row 2 includes updated values for concept and DataElements (no insertion, only update)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_2.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setDefinition("csv_test_concept_def_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();

        element.setAttributeValue("bg2_csv_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);
        element = new DataElement();
        element.setAttributeValue("en_csv_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("en");
        elems.add(element);

        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setLabel("csv_test_concept_label_3_updated");

        dataElements = vc10.getElementAttributes();
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        element = new DataElement();
        element.setAttributeValue("bg_csv_test_concept_3_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        dataElemId = 9;
        identifier = "skos:definition";
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        element = new DataElement();
        element.setAttributeValue("pl_csv_test_concept_3_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("pl");
        elems.add(element);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAndElementsUpdated

    /**
     * In this test, one line CSV is imported. Row 1 includes a non existing concept to be imported with data elements
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAdded() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_3.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("csv_test_concept_4");
        vc11.setLabel("csv_test_concept_label_4");
        vc11.setDefinition("csv_test_concept_def_4");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        vc11.setCreated(dateFormatter.parse("2014-02-17"));

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
        elem.setAttributeValue("bg_csv_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg2_csv_test_concept_4");
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
        elem.setAttributeValue("de_csv_test_concept_4");
        elem.setAttributeLanguage("de");
        elements.add(elem);
        elementAttributes.add(elements);

        vc11.setElementAttributes(elementAttributes);
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 4 vocabulary concepts", updatedConcepts.size(), 4);

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(3).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAdded

    /**
     * In this test, one line CSV is imported. Row 1 includes a non existing concept to be imported with data elements, headers are
     * in arbitrary order
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedInArbitraryOrder() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_13.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("csv_test_concept_4");
        vc11.setLabel("csv_test_concept_label_4");
        vc11.setDefinition("csv_test_concept_def_4");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        vc11.setCreated(dateFormatter.parse("2014-02-17"));

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
        elem.setAttributeValue("bg_csv_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg2_csv_test_concept_4");
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
        elem.setAttributeValue("de_csv_test_concept_4");
        elem.setAttributeLanguage("de");
        elements.add(elem);
        elementAttributes.add(elements);

        vc11.setElementAttributes(elementAttributes);
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 4 vocabulary concepts", updatedConcepts.size(), 4);

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(3).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAdded

    /**
     * In this test, one line CSV is imported. Row 1 includes a non existing concept to be imported with data elements after purge
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_3.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        // vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("csv_test_concept_4");
        vc11.setLabel("csv_test_concept_label_4");
        vc11.setDefinition("csv_test_concept_def_4");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        vc11.setCreated(dateFormatter.parse("2014-02-17"));

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
        elem.setAttributeValue("bg_csv_test_concept_4");
        elem.setAttributeLanguage("bg");
        elements.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg2_csv_test_concept_4");
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
        elem.setAttributeValue("de_csv_test_concept_4");
        elem.setAttributeLanguage("de");
        elements.add(elem);
        elementAttributes.add(elements);

        vc11.setElementAttributes(elementAttributes);
        concepts = new ArrayList<VocabularyConcept>();
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 1 vocabulary concept", 1, updatedConcepts.size());

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(0).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAddedAfterPurge

    /**
     * In this test, two line CSV is imported. Row 1 includes updated values for concept and DataElements (no insertion, only
     * update) Row 2 includes updated values for concept and DataElements (no insertion, only update)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdatedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_2.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setDefinition("csv_test_concept_def_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element = findDataElemByAttrValue(elems, "bg2_csv_test_concept_1");
        element.setAttributeValue("bg2_csv_test_concept_1_updated");

        elems = VocabularyOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "en", dataElements);
        element = findDataElemByAttrValue(elems, "en_csv_test_concept_1");
        element.setAttributeValue("en_csv_test_concept_1_updated");

        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setLabel("csv_test_concept_label_3_updated");

        dataElements = vc10.getElementAttributes();
        elems = VocabularyOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        element = findDataElemByAttrValue(elems, "bg_csv_test_concept_3");
        element.setAttributeValue("bg_csv_test_concept_3_updated");

        elems = VocabularyOutputHelper.getDataElementValuesByNameAndLang("skos:definition", "pl", dataElements);
        element = findDataElemByAttrValue(elems, "pl_csv_test_concept_3");
        element.setAttributeValue("pl_csv_test_concept_3_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        VocabularyConcept vc8Updated = findVocabularyConceptByIdentifier(updatedConcepts, vc8.getIdentifier());
        vc8.setId(vc8Updated.getId());
        VocabularyConcept vc10Updated = findVocabularyConceptByIdentifier(updatedConcepts, vc10.getIdentifier());
        vc10.setId(vc10Updated.getId());
        concepts = new ArrayList<VocabularyConcept>();
        concepts.add(vc8);
        concepts.add(vc10);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAndElementsUpdatedAfterPurge

    /**
     * In this test, two line CSV is imported. Rows are derived from base CSV. Just identifiers are updated. Purge operation is
     * tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_4.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        concepts.remove(2);// remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", 2, updatedConcepts.size());

        // concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(0).getIdentifier()).getId());
        concepts.get(1).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(1).getIdentifier()).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterPurge

    /**
     * In this test, two line CSV is imported. Rows are derived from base CSV. Just identifiers are updated. Both purge operations
     * are tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedAfterAllPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_4.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        concepts.remove(2);// remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", 2, updatedConcepts.size());

        // concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(0).getIdentifier()).getId());
        concepts.get(1).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(1).getIdentifier()).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterAllPurge

    /**
     * In this test, two line CSV is imported. Rows are derived from base CSV. Just identifiers are updated. Both purge operations
     * are tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedBoundElementsRemovedAndNewElementsAddedAfterAllPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_5.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values for data elements
        String[] boundElementIdentifiers =
                new String[] {"skos:relatedMatch", "skos:prefLabel", "skos:definition", "env:prefLabel", "env:definition",
                        "env:declaration"};
        List<String> boundElements = new ArrayList<String>(Arrays.asList(boundElementIdentifiers));

        // get updated values of data elements of this vocabulary folder
        List<DataElement> boundElementsUpdated = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        Assert.assertEquals("Updated bound elements does not include 6 items", boundElementsUpdated.size(), 6);

        // compare manually updated objects with queried ones (after import operation)
        // just compare identifiers
        for (DataElement boundElem : boundElementsUpdated) {
            Assert.assertTrue("Does not contain element with identifier: " + boundElem.getIdentifier(),
                    boundElements.remove(boundElem.getIdentifier()));
        }
        Assert.assertEquals("Some elements didn't match", boundElements.size(), 0);

        // manually create values of new concepts for comparison
        concepts.remove(2);// remove last object
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge_2");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge_2");

        // create element attributes
        List<List<DataElement>> elementAttributes = null;
        DataElement elem = null;
        String identifier = null;
        int dataElemId = -1;
        List<DataElement> elements = null;

        // CONCEPT 1
        elementAttributes = new ArrayList<List<DataElement>>();
        // skos:relatedMatch
        identifier = "skos:relatedMatch";
        dataElemId = 6;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("csv_test_concept_1");
        elements.add(elem);

        elementAttributes.add(elements);

        // skos:prefLabel
        identifier = "skos:prefLabel";
        dataElemId = 8;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg_csv_test_concept_1");
        elem.setAttributeLanguage("bg");
        elements.add(elem);

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("en_csv_test_concept_1");
        elem.setAttributeLanguage("en");
        elements.add(elem);

        elementAttributes.add(elements);

        // skos:definition
        identifier = "skos:definition";
        dataElemId = 9;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("pl_csv_test_concept_1");
        elem.setAttributeLanguage("pl");
        elements.add(elem);

        elementAttributes.add(elements);

        // env:prefLabel
        identifier = "env:prefLabel";
        dataElemId = 10;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("en_csv_test_concept_1");
        elem.setAttributeLanguage("en");
        elements.add(elem);

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("lt_csv_test_concept_1");
        elem.setAttributeLanguage("lt");
        elements.add(elem);

        elementAttributes.add(elements);

        // env:definition
        identifier = "env:definition";
        dataElemId = 11;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("csv_test_concept_1");
        elements.add(elem);

        elementAttributes.add(elements);
        concepts.get(0).setElementAttributes(elementAttributes);

        // CONCEPT 2
        elementAttributes = new ArrayList<List<DataElement>>();
        // skos:relatedMatch
        identifier = "skos:relatedMatch";
        dataElemId = 6;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("csv_test_concept_2");
        elements.add(elem);

        elementAttributes.add(elements);

        // skos:prefLabel
        identifier = "skos:prefLabel";
        dataElemId = 8;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("et_csv_test_concept_2");
        elem.setAttributeLanguage("et");
        elements.add(elem);

        elementAttributes.add(elements);

        // skos:definition
        identifier = "skos:definition";
        dataElemId = 9;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("en_csv_test_concept_2");
        elem.setAttributeLanguage("en");
        elements.add(elem);

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("pl_csv_test_concept_2");
        elem.setAttributeLanguage("pl");
        elements.add(elem);

        elementAttributes.add(elements);

        // env:prefLabel
        identifier = "env:prefLabel";
        dataElemId = 10;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("en_csv_test_concept_2");
        elem.setAttributeLanguage("en");
        elements.add(elem);

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("de_csv_test_concept_2");
        elem.setAttributeLanguage("de");
        elements.add(elem);

        elementAttributes.add(elements);

        // env:declaration
        identifier = "env:declaration";
        dataElemId = 13;
        elements = new ArrayList<DataElement>();

        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("csv_test_concept_2");
        elements.add(elem);

        elementAttributes.add(elements);
        concepts.get(1).setElementAttributes(elementAttributes);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", 2, updatedConcepts.size());

        // concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(0).getIdentifier()).getId());
        concepts.get(1).setId(findVocabularyConceptByIdentifier(updatedConcepts, concepts.get(1).getIdentifier()).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterAllPurge

    /**
     * In this test, 9 lines CSV is imported. Row 1 includes updated values for concept and DataElement (no insertion, only update)
     * Row 2 is a commented out line, it has updated values but importer should ignore this line. Row 3 has wrong base uri, it has
     * updated values but importer should ignore this line. Row 4 does not have base uri, it has updated values but importer should
     * ignore this line. Row 5 has blank base uri, it has updated values but importer should ignore this line. Row 6 has identifier
     * starting with /, it has updated values but importer should ignore this line. Row 7 has identifier ending with /, it has
     * updated values but importer should ignore this line. Row 8 is a duplicate with concept at row 1, it has updated values but
     * importer should ignore this line. Row 9 has not equal column with header, it has updated values but importer should ignore
     * this line.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfLinesAreSkippedAndDataElementsNotPurged() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        // get initial values of data elements of this vocabulary folder
        List<DataElement> boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_6.csv");

        // import CSV into database
        List<String> logMessages = vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, true);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        // only vocabulary concept 1 should change
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("csv_test_concept_label_1_updated");

        int dataElemId = 8;
        String identifier = "skos:prefLabel";
        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyImportBaseHandler.getDataElementValuesByName(identifier, dataElements);
        DataElement element = new DataElement();
        element.setAttributeValue("bg_csv_test_concept_1_updated");
        element.setIdentifier(identifier);
        element.setId(dataElemId);
        element.setAttributeLanguage("bg");
        elems.add(element);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // get updated values of data elements of this vocabulary folder (there shouldn't be any difference)
        List<DataElement> boundElementsUpdated = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        // compare
        ReflectionAssert.assertReflectionEquals(boundElements, boundElementsUpdated, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // and finally compare log messages
        String[] logMessagesManually =
                new String[] {
                        "Row (3) was skipped (Concept was excluded by user from update operation).",
                        "Row (4) was skipped (Base URI did not match with Vocabulary).",
                        "Row (5) was skipped (Base URI did not match with Vocabulary).",
                        "Row (6) was skipped (Base URI was empty).",
                        "Row (7) did not contain a valid concept identifier.",
                        "Row (8) did not contain a valid concept identifier.",
                        "Row (9) duplicated with a previous concept, it was skipped.",
                        "Row (10) did not have same number of columns with header, it was skipped. It should have have same number of columns (empty or filled).",
                        "CSV imported into Database."};
        // compare
        ReflectionAssert.assertReflectionEquals(Arrays.asList(logMessagesManually), logMessages);
    }// end of test step testIfLinesAreSkippedAndDataElementsNotPurged

    /**
     * In this test, two line CSV is imported. New concepts added to another vocabulary folder and related items are set.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfRelatedConceptsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(6);
        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_12.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated concepts does not include 2 items", 2, updatedConcepts.size());

        // Check for first concept
        VocabularyConcept concept = findVocabularyConceptByIdentifier(updatedConcepts, "csv_test_concept_1");
        List<DataElement> elements =
                VocabularyOutputHelper.getDataElementValuesByName("skos:relatedMatch", concept.getElementAttributes());
        DataElement element = elements.get(0);
        Assert.assertEquals("Related Concept Id Doesn't Match", 11, element.getRelatedConceptId().intValue());
        Assert.assertEquals("Related Concept Identifier Doesn't Match", "csv_test_concept_3",
                element.getRelatedConceptIdentifier());
        Assert.assertEquals("Related Concept Label Doesn't Match", "csv_test_concept_label_3", element.getRelatedConceptLabel());
        Assert.assertEquals("Related Concept Vocabulary Doesn't Match", "csv_header_vocab", element.getRelatedConceptVocabulary());
        Assert.assertNull("Attribute Value is Not Null", element.getAttributeValue());

        elements = VocabularyOutputHelper.getDataElementValuesByName("skos:related", concept.getElementAttributes());
        element = elements.get(0);
        Assert.assertEquals("Related Concept Id Doesn't Match",
                findVocabularyConceptByIdentifier(updatedConcepts, "csv_test_concept_2").getId(), element.getRelatedConceptId()
                        .intValue());
        Assert.assertEquals("Related Concept Identifier Doesn't Match", "csv_test_concept_2",
                element.getRelatedConceptIdentifier());
        Assert.assertEquals("Related Concept Label Doesn't Match", "csv_test_concept_label_2", element.getRelatedConceptLabel());
        Assert.assertEquals("Related Concept Vocabulary Doesn't Match", "csv_header_vocab_2",
                element.getRelatedConceptVocabulary());
        Assert.assertNull("Attribute Value is Not Null", element.getAttributeValue());

        // Check for second concept
        concept = findVocabularyConceptByIdentifier(updatedConcepts, "csv_test_concept_2");
        elements = VocabularyOutputHelper.getDataElementValuesByName("skos:relatedMatch", concept.getElementAttributes());
        element = elements.get(0);
        Assert.assertNull("Related Concept Id is Not Null", element.getRelatedConceptId());
        Assert.assertNull("Related Concept Identifier is Not Null", element.getRelatedConceptIdentifier());
        Assert.assertNull("Related Concept Label is Not Null", element.getRelatedConceptLabel());
        Assert.assertNull("Related Concept Vocabulary is Not Null", element.getRelatedConceptVocabulary());
        Assert.assertEquals("Attribute Value Doesn't Match",
                "http://127.0.0.1:8080/datadict/vocabulary/csv_header_vs/csv_header_vocab_2/csv_test_concept_3",
                element.getAttributeValue());

        elements = VocabularyOutputHelper.getDataElementValuesByName("skos:related", concept.getElementAttributes());
        element = elements.get(0);
        Assert.assertEquals("Related Concept Id Doesn't Match",
                findVocabularyConceptByIdentifier(updatedConcepts, "csv_test_concept_1").getId(), element.getRelatedConceptId()
                        .intValue()
        );
        Assert.assertEquals("Related Concept Identifier Doesn't Match", "csv_test_concept_1",
                element.getRelatedConceptIdentifier());
        Assert.assertEquals("Related Concept Label Doesn't Match", "csv_test_concept_label_1", element.getRelatedConceptLabel());
        Assert.assertEquals("Related Concept Vocabulary Doesn't Match", "csv_header_vocab_2",
                element.getRelatedConceptVocabulary());
        Assert.assertNull("Attribute Value is Not Null", element.getAttributeValue());
        Assert.assertNull("Attribute Value is Not Null", element.getAttributeValue());

    }// end of test step testIfRelatedConceptsUpdated

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (invalid fixed header). All transaction should be rollbacked
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenFixedHeadersAreMissing() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_7.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct", "Missing header! CSV file should start with header: 'URI'",
                    e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenFixedHeadersAreMissing

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (empty header column). All transaction should be rolled back
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnIsEmpty() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_8.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct", "Header for column (8) is empty!", e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnIsEmpty

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (not found element to bind). All transaction should be rolled back
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnIsNotFoundElement() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_9.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals(
                    "Exception Message is not correct",
                    "Cannot find any data element for column: skos:prefLabelllllllll. Please bind element manually then upload CSV.",
                    e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnIsNotFoundElement

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (more than one found element to bind). All transaction should be rolled back
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnIsFoundMoreThanOne() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_10.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct",
                    "Cannot find any data element for column: env:de. Please bind element manually then upload CSV.",
                    e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnIsFoundMoreThanOne

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (one found element to bind but does not match exactly). All transaction should be rolled back
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnDoesNotExactlyMatch() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for CSV file
        Reader reader = getReaderFromResource("csv_import/csv_import_test_11.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct",
                    "Cannot find any data element for column: env:dec. Please bind element manually then upload CSV.",
                    e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnDoesNotExactlyMatch

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
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct", "Vocabulary does not have a valid base URI", e.getMessage());
            Assert.assertTrue("Transaction wasn't rolled back", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionWhenVocabularyDoesNotHaveAValidBaseUri

}// end of test case CSVVocabularyImportServiceTest
