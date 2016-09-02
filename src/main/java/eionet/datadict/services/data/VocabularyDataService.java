package eionet.datadict.services.data;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.VocabularySet;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.List;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface VocabularyDataService {

    VocabularySet createVocabularySet(VocabularySet vocabularySet) 
            throws EmptyParameterException, DuplicateResourceException;
    
    VocabularyFolder createVocabulary(String vocabularySetIdentifier, VocabularyFolder vocabulary, DDUser creator)
            throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException;

    /**
     * Fetches the vocabulary concepts of the vocabulary with the specified id whose status is a sub-status of the given status.
     * 
     * @param vocabularyId the id of the vocabulary whose concepts are to be fetched.
     * @param status the (super) status of the concepts to be fetched.
     * @return a {@link List} of the {@link VocabularyConcept} objects of the specified vocabulary.
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyId, StandardGenericStatus status);
    
    /**
     * Check if the vocabulary concept with the specified identifier corresponding to the vocabulary of the given id exists.
     * 
     * @param vocabularyId the id of the vocabulary.
     * @param identifier the identifier of the vocabulary concept.
     * @return true if the vocabulary concept exists, false otherwise.
     */
    boolean existsVocabularyConcept(int vocabularyId, String identifier);
}
