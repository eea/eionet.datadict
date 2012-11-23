/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 * Vocabulary service.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class VocabularyServiceImpl implements IVocabularyService {

    /** Vocabulary folder DAO. */
    @Autowired
    private IVocabularyFolderDAO vocabularyFolderDAO;

    /** Vocabulary concept DAO. */
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getVocabularyFolders(String userName) throws ServiceException {
        try {
            return vocabularyFolderDAO.getVocabularyFolders(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary folders: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createVocabularyFolder(VocabularyFolder vocabularyFolder) throws ServiceException {
        try {
            return vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);
        } catch (Exception e) {
            throw new ServiceException("Failed to create vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyFolder getVocabularyFolder(String identifier, boolean workingCopy) throws ServiceException {
        try {
            return vocabularyFolderDAO.getVocabularyFolder(identifier, workingCopy);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId) throws ServiceException {
        try {
            return vocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary concepts: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException {
        try {
            return vocabularyConceptDAO.createVocabularyConcept(vocabularyFolderId, vocabularyConcept);
        } catch (Exception e) {
            throw new ServiceException("Failed to create vocabulary concept: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException {
        try {
            vocabularyConceptDAO.updateVocabularyConcept(vocabularyConcept);
        } catch (Exception e) {
            throw new ServiceException("Failed to update vocabulary concept: " + e.getMessage(), e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyFolder(VocabularyFolder vocabularyFolder) throws ServiceException {
        try {
            vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);
        } catch (Exception e) {
            throw new ServiceException("Failed to update vocabulary folder: " + e.getMessage(), e);
        }
    }

}
