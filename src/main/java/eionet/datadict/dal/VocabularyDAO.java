package eionet.datadict.dal;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 *
 * @author Aliki Kopaneli
 */
public interface VocabularyDAO {
    
    /**
     * Fetches the vocabulary corresponding to the specified id. 
     * VocabularyFolder object holds minimum information for its presentation to
     * the user
     * 
     * @param id
     * @return
     * @throws ResourceNotFoundException 
     */
    VocabularyFolder getPlainVocabularyById(int id) throws ResourceNotFoundException;
}
