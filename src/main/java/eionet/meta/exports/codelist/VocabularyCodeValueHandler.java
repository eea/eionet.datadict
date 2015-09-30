/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.CleanupServlet;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyRelationship;
import eionet.meta.service.VocabularyRelationshipService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class VocabularyCodeValueHandler extends CodeValueHandler {
    
    private static final Logger LOGGER = Logger.getLogger(VocabularyCodeValueHandler.class);
    
    private final VocabularyRelationshipService vocabularyRelationshipService;
    
    private final IVocabularyConceptDAO vocabularyConceptDAO;
    
    private List<String> relationshipNames;
    
    public VocabularyCodeValueHandler( VocabularyRelationshipService vocabularyRelationshipService, IVocabularyConceptDAO vocabularyConceptDAO, IDataElementDAO elementDAO ){
        super( elementDAO );
        this.vocabularyRelationshipService = vocabularyRelationshipService;
        this.vocabularyConceptDAO = vocabularyConceptDAO;
    }
    @Override
    List<CodeItem> getCodeItemList() {
        if ( this.element == null ){
            throw new UnsupportedOperationException("Data element is not set");
        }
        Integer vocabularyID = this.element.getVocabularyId();
        
        if ( vocabularyID == null ){
            LOGGER.info("Vocabulary Element identified by '"+this.element.getIdentifier()+"' is not bound to any vocabulary. Skipping...");
            return Collections.emptyList();
        }
        
        List<VocabularyRelationship> relationships = this.vocabularyRelationshipService.getVocabularyRelationships(vocabularyID);

        //Define extra headers
        this.addRelationshipNames( relationships );
        
        List<VocabularyConcept> concepts = this.vocabularyConceptDAO.getVocabularyConcepts( vocabularyID );

        List<CodeItem> items = new ArrayList<CodeItem>();
        
        for (VocabularyConcept concept : concepts) {
            if ( concept.getStatus().isValid() ){
                int conceptID = concept.getId();
                String code = concept.getIdentifier();
                String label = concept.getLabel();
                String definition = concept.getDefinition();
                String notation = concept.getNotation();
                
                CodeItem item = new CodeItem( code, label, definition, notation );
                
                //Set the related concepts
                Map<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> relatedConcepts = this.vocabularyRelationshipService.getRelatedVocabularyConcepts(conceptID, relationships);
                //Iterate Relationships
                for ( Map.Entry<DataElement, Map<VocabularyFolder, List<VocabularyConcept>>> relationshipEntry : relatedConcepts.entrySet() ){
                    
                    String relationshipAttribute = relationshipEntry.getKey().getName();
                    
                    //Iterate Vocabularies and their Concepts
                    for ( Map.Entry<VocabularyFolder, List<VocabularyConcept>> entry : relationshipEntry.getValue().entrySet() ){
                        
                        VocabularyFolder relVocabulary = entry.getKey();
                        String relVocSetName = relVocabulary.getFolderLabel();
                        String relVocName = relVocabulary.getLabel();
                        
                        List<CodeItem> relCodeItems = new ArrayList<CodeItem>();
                        //Iterate Vocabulary Concepts
                        for( VocabularyConcept relConcept : entry.getValue() ){
                            CodeItem relatedItem = new CodeItem( relConcept.getIdentifier(), relConcept.getLabel(), relConcept.getDefinition(), relConcept.getNotation() );
                            relCodeItems.add(relatedItem);
                        }
                        
                        RelationshipInfo info = new RelationshipInfo(relationshipAttribute, relVocName, relVocSetName, relCodeItems);
                        
                        //Augment the CodeItem with relationship information
                        item.addRelationship(info);
                    }   
                }
                items.add( item );
            }
        }
        return items;
    }
    /**
     * Add the name of the relationship Data Element to the list of extra headers
     * 
     * @param relationships 
     */
    private void addRelationshipNames( List<VocabularyRelationship> relationships ){
        for ( VocabularyRelationship relationship : relationships ){
            this.addRelationshipName( relationship.getRelationship().getName() );
        }
    }
    private void addRelationshipName( String relationshipName ){
        if ( relationshipName == null )
            return;
        if ( relationshipNames == null ){
            relationshipNames = new ArrayList<String>();
        }
        if ( relationshipNames.contains(relationshipName) )
            return;
        relationshipNames.add(relationshipName);
    }

    @Override
    public List<String> getRelationshipNames() {
        return relationshipNames;
    }
    
}
