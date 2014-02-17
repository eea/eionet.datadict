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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.unitils.UnitilsJUnit4;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.util.VocabularyCSVOutputHelper;

/**
 * JUnit integration test with Unitils for CSV Vocabulary Import Service.
 *
 * @author
 */
// @DataSet(value = {"seed-vocabularycsv-import.xml"}) //TODO why does not this work!!!
//also spring does not commit when it is working in test context so we cant utilize Dbunit compare dataset!! :(
@SpringApplicationContext("spring-context.xml")
// @TransactionConfiguration(defaultRollback = false)
public class CSVVocabularyImportServiceTest extends UnitilsJUnit4 {

    private static final int TEST_VALID_VOCAB_FOLDER_ID = 4;
    private static final int[] VOCAB_CONCEPT_IDS = new int[]{8, 9, 10};

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

    @Test
    @Rollback
    //expected : seed-vocabularycsv-import-step-1-expected
    public void testIfConceptAndElementsUpdated() throws Exception {
        //initialize values:
        Map<Integer, VocabularyConcept> plainVocabularyConcepts = new HashMap<Integer,VocabularyConcept>();
        for (int vocabId : VOCAB_CONCEPT_IDS ){
            plainVocabularyConcepts.put(vocabId, vocabularyService.getVocabularyConcept(vocabId));
        }

        //get file and create a reader for file
        InputStream is = getClass().getClassLoader().getResourceAsStream("csv_import/csv_import_test_1.csv");
        byte[] firstThreeBytes = new byte[3];
        is.read(firstThreeBytes);

        if (!Arrays.equals(firstThreeBytes, VocabularyCSVOutputHelper.BOM_BYTE_ARRAY)) {
            is.close();
            is = getClass().getClassLoader().getResourceAsStream("csv_import/csv_import_test_1.csv");
        }
        InputStreamReader reader = new InputStreamReader(is);

        //get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCAB_FOLDER_ID);

        //import CSV into database
        vocabularyImportService.importCsvIntoVocabulary(reader, vocabularyFolder, false, false);

        Map<Integer, VocabularyConcept> updatedPlainVocabularyConcepts = new HashMap<Integer, VocabularyConcept>();
        for (int vocabId : VOCAB_CONCEPT_IDS ){
            updatedPlainVocabularyConcepts.put(vocabId, vocabularyService.getVocabularyConcept(vocabId));
        }

        //do updates of objects here
        VocabularyConcept updated = plainVocabularyConcepts.get(8);
        updated.setLabel("csv_test_concept_label_1_updated");

        ReflectionAssert.assertLenientEquals(plainVocabularyConcepts, updatedPlainVocabularyConcepts);


    }// end of test step testStep1
}// end of test case CSVVocabularyImportServiceTest
