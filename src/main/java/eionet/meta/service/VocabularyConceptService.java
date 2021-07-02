package eionet.meta.service;

import eionet.meta.dao.domain.VocabularyConcept;

import java.util.List;

public interface VocabularyConceptService {

    /**
     * lists vocabulary concepts
     * @param vocabularyFolderId
     * @return
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId);

}
