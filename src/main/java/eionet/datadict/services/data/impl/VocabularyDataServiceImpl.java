package eionet.datadict.services.data.impl;

import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.dal.VocabularySetDao;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.VocabularySet;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class VocabularyDataServiceImpl implements VocabularyDataService {

    private final VocabularySetDao vocabularySetDao;
    private final VocabularyDao vocabularyDao;
    private final IVocabularyService legacyVocabularyService;

    @Autowired
    public VocabularyDataServiceImpl(VocabularySetDao vocabularySetDao, VocabularyDao vocabularyDao, IVocabularyService legacyVocabularyService) {
        this.vocabularySetDao = vocabularySetDao;
        this.vocabularyDao = vocabularyDao;
        this.legacyVocabularyService = legacyVocabularyService;
    }

    @Override
    @Transactional
    public VocabularySet createVocabularySet(VocabularySet vocabularySet)
            throws EmptyParameterException, DuplicateResourceException {
        if (StringUtils.isBlank(vocabularySet.getIdentifier())) {
            throw new EmptyParameterException("identifier");
        }

        if (StringUtils.isBlank(vocabularySet.getLabel())) {
            throw new EmptyParameterException("label");
        }

        if (this.vocabularySetDao.exists(vocabularySet.getIdentifier())) {
            String msg = String.format("Vocabulary set %s already exists.", vocabularySet.getIdentifier());
            throw new DuplicateResourceException(msg);
        }

        this.vocabularySetDao.create(vocabularySet);

        return this.vocabularySetDao.get(vocabularySet.getIdentifier());
    }

    @Override
    @Transactional
    public VocabularyFolder createVocabulary(String vocabularySetIdentifier, VocabularyFolder vocabulary, DDUser creator)
            throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException {
        if (StringUtils.isBlank(vocabularySetIdentifier)) {
            throw new EmptyParameterException("vocabularySetIdentifier");
        }

        if (StringUtils.isBlank(vocabulary.getIdentifier())) {
            throw new EmptyParameterException("vocabularyIdentifier");
        }

        if (StringUtils.isBlank(vocabulary.getLabel())) {
            throw new EmptyParameterException("vocabularyLabel");
        }

        VocabularySet existingVocabularySet = this.vocabularySetDao.get(vocabularySetIdentifier);
        
        if (existingVocabularySet == null) {
            String msg = String.format("Vocabulary set %s does not exist.", vocabularySetIdentifier);
            throw new ResourceNotFoundException(msg);
        }

        if (this.vocabularyDao.exists(existingVocabularySet.getId(), vocabulary.getIdentifier())) {
            String msg = String.format("Vocabulary %s already exists.", vocabulary.getIdentifier());
            throw new DuplicateResourceException(msg);
        }

        vocabulary.setFolderId(existingVocabularySet.getId());

        try {
            int vocabularyId = this.legacyVocabularyService.createVocabularyFolder(vocabulary, null, creator.getUserName());

            return this.legacyVocabularyService.getVocabularyFolder(vocabularyId);
        } catch (ServiceException ex) {
            throw new RuntimeException(ex);
        }
    }

}
