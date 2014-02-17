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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
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

    @SpringBeanByType
    private ICSVVocabularyImportService vocabularyImportService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @BeforeClass
    public static void loadData() throws Exception {
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
     * In this test, three line csv is imported.
     * Row 1 includes updated values for concept and datelements (no insertion, only update)
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
        List<VocabularyConcept> concepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_1.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = (VocabularyConcept) CollectionUtils.find(concepts, new VocabularyConceptEvaluateOnIdPredicate(8));
        vc8.setLabel("csv_test_concept_label_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems =
                VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element =
                (DataElement) CollectionUtils.find(elems,
                        new DataElementEvaluateOnAttributeValuePredicate("bg_csv_test_concept_1"));
        element.setAttributeValue("bg_csv_test_concept_1_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "et", dataElements);
        element =
                (DataElement) CollectionUtils.find(elems,
                        new DataElementEvaluateOnAttributeValuePredicate("et_csv_test_concept_1"));
        element.setAttributeValue("et_csv_test_concept_1_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        // compare manually updated objects with queried ones (after import operation)

        assertEquals("Expected equal list size", concepts.size(), updatedConcepts.size());
        for (int i = 0; i < concepts.size(); i++) {
            assertEquals("Expected equals concepts", concepts.get(i), updatedConcepts.get(i));
        }
    }// end of test step testIfConceptAndElementsUpdated

    /**
     * In this test, two line csv is imported.
     * Row 1 includes updated values for concept and datelements (no insertion, only update)
     * Row 2 includes updated values for concept and datelements (no insertion, only update)
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptsAndElementsUpdated() throws Exception {
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        // get reader for CSV file
        Reader reader = getReaderFromCsvResource("csv_import/csv_import_test_2.csv");

        // import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);

        // manually update initial values of concepts for comparison
        VocabularyConcept vc8 = (VocabularyConcept) CollectionUtils.find(concepts, new VocabularyConceptEvaluateOnIdPredicate(8));
        vc8.setDefinition("csv_test_concept_def_1_updated");

        List<List<DataElement>> dataElements = vc8.getElementAttributes();
        List<DataElement> elems =
                VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        DataElement element =
                (DataElement) CollectionUtils.find(elems, new DataElementEvaluateOnAttributeValuePredicate(
                        "bg2_csv_test_concept_1"));
        element.setAttributeValue("bg2_csv_test_concept_1_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "en", dataElements);
        element =
                (DataElement) CollectionUtils.find(elems,
                        new DataElementEvaluateOnAttributeValuePredicate("en_csv_test_concept_1"));
        element.setAttributeValue("en_csv_test_concept_1_updated");

        VocabularyConcept vc10 =
                (VocabularyConcept) CollectionUtils.find(concepts, new VocabularyConceptEvaluateOnIdPredicate(10));
        vc10.setLabel("csv_test_concept_label_3_updated");

        dataElements = vc10.getElementAttributes();
        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", "bg", dataElements);
        element =
                (DataElement) CollectionUtils.find(elems,
                        new DataElementEvaluateOnAttributeValuePredicate("bg_csv_test_concept_3"));
        element.setAttributeValue("bg_csv_test_concept_3_updated");

        elems = VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang("skos:definition", "pl", dataElements);
        element =
                (DataElement) CollectionUtils.find(elems,
                        new DataElementEvaluateOnAttributeValuePredicate("pl_csv_test_concept_3"));
        element.setAttributeValue("pl_csv_test_concept_3_updated");

        // get updated values of concepts with attributes
        List<VocabularyConcept> updatedConcepts =
                vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                        vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);

        // compare manually updated objects with queried ones (after import operation)

        assertEquals("Expected equal list size", concepts.size(), updatedConcepts.size());
        for (int i = 0; i < concepts.size(); i++) {
            assertEquals("Expected equals concepts", concepts.get(i), updatedConcepts.get(i));
        }
    }// end of test step testIfConceptsAndElementsUpdated

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
