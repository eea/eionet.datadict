package eionet.datadict.services.data;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.VocabularySet;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface VocabularyDataService {

    VocabularySet createVocabularySet(VocabularySet vocabularySet) 
            throws EmptyParameterException, DuplicateResourceException;
    
    VocabularyFolder createVocabulary(String vocabularySetIdentifier, VocabularyFolder vocabulary, DDUser creator)
            throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException;
    
}
