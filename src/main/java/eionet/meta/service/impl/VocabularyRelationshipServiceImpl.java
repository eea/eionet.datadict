/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service.impl;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyRelationship;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.VocabularyRelationshipService;
import eionet.util.Triple;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lena KARGIOTI eka@eworx.gr
 */
@Service
public class VocabularyRelationshipServiceImpl implements VocabularyRelationshipService{

    private final IVocabularyFolderDAO vocabularyFolderDAO;
    
    private final IVocabularyConceptDAO vocabularyConceptDAO;
    
    private final IDataElementDAO dataElementDao;
    
    @Autowired
    public VocabularyRelationshipServiceImpl(IDataElementDAO dataElementDao, IVocabularyConceptDAO vocabularyConceptDAO, IVocabularyFolderDAO vocabularyFolderDAO) {
        this.dataElementDao = dataElementDao;
        this.vocabularyConceptDAO = vocabularyConceptDAO;
        this.vocabularyFolderDAO = vocabularyFolderDAO;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyRelationship> getVocabularyRelationships(int vocabularyID) {
        List<Triple<Integer,Integer,Integer>> triples = this.vocabularyFolderDAO.getVocabulariesRelation(vocabularyID);
        
        List<VocabularyRelationship> relationshipsInfo = new ArrayList<VocabularyRelationship>();
        
        for ( Triple triple : triples ){
            VocabularyRelationship vocabularyRelationship = new VocabularyRelationship();
            
            //A Vocabulary
            VocabularyFolder vocabulary = this.vocabularyFolderDAO.getVocabularyFolder( (Integer)triple.getLeft() );
            vocabularyRelationship.setVocabulary(vocabulary);
            
            //Related to
            DataElement relationship = this.dataElementDao.getDataElement( (Integer)triple.getCentral() );
            Map<String, List<String>> attrs = this.dataElementDao.getDataElementAttributeValues( relationship.getId() );
            relationship.setName( attrs.get("Name").get(0) );
            vocabularyRelationship.setRelationship(relationship);
            
            //Another vocabulary
            VocabularyFolder relatedVocabulary = this.vocabularyFolderDAO.getVocabularyFolder( (Integer)triple.getRight() );
            vocabularyRelationship.setRelatedVocabulary(relatedVocabulary);            
            
            relationshipsInfo.add(vocabularyRelationship);
        }
        
        return relationshipsInfo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> getRelatedVocabularyConcepts( int vocabularyConceptID, List<VocabularyRelationship> vocRelationships ){
        
        Map<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> result = new LinkedHashMap<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>>();
        
        for ( VocabularyRelationship vocRelationship : vocRelationships ){
            //Vocabulary Relationship is: Vocabulary - Relationship DataElement - Related Vocabulary
            DataElement relationship = vocRelationship.getRelationship();
            VocabularyFolder relatedVocabulary = vocRelationship.getRelatedVocabulary();
            
            List<Integer> relatedConceptIDs = this.vocabularyFolderDAO.getRelatedVocabularyConcepts( vocabularyConceptID, relationship.getId(), relatedVocabulary.getId() );
            if ( relatedConceptIDs.isEmpty() ){
                continue;
            }
            List<VocabularyConcept> relatedConcepts = new ArrayList<VocabularyConcept>();
            for ( Integer relatedConceptID : relatedConceptIDs ){
                relatedConcepts.add( this.vocabularyConceptDAO.getVocabularyConcept(relatedConceptID) ) ;
            }
            
            Map<VocabularyFolder, List<VocabularyConcept>> perVocabulary = new LinkedHashMap<VocabularyFolder, List<VocabularyConcept>>();
            perVocabulary.put(relatedVocabulary, relatedConcepts);
            
            result.put(relationship, perVocabulary);
        
        }
        
        return result;
    }
    
}
