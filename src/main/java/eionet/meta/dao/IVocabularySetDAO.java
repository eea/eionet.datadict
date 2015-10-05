/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.dao;

import eionet.meta.dao.domain.VocabularySet;

/**
 * @author Lena KARGIOTI eka@eworx.gr
 */
public interface IVocabularySetDAO {
    
    VocabularySet get( int vocabularySetID );
    
}
