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
import org.unitils.UnitilsJUnit4;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
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

        // manually create values of new concept for comparison
        VocabularyConcept vc11 = new VocabularyConcept();
        //vc11.setId(11); //this field will be updated after re-querying
        vc11.setIdentifier("csv_test_concept_4");
        vc11.setLabel("csv_test_concept_label_4");
        vc11.setDefinition("csv_test_concept_def_4");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        vc11.setCreated(dateFormatter.parse("2014-02-17"));

        // create element attributes
        List<List<DataElement>> elementAttributes = new ArrayList<List<DataElement>>();
        DataElement elem = null;
        String identifier = null;
        DataElementsFilter elementsFilter = new DataElementsFilter();
        elementsFilter.setRegStatus("Released");
        elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
        DataElementsResult elementsResult = null;
        int dataElemId = -1;

        // skos:prefLabel
        identifier = "skos:prefLabel";
        ArrayList<DataElement> skosPrefLabel = new ArrayList<DataElement>();
        elementsFilter.setIdentifier(identifier);
        elementsResult = dataService.searchDataElements(elementsFilter);
        dataElemId = elementsResult.getDataElements().get(0).getId();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg_csv_test_concept_4");
        elem.setAttributeLanguage("bg");
        skosPrefLabel.add(elem);
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("bg2_csv_test_concept_4");
        elem.setAttributeLanguage("bg");
        skosPrefLabel.add(elem);
        elementAttributes.add(skosPrefLabel);

        // skos:definition
        identifier = "skos:definition";
        ArrayList<DataElement> skosDef = new ArrayList<DataElement>();
        elementsFilter.setIdentifier(identifier);
        elementsResult = dataService.searchDataElements(elementsFilter);
        dataElemId = elementsResult.getDataElements().get(0).getId();
        elem = new DataElement();
        elem.setId(dataElemId);
        elem.setIdentifier(identifier);
        elem.setAttributeValue("de_csv_test_concept_4");
        elem.setAttributeLanguage("de");
        skosDef.add(elem);
        elementAttributes.add(skosDef);

        vc11.setElementAttributes(elementAttributes);
        concepts.add(vc11);

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 4 vocabulary concepts", updatedConcepts.size(), 4);

        //last object should be the inserted one, so use it is id to set (all other fields are updated manually)
        vc11.setId(updatedConcepts.get(3).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfNewConceptAdded

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

        // manually create values of new concept for comparison
        concepts.remove(2);//remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        //concepts should be inserted in the same order as they are in csv file, get ids from updated beans
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

        // manually create values of new concept for comparison
        concepts.remove(2);//remove last object
        // there is not much object just update, no need to iterate
        concepts.get(0).setIdentifier("csv_test_concept_1_after_purge");
        concepts.get(1).setIdentifier("csv_test_concept_2_after_purge");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertEquals("Updated Concepts does not include 2 vocabulary concepts", updatedConcepts.size(), 2);

        //concepts should be inserted in the same order as they are in csv file, get ids from updated beans
        concepts.get(0).setId(updatedConcepts.get(0).getId());
        concepts.get(1).setId(updatedConcepts.get(1).getId());

        // compare manually updated objects with queried ones (after import operation)
        ReflectionAssert.assertReflectionEquals(concepts, updatedConcepts, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }// end of test step testIfConceptsAddedAfterAllPurge

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
