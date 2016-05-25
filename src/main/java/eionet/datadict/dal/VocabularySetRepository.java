package eionet.datadict.dal;

import eionet.datadict.model.VocabularySet;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface VocabularySetRepository {

    boolean exists(String identifier);
    
    Integer resolve(String identifier);
    
    VocabularySet get(String identifier);
    
    void create(VocabularySet vocabularySet);
    
}
