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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.unitils.UnitilsJUnit4;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.util.VocabularyCSVOutputHelper;

/**
 * JUnit integration test with Unitils for CSV Vocabulary Import Service.
 *
 * @author
 */
@SpringApplicationContext("spring-context.xml")
public class CSVVocabularyImportServiceTest extends UnitilsJUnit4 {

    private static final int TEST_VALID_VOCAB_FOLDER_ID = 4;

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(CSVVocabularyImportServiceTest.class);

    /** Vocabulary folder CSV import service. */
    @SpringBeanByType
    private ICSVVocabularyImportService vocabularyImportService;

    /** Vocabulary service. */
    @SpringBeanByType
    private IVocabularyService vocabularyService;

    /** Data elements service. */
    @SpringBeanByType
    private IDataService dataService;

    @SpringBeanByType
    private PlatformTransactionManager transactionManager;

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
     * Get a reader from given CSV file location. If there is a BOM character, skip it.
     *
     * @param resourceLoc
     *            CSV file location
     * @return Reader object (BOM skipped)
     * @throws Exception
     *             if an error occurs
     */
    private Reader getReaderFromCsvResource(String resourceLoc) throws Exception {

        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceLoc);
        byte[] firstThreeBytes = new byte[3];
        is.read(firstThreeBytes);

        if (!Arrays.equals(firstThreeBytes, VocabularyCSVOutputHelper.BOM_BYTE_ARRAY)) {
            is.close();
            is = getClass().getClassLoader().getResourceAsStream(resourceLoc);
        }
        InputStreamReader reader = new InputStreamReader(is);

        return reader;
    }// end of method getReaderFromCsvResource

    /**
     * Utility code to make test code more readable. Returns vocabulary concepts with attributes by delegating call to
     * vocabularyService
     *
     * @param vf
     *            VocabularyFolder which holds concepts
     * @return List of vocabulary concepts of given folder
     * @throws Exception
     *             if an error occurs
     */
    private List<VocabularyConcept> getVocabularyConceptsWithAttributes(VocabularyFolder vf) throws Exception {
        return vocabularyService.getVocabularyConceptsWithAttributes(vf.getId(), vf.isNumericConceptIdentifiers(),
                ObsoleteStatus.ALL);
    }// end of method getVocabularyConceptsWithAttributes

    /**
     * Utility code to make test code more readable. Finds DataElement with given name in a list
     *
     * @param elems
     *            DataElements to be searched
     * @param attrValue
     *            Value for comparison
     * @return First found DataElement
     */
    private DataElement findDataElemByAttrValue(List<DataElement> elems, String attrValue) {
        return (DataElement) CollectionUtils.find(elems, new DataElementEvaluateOnAttributeValuePredicate(attrValue));
    }// end of method findDataElemByAttrValue

    /**
     * Utility code to make test code more readable. Finds VocabularyConcept with given id in a list
     *
     * @param concepts
     *            VocabularyConcepts to be searched
     * @param id
     *            Id for comparison
     * @return First found VocabularyConcept
     */
    private VocabularyConcept findVocabularyConceptById(List<VocabularyConcept> concepts, int id) {
        return (VocabularyConcept) CollectionUtils.find(concepts, new VocabularyConceptEvaluateOnIdPredicate(id));
    }// end of method findVocabularyConceptById

    /**
     * In this test, three line CSV is imported.
     * Row 1 includes updated values for concept and DataElements (no insertion, only update)
     * Row 2 is a commented out line, it has updated values but importer should ignore this line.
     * Row 3 is not updated (it has same values in database, there should be no update for this vocabulary concept)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_1.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        //transactionManager.getTransaction(null).flush();
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("csv_test_concept_label_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element = findDataElemByAttrValue(elems, "bg_csv_test_concept_1");
        element.setAttributeValue("bg_csv_test_concept_1_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "et", dataElements);
        element = findDataElemByAttrValue(elems, "et_csv_test_concept_1");
        element.setAttributeValue("et_csv_test_concept_1_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // boolean result2 = EqualsBuilder.reflectionEquals(concepts, updatedConcepts);
    }// end of test step testIfConceptAndElementsUpdated

    /**
     * In this test, two line CSV is imported.
     * Row 1 includes updated values for concept and DataElements (no insertion, only update)
     * Row 2 includes updated values for concept and DataElements (no insertion, only update)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_2.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setDefinition("csv_test_concept_def_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element = findDataElemByAttrValue(elems, "bg2_csv_test_concept_1");
        element.setAttributeValue("bg2_csv_test_concept_1_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "en", dataElements);
        element = findDataElemByAttrValue(elems, "en_csv_test_concept_1");
        element.setAttributeValue("en_csv_test_concept_1_updated");

        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setLabel("csv_test_concept_label_3_updated");

        dataElements = vc10.getElementAttributes();
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        element = findDataElemByAttrValue(elems, "bg_csv_test_concept_3");
        element.setAttributeValue("bg_csv_test_concept_3_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:definition", "pl", dataElements);
        element = findDataElemByAttrValue(elems, "pl_csv_test_concept_3");
        element.setAttributeValue("pl_csv_test_concept_3_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAndElementsUpdated

    /**
     * In this test, one line CSV is imported.
     * Row 1 includes a non existing concept to be imported with data elements
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAdded() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_3.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

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
     * In this test, one line CSV is imported.
     * Row 1 includes a non existing concept to be imported with data elements after purge
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfNewConceptAddedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_3.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

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
        Assert.assertEquals("Updated Concepts does not include 1 vocabulary concept", updatedConcepts.size(), 1);

        // last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(0).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAddedAfterPurge

    /**
     * In this test, two line CSV is imported.
     * Row 1 includes updated values for concept and DataElements (no insertion, only update)
     * Row 2 includes updated values for concept and DataElements (no insertion, only update)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdatedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_2.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setDefinition("csv_test_concept_def_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element = findDataElemByAttrValue(elems, "bg2_csv_test_concept_1");
        element.setAttributeValue("bg2_csv_test_concept_1_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "en", dataElements);
        element = findDataElemByAttrValue(elems, "en_csv_test_concept_1");
        element.setAttributeValue("en_csv_test_concept_1_updated");

        VocabularyConcept vc10 = findVocabularyConceptById(concepts, 10);
        vc10.setLabel("csv_test_concept_label_3_updated");

        dataElements = vc10.getElementAttributes();
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        element = findDataElemByAttrValue(elems, "bg_csv_test_concept_3");
        element.setAttributeValue("bg_csv_test_concept_3_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:definition", "pl", dataElements);
        element = findDataElemByAttrValue(elems, "pl_csv_test_concept_3");
        element.setAttributeValue("pl_csv_test_concept_3_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);
        vc8.setId(updatedConcepts.get(0).getId());
        vc10.setId(updatedConcepts.get(1).getId());
        concepts = new ArrayList<VocabularyConcept>();
        concepts.add(vc8);
        concepts.add(vc10);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAndElementsUpdatedAfterPurge

    /**
     * In this test, two line CSV is imported.
     * Rows are derived from base CSV. Just identifiers are updated. Purge operation is tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedAfterPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_4.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, false);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        concepts.remove(2);// remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        // concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(updatedConcepts.get(0).getId());
        concepts.get(1).setId(updatedConcepts.get(1).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterPurge

    /**
     * In this test, two line CSV is imported.
     * Rows are derived from base CSV. Just identifiers are updated. Both purge operations are tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedAfterAllPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_4.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values of new concept for comparison
        concepts.remove(2);// remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        // concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(updatedConcepts.get(0).getId());
        concepts.get(1).setId(updatedConcepts.get(1).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterAllPurge

    /**
     * In this test, two line CSV is imported.
     * Rows are derived from base CSV. Just identifiers are updated. Both purge operations are tested.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAddedBindedElementsRemovedAndNewElementsAddedAfterAllPurge() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_5.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually create values for data elements
        String[] boundElementIdentifiers =
                new String[] {"skos:relatedMatch", "skos:prefLabel", "skos:definition", "env:prefLabel", "env:definition",
                        "env:declaration"};
        List<String> bindedElements = new ArrayList<String>(Arrays.asList(boundElementIdentifiers));

        // get updated values of data elements of this vocabulary folder
        List<DataElement> bindedElementsUpdated = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        Assert.assertEquals("Updated bound elements does not include 6 items", bindedElementsUpdated.size(), 6);

        // compare manually updated objects with queried ones (after import operation)
        // just compare identifiers
        for (DataElement bindedElem : bindedElementsUpdated) {
            Assert.assertTrue("Does not contain element with identifier: " + bindedElem.getIdentifier(),
                    bindedElements.remove(bindedElem.getIdentifier()));
        }
        Assert.assertEquals("Some elements didn't match", bindedElements.size(), 0);

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
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        // concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(updatedConcepts.get(0).getId());
        concepts.get(1).setId(updatedConcepts.get(1).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterAllPurge

    /**
     * In this test, 9 lines CSV is imported.
     * Row 1 includes updated values for concept and DataElement (no insertion, only update)
     * Row 2 is a commented out line, it has updated values but importer should ignore this line.
     * Row 3 has wrong base uri, it has updated values but importer should ignore this line.
     * Row 4 does not have base uri, it has updated values but importer should ignore this line.
     * Row 5 has blank base uri, it has updated values but importer should ignore this line.
     * Row 6 has identifier starting with /, it has updated values but importer should ignore this line.
     * Row 7 has identifier ending with /, it has updated values but importer should ignore this line.
     * Row 8 is a duplicate with concept at row 1, it has updated values but importer should ignore this line.
     * Row 9 has not equal column with header, it has updated values but importer should ignore this line.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfLinesAreSkippedAndDataElementsNotPurged() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        // get initial values of data elements of this vocabulary folder
        List<DataElement> bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_6.csv");

        // import CSV into database
        List<String> logMessages = vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, true);
        Assert.assertFalse("Transaction rollbacked (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // manually update initial values of concepts for comparison
        // only vocabulary concept 1 should change
        VocabularyConcept vc8 = findVocabularyConceptById(concepts, 8);
        vc8.setLabel("csv_test_concept_label_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems = null;
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element = findDataElemByAttrValue(elems, "bg_csv_test_concept_1");
        element.setAttributeValue("bg_csv_test_concept_1_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // get updated values of data elements of this vocabulary folder (there shouldn't be any difference)
        List<DataElement> bindedElementsUpdated = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        // compare
        ReflectionAssert.assertReflectionEquals(bindedElements, bindedElementsUpdated, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // and finally compare log messages
        String[] logMessagesManually =
                new String[] {
                        "Row (3) is skipped (Concept is excluded by user from update operation).",
                        "Row (4) is skipped (Base URI does not match with Vocabulary).",
                        "Row (5) is skipped (Base URI does not match with Vocabulary).",
                        "Row (6) is skipped (Base URI is empty).",
                        "Row (7) does not contain a valid concept identifier.",
                        "Row (8) does not contain a valid concept identifier.",
                        "Row (9) duplicates with a previous concept, it is skipped.",
                        "Row (10) does not have same number of columns with header, it is skipped. It should have have same number of columns (empty or filled).",
                        "CSV imported into Database."};
        // compare
        ReflectionAssert.assertReflectionEquals(Arrays.asList(logMessagesManually), logMessages);
    }// end of test step testIfLinesAreSkippedAndDataElementsNotPurged

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
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_7.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals(
                    "Exception Message is not correct",
                    "Missing headers! CSV file should contain following headers: [URI, Label, Definition, Notation, StartDate, EndDate]",
                    e.getMessage());
            Assert.assertTrue("Transaction didn't rollbacked", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenFixedHeadersAreMissing

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (empty header column). All transaction should be rollbacked
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnIsEmpty() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_8.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct", "Header for column (8) is empty!", e.getMessage());
            Assert.assertTrue("Transaction didn't rollbacked", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnIsEmpty

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (not found element to bind). All transaction should be rollbacked
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnIsNotFoundElement() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_9.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals(
                    "Exception Message is not correct",
                    "Cannot find any data element for column: skos:prefLabelllllllll. Please bind element manually then upload CSV.",
                    e.getMessage());
            Assert.assertTrue("Transaction didn't rollbacked", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnIsNotFoundElement

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (more than one found element to bind). All transaction should be rollbacked
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnIsFoundMoreThanOne() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_10.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals(
                    "Exception Message is not correct",
                    "Cannot find any data element for column: env:de. Please bind element manually then upload CSV.",
                    e.getMessage());
            Assert.assertTrue("Transaction didn't rollbacked", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnIsFoundMoreThanOne

    /**
     * In this test, three line CSV is imported. All rows includes updated values. But there should be no update performed since it
     * does not have valid headers (one found element to bind but does not match exactly). All transaction should be rollbacked
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testExceptionAndRollbackWhenAHeaderColumnDoesNotExactlyMatch() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_11.csv");

        try {
            // import CSV into database
            vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, true, true);
            Assert.fail("Exception is not received");
        } catch (ServiceException e) {
            Assert.assertEquals("Exception Message is not correct",
                    "Cannot find any data element for column: env:dec. Please bind element manually then upload CSV.", e.getMessage());
            Assert.assertTrue("Transaction didn't rollbacked", transactionManager.getTransaction(null).isRollbackOnly());
        }
    }// end of test step testExceptionAndRollbackWhenAHeaderColumnDoesNotExactlyMatch

    /**
     *
     * Inner class used to search for a VocabularyConcept in a Collection using it's id
     *
     */
    public static class VocabularyConceptEvaluateOnIdPredicate implements Predicate {
        private int id = -1;

        public VocabularyConceptEvaluateOnIdPredicate(int id) {
            this.id = id;
        }

        @Override
        public boolean evaluate(Object object) {
            VocabularyConcept vc = (VocabularyConcept) object;
            return this.id == vc.getId();
        }
    }// end of inner class VocabularyConceptEvaluateOnIdPredicate

    /**
     *
     * Inner class used to search for a DataElement using it's attribute value in a Collection
     *
     */
    public static class DataElementEvaluateOnAttributeValuePredicate implements Predicate {
        private String value = null;

        public DataElementEvaluateOnAttributeValuePredicate(String value) {
            this.value = value;
        }

        @Override
        public boolean evaluate(Object object) {
            DataElement elem = (DataElement) object;
            return StringUtils.equals(value, elem.getAttributeValue());
        }
    }// end of inner class DataElementEvaluateOnAttributeValuePredicate
}// end of test case CSVVocabularyImportServiceTest
