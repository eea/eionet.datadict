package eionet.datadict.dal;

import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 *
 * @author exorx-alk
 */
public interface VocabularyDAO {
    
    VocabularyFolder getPlainVocabularyById(int id) throws ResourceNotFoundException;
}
