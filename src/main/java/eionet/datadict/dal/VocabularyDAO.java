package eionet.datadict.dal;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.List;

public interface VocabularyDAO {
    
    /**
     * Fetches the vocabulary corresponding to the specified id. 
     * VocabularyFolder object holds minimum information for its presentation to
     * the user
     * 
     * @param id
     * @return
     */
    VocabularyFolder getPlainVocabularyById(int id);
    
    /**
     * Fetches the vocabulary concepts of the vocabulary with the given id.
     * 
     * @param vocabularyId the id of the vocabulary whose concepts are to be fetched
     * @return a list of the {@link VocabularyConcept} objects of the vocabulary with the given id.
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyId);
    
    /**
     * Checks if a vocabulary concept with the given identifier of the vocabulary with the given id exists.
     * 
     * @param vocabularyId the id of the vocabulary
     * @param identifier the identifier of the vocabulary concept
     * @return true if the vocabulary concept exists, false otherwise
     */
    boolean existsVocabularyConcept(int vocabularyId, String identifier);
}
