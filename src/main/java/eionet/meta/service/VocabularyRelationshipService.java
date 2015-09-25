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
import java.util.List;
import java.util.Map;

/**
 * Service for managing anything that has to do with the relationships between Vocabularies.
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
public interface VocabularyRelationshipService {
    
    /**
     * Returns the relationship information for the vocabulary identified by the given ID
     * @param vocabularyID
     * @return a list of Vocabulary Relationship Info
     */
    List<VocabularyRelationship> getVocabularyRelationships( int vocabularyID );
    
    /**
     * Returns this complex structure which describes all related concepts organized by Vocabulary and Relationship
     * 
     * @param vocabularyConceptID
     * @param vocRelationships
     * @return 
     */
    Map<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> getRelatedVocabularyConcepts( int vocabularyConceptID, List<VocabularyRelationship> vocRelationships );
}
