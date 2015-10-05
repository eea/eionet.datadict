/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyRelationship;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
@SpringApplicationContext("spring-context.xml")
public class VocabularyRelationshipServiceTest  extends UnitilsJUnit4 {
    
    @SpringBeanByType
    private VocabularyRelationshipService service;
    
    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-vocabulary-relationships.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-vocabulary-relationships.xml");
    }
    
    @Test
    public void getVocabularyRelationshipsVoc1(){
        List<VocabularyRelationship> rels = service.getVocabularyRelationships(1);
        
        Assert.assertThat("There are 3 relationships between Vocabulary ID:1 and other", rels.size(), CoreMatchers.is(3) );
        
        Assert.assertThat("1st triple: Vocabulary 1", rels.get(0).getVocabulary().getId(), CoreMatchers.is(1));
        Assert.assertThat("1st triple: via data element with ID 6 is related to", rels.get(0).getRelationship().getId(), CoreMatchers.is(6));
        Assert.assertThat("1st triple: To Vocabulary 2", rels.get(0).getRelatedVocabulary().getId(), CoreMatchers.is(2));
        
        Assert.assertThat("1st triple: Vocabulary 1", rels.get(1).getVocabulary().getId(), CoreMatchers.is(1));
        Assert.assertThat("1st triple: via data element with ID 5 is related to", rels.get(1).getRelationship().getId(), CoreMatchers.is(5));
        Assert.assertThat("1st triple: To Vocabulary 3", rels.get(1).getRelatedVocabulary().getId(), CoreMatchers.is(3));
        
        Assert.assertThat("2nd triple: Vocabulary 1", rels.get(2).getVocabulary().getId(), CoreMatchers.is(1));
        Assert.assertThat("2nd triple: via data element with ID 5 is related to", rels.get(2).getRelationship().getId(), CoreMatchers.is(5));
        Assert.assertThat("2nd triple: To Vocabulary 2", rels.get(2).getRelatedVocabulary().getId(), CoreMatchers.is(2));
    }
    @Test
    public void getVocabularyRelationshipsVoc2(){
        List<VocabularyRelationship> rels = service.getVocabularyRelationships(2);
        
        Assert.assertThat("There is 1 relationship between Vocabulary ID:2 and other", rels.size(), CoreMatchers.is(1) );
        
        Assert.assertThat("1st triple: Vocabulary 2", rels.get(0).getVocabulary().getId(), CoreMatchers.is(2));
        Assert.assertThat("1st triple: via data element with ID 6 is related to", rels.get(0).getRelationship().getId(), CoreMatchers.is(6));
        Assert.assertThat("1st triple: To Vocabulary 3", rels.get(0).getRelatedVocabulary().getId(), CoreMatchers.is(3));

    }
    @Test
    public void getVocabularyRelationshipsVoc3(){
        List<VocabularyRelationship> rels = service.getVocabularyRelationships(23);
        
        Assert.assertThat("There is noe relationship between Vocabulary ID:3 and other", rels.size(), CoreMatchers.is(0) );

    }
    
    static List<Integer> concepts1            = new ArrayList<Integer>() {{ add(1); add(2);         }};
    static List<Integer> rel1DataElementSizes = new ArrayList<Integer>() {{ add(1); add(1);         }};
    static List<Integer> rel1DataElements     = new ArrayList<Integer>() {{ add(6); add(5);         }};
    static List<Integer> rel1Vocabularies     = new ArrayList<Integer>() {{ add(2); add(3);         }};
    static List<Integer> rel1ConceptSizes     = new ArrayList<Integer>() {{ add(1); add(2);         }};
    static List<Integer> rel1Concepts         = new ArrayList<Integer>() {{ add(5); add(8); add(10);}};
    @Test
    public void getRelatedVocabularyConcepts1(){
        getRelatedVocabularyConcepts(concepts1, 1, rel1DataElementSizes, rel1DataElements, rel1Vocabularies, rel1ConceptSizes, rel1Concepts);
    }
    
    static List<Integer> concepts2            = new ArrayList<Integer>() {{ add(5); add(6);         }};
    static List<Integer> rel2DataElementSizes = new ArrayList<Integer>() {{ add(1); add(0);         }};
    static List<Integer> rel2DataElements     = new ArrayList<Integer>() {{ add(6); }};
    static List<Integer> rel2Vocabularies     = new ArrayList<Integer>() {{ add(3); }};
    static List<Integer> rel2ConceptSizes     = new ArrayList<Integer>() {{ add(1); }};
    static List<Integer> rel2Concepts         = new ArrayList<Integer>() {{ add(9); }};
    @Test
    public void getRelatedVocabularyConcepts2(){
        getRelatedVocabularyConcepts(concepts2, 2, rel2DataElementSizes, rel2DataElements, rel2Vocabularies, rel2ConceptSizes, rel2Concepts);
    }
    
    private void getRelatedVocabularyConcepts(List<Integer> conceptIds, int vocId, List<Integer> relDataElementSizes, List<Integer> relDataElements, List<Integer> relVocabularies, List<Integer> relConceptSizes, List<Integer> relConcepts ){
        
        int idx = 0; int vIdx = 0; int cIdx = 0;
        
        for ( int conceptId: conceptIds ){
            
            Map<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> concepts = service.getRelatedVocabularyConcepts(conceptId, service.getVocabularyRelationships(vocId) );
        
            Assert.assertThat("There are "+relDataElementSizes.get(idx)+" relationships between Vocabulary ID:"+vocId+" and other for Given concept "+conceptId, 
                    concepts.size(), CoreMatchers.is(relDataElementSizes.get(idx)) );

            for ( Map.Entry<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> relEntry : concepts.entrySet() ){

                Assert.assertThat("Relationship is achieved through data element with ID "+relDataElements.get(idx),
                        relEntry.getKey().getId(), CoreMatchers.is(relDataElements.get(idx)));

                for ( Map.Entry<VocabularyFolder, List<VocabularyConcept>> entry : relEntry.getValue().entrySet() ){
                    Assert.assertThat("...towards Vocabulary with ID "+relVocabularies.get(vIdx),
                        entry.getKey().getId(), CoreMatchers.is(relVocabularies.get(vIdx)));

                    Assert.assertThat("...for "+relConceptSizes.get(vIdx)+" concepts ",
                        entry.getValue().size(), CoreMatchers.is(relConceptSizes.get(vIdx)));

                    for( VocabularyConcept concept : entry.getValue() ){
                        Assert.assertThat("...and concept with ID "+relConcepts.get(cIdx),
                            concept.getId(), CoreMatchers.is(relConcepts.get(cIdx)));
                        cIdx++;
                    }
                    vIdx++;
                }
                
            }
            idx++;
        }
        
    }
}
