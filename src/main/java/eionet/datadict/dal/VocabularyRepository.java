package eionet.datadict.dal;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface VocabularyRepository {

    boolean exists(Integer vocabularySetId, String vocabularyIdentifier);
    
    boolean exists(String vocabularySetIdentifier, String vocabularyIdentifier);
    
}
