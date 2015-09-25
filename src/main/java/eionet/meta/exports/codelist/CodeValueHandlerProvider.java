/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataElement.DataElementValueType;
import eionet.meta.service.VocabularyRelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
@Component
public class CodeValueHandlerProvider {
    
    @Autowired
    private final IDataElementDAO elementDAO;
    @Autowired
    private final IVocabularyConceptDAO vocabularyConceptDAO;
    @Autowired
    private final VocabularyRelationshipService vocabularyRelationshipService;
            
    @Autowired
    public CodeValueHandlerProvider(IDataElementDAO elementDAO, IVocabularyConceptDAO vocabularyConceptDAO, VocabularyRelationshipService vocabularyRelationshipService ){
        this.elementDAO = elementDAO;
        this.vocabularyConceptDAO = vocabularyConceptDAO;
        this.vocabularyRelationshipService = vocabularyRelationshipService;
    }
    
    public CodeValueHandler get( DataElementValueType type ){
        switch ( type ){
            case VOCABULARY:{
                return new VocabularyCodeValueHandler(vocabularyRelationshipService, vocabularyConceptDAO, elementDAO);
            }
            case QUANTITIVE:
            case FIXED:
            default:{
                return new FixedValueHandler(elementDAO);
            }
        }        
    }
}
