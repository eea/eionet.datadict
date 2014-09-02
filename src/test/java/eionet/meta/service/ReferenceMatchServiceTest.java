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

import java.util.List;

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
import eionet.meta.exports.VocabularyOutputHelper;

/**
 * JUnit integration test with Unitils for Reference Match Service.
 *
 * @author enver
 */
@SpringApplicationContext("spring-context.xml")
public class ReferenceMatchServiceTest extends UnitilsJUnit4 {
    /**
     * Constants for concepts.
     */
    private static final String CONCEPT_1_IDENTIFIER = "vocab_1_concept_1";
    private static final String CONCEPT_3_IDENTIFIER = "vocab_3_concept_1";
    private static final int CONCEPT_1_FOLDER_ID = 3;
    private static final int CONCEPT_3_FOLDER_ID = 5;
    private static final int VOCABULARY_2_ID = 4;
    private static final String CONCEPT_2_IN_VOCABULARY_2 = "vocab_2_concept_2";

    /**
     * Vocabulary reference match service.
     */
    @SpringBeanByType
    private IVocabularyReferenceMatchService vocabularyReferenceMatchService;
    /**
     * Vocabulary service.
     */
    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-emptydb.xml");
        DBUnitHelper.loadData("seed-referencematch.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-referencematch.xml");
    }

    /**
     * Call match references method with invalid input.
     *
     * @throws Exception
     */
    @Test
    public void callReferenceMatchWithInvalidJobArrays() throws Exception {
        this.vocabularyReferenceMatchService.matchReferences(null);
        this.vocabularyReferenceMatchService.matchReferences(new String[] {});
        this.vocabularyReferenceMatchService.matchReferences(new String[] {"aasdad", "",});
    } // end of test step callReferenceMatchWithInvalidJobArrays

    /**
     * In this test reference match method is called to update vocabulary concept elements.
     *
     * @throws Exception
     */
    @Test
    @Rollback
    public void testIfConceptAndElementsUpdatedAfterMatch() throws Exception {
        // get initial values of concepts, there are links in element values to be matched
        VocabularyConcept concept1 = this.vocabularyService.getVocabularyConcept(CONCEPT_1_FOLDER_ID, CONCEPT_1_IDENTIFIER, false);
        VocabularyConcept concept3 = this.vocabularyService.getVocabularyConcept(CONCEPT_3_FOLDER_ID, CONCEPT_3_IDENTIFIER, false);

        // also create a new concept to be matched
        VocabularyConcept newConceptInVocabulary2 = new VocabularyConcept();
        newConceptInVocabulary2.setIdentifier(CONCEPT_2_IN_VOCABULARY_2);
        newConceptInVocabulary2.setLabel(CONCEPT_2_IN_VOCABULARY_2);
        int lastInserted = this.vocabularyService.createVocabularyConcept(VOCABULARY_2_ID, newConceptInVocabulary2);
        newConceptInVocabulary2.setId(lastInserted);

        // call match references method of service
        this.vocabularyReferenceMatchService
                .matchReferences(new String[] {VocabularyReferenceMatchServiceImpl.MatchPotentialReferringElementValues.JOB_IDENTIFIER});

        // get updated values of concepts
        VocabularyConcept concept1Updated =
                this.vocabularyService.getVocabularyConcept(CONCEPT_1_FOLDER_ID, CONCEPT_1_IDENTIFIER, false);
        VocabularyConcept concept3Updated =
                this.vocabularyService.getVocabularyConcept(CONCEPT_3_FOLDER_ID, CONCEPT_3_IDENTIFIER, false);

        // update manually initial concepts for comparison
        List<List<DataElement>> dataElements = concept1.getElementAttributes();
        List<DataElement> elems;
        DataElement element;
        elems = VocabularyOutputHelper.getDataElementValuesByName("skos:relatedMatch", dataElements);

        element =
                VocabularyImportServiceTestBase.findDataElemByAttrValue(elems,
                        "http://test.tripledev.ee/match_vocab_2/vocab_2_concept_1");
        element.setAttributeValue(null);
        element.setRelatedConceptId(4);
        element.setRelatedConceptIdentifier("vocab_2_concept_1");
        element.setRelatedConceptLabel("vocab_2_concept_1");
        element.setRelatedConceptVocabulary("match_vocab_2");
        element.setRelatedConceptBaseURI("http://test.tripledev.ee/match_vocab_2/");
        element.setRelatedVocabularyStatus("Released");
        element.setRelatedConceptVocSet("match_references");

        element =
                VocabularyImportServiceTestBase.findDataElemByAttrValue(elems, "http://test.eea.eu/matchvocab3/vocab_3_concept_1");
        element.setAttributeValue(null);
        element.setRelatedConceptId(5);
        element.setRelatedConceptIdentifier("vocab_3_concept_1");
        element.setRelatedConceptLabel("vocab_3_concept_1");
        element.setRelatedConceptVocabulary("match_vocab_3");
        element.setRelatedConceptBaseURI("http://test.eea.eu/matchvocab3/");
        element.setRelatedVocabularyStatus("Released");
        element.setRelatedConceptVocSet("match_references_2");

        element =
                VocabularyImportServiceTestBase.findDataElemByAttrValue(elems,
                        "http://test.tripledev.ee/match_vocab_2/vocab_2_concept_2");
        element.setAttributeValue(null);
        element.setRelatedConceptId(lastInserted);
        element.setRelatedConceptIdentifier("vocab_2_concept_2");
        element.setRelatedConceptLabel("vocab_2_concept_2");
        element.setRelatedConceptVocabulary("match_vocab_2");
        element.setRelatedConceptBaseURI("http://test.tripledev.ee/match_vocab_2/");
        element.setRelatedVocabularyStatus("Released");
        element.setRelatedConceptVocSet("match_references");

        // remove for comparison
        dataElements.remove(elems);
        // also remove from updated concept
        List<List<DataElement>> concept1UpdatedDataElements = concept1Updated.getElementAttributes();
        List<DataElement> concept1UpdatedElems =
                VocabularyOutputHelper.getDataElementValuesByName("skos:relatedMatch", concept1UpdatedDataElements);
        concept1UpdatedDataElements.remove(concept1UpdatedElems);

        // order changes after second query so compare skos:relatedMatch items first
        ReflectionAssert.assertReflectionEquals(elems, concept1UpdatedElems, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // compare manually updated objects with queried ones (after match operation)
        ReflectionAssert.assertReflectionEquals(concept1, concept1Updated, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

        // compare manually updated objects with queried ones (after match operation)
        ReflectionAssert.assertReflectionEquals(concept3, concept3Updated, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);

    } // end of test step testIfConceptAndElementsUpdatedAfterMatch
} // end of test class ReferenceMatchServiceTest
