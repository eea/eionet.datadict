package eionet.meta.service;

import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.VocabularyConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularyConceptServiceImpl implements VocabularyConceptService {

    private IVocabularyConceptDAO iVocabularyConceptDAO;

    @Autowired
    public VocabularyConceptServiceImpl(IVocabularyConceptDAO iVocabularyConceptDAO) {
        this.iVocabularyConceptDAO = iVocabularyConceptDAO;
    }

    @Override
    public List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId) {
        return iVocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
    }
}
