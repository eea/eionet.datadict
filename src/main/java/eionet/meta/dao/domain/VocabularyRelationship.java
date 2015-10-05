/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.dao.domain;

/**
 * This class holds information about the relationship between two vocabularies.
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class VocabularyRelationship {
   //a vocabulary
   private VocabularyFolder vocabulary;
   //another vocabulary
   private VocabularyFolder relatedVocabulary;
   //a data element 
   private DataElement relationship;

    public VocabularyFolder getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(VocabularyFolder vocabulary) {
        this.vocabulary = vocabulary;
    }

    public VocabularyFolder getRelatedVocabulary() {
        return relatedVocabulary;
    }

    public void setRelatedVocabulary(VocabularyFolder relatedVocabulary) {
        this.relatedVocabulary = relatedVocabulary;
    }

    public DataElement getRelationship() {
        return relationship;
    }

    public void setRelationship(DataElement relationship) {
        this.relationship = relationship;
    }

   
   
}
