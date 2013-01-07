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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.util.Util;

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
    public int createVocabularyFolder(VocabularyFolder vocabularyFolder, String userName) throws ServiceException {
        try {
            if (StringUtils.isEmpty(vocabularyFolder.getContinuityId())) {
                vocabularyFolder.setContinuityId(Util.generateContinuityId(vocabularyFolder));
            }
            // Validate type
            if (vocabularyFolder.isSiteCodeType() && !vocabularyFolder.isNumericConceptIdentifiers()) {
                throw new IllegalArgumentException("Site code type vocabulary must have numeric concept identifiers");
            }

            vocabularyFolder.setUserModified(userName);
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
    public VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) throws ServiceException {
        try {
            return vocabularyConceptDAO.searchVocabularyConcepts(filter);
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
            VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolder.getId());
            vf.setIdentifier(vocabularyFolder.getIdentifier());
            vf.setLabel(vocabularyFolder.getLabel());
            vf.setRegStatus(vocabularyFolder.getRegStatus());
            vf.setNumericConceptIdentifiers(vocabularyFolder.isNumericConceptIdentifiers());
            vf.setBaseUri(vocabularyFolder.getBaseUri());
            vocabularyFolderDAO.updateVocabularyFolder(vf);
        } catch (Exception e) {
            throw new ServiceException("Failed to update vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyConcepts(List<Integer> ids) throws ServiceException {
        try {
            vocabularyConceptDAO.deleteVocabularyConcepts(ids);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete vocabulary concepts: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyFolders(List<Integer> ids) throws ServiceException {
        try {
            vocabularyFolderDAO.deleteVocabularyFolders(ids);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete vocabulary folders: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyFolder getVocabularyFolder(int vocabularyFolderId) throws ServiceException {
        try {
            return vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkOutVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Cannot check out a working copy!");
            }

            if (StringUtils.isNotBlank(vocabularyFolder.getWorkingUser())) {
                throw new ServiceException("Cannot check out an already checked-out schema set!");
            }

            // Update existing working user
            vocabularyFolder.setWorkingUser(userName);
            vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);

            // Make new copy of vocabulary folder
            vocabularyFolder.setCheckedOutCopyId(vocabularyFolderId);
            vocabularyFolder.setWorkingCopy(true);
            int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);

            // Copy the vocabulary concepts under new vocabulary folder
            List<VocabularyConcept> vocabularyConcepts = vocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
            for (VocabularyConcept vc : vocabularyConcepts) {
                vocabularyConceptDAO.createVocabularyConcept(newVocabularyFolderId, vc);
            }

            return newVocabularyFolderId;
        } catch (Exception e) {
            throw new ServiceException("Failed to check-out vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkInVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (!vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Vocabulary is not a working copy.");
            }

            if (!StringUtils.equals(userName, vocabularyFolder.getWorkingUser())) {
                throw new ServiceException("Check-in user is not the current working user.");
            }

            int originalVocabularyFolderId = vocabularyFolder.getCheckedOutCopyId();

            // Remove old vocabulary concepts
            vocabularyConceptDAO.deleteVocabularyConcepts(originalVocabularyFolderId);

            // Update original vocabulary folder
            vocabularyFolder.setCheckedOutCopyId(0);
            vocabularyFolder.setId(originalVocabularyFolderId);
            vocabularyFolder.setUserModified(userName);
            vocabularyFolder.setWorkingCopy(false);
            vocabularyFolder.setWorkingUser(null);
            vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);

            // Move new vocabulary concepts to folder
            vocabularyConceptDAO.moveVocabularyConcepts(vocabularyFolderId, originalVocabularyFolderId);

            // Delete checked out version
            vocabularyFolderDAO.deleteVocabularyFolders(Collections.singletonList(vocabularyFolderId));

            return originalVocabularyFolderId;
        } catch (Exception e) {
            throw new ServiceException("Failed to check-in vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName)
            throws ServiceException {
        try {
            VocabularyFolder originalVocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            vocabularyFolder.setContinuityId(originalVocabularyFolder.getContinuityId());
            vocabularyFolder.setUserModified(userName);
            int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);

            List<VocabularyConcept> concepts = vocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
            for (VocabularyConcept vc : concepts) {
                vocabularyConceptDAO.createVocabularyConcept(newVocabularyFolderId, vc);
            }

            return newVocabularyFolderId;
        } catch (Exception e) {
            throw new ServiceException("Failed to create vocabulary folder copy: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName)
            throws ServiceException {
        try {
            return vocabularyFolderDAO.getVocabularyFolderVersions(continuityId, vocabularyFolderId, userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary folder versions: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undoCheckOut(int vocabularyFolderId, String userName) throws ServiceException {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (!vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Vocabulary is not a working copy.");
            }

            if (!StringUtils.equals(userName, vocabularyFolder.getWorkingUser())) {
                throw new ServiceException("Check-in user is not the current working user.");
            }

            int originalVocabularyFolderId = vocabularyFolder.getCheckedOutCopyId();

            // Update original vocabulary folder
            VocabularyFolder originalVocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(originalVocabularyFolderId);
            originalVocabularyFolder.setCheckedOutCopyId(0);
            originalVocabularyFolder.setWorkingUser(null);
            vocabularyFolderDAO.updateVocabularyFolder(originalVocabularyFolder);

            // Delete checked out version
            vocabularyFolderDAO.deleteVocabularyFolders(Collections.singletonList(vocabularyFolderId));

        } catch (Exception e) {
            throw new ServiceException("Failed to undo checkout for vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) throws ServiceException {
        try {
            return vocabularyFolderDAO.getVocabularyWorkingCopy(checkedOutCopyId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get the checked out vocabulary folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUniqueFolderIdentifier(String identifier, int... excludedVocabularyFolderIds) throws ServiceException {
        try {
            return vocabularyFolderDAO.isUniqueFolderIdentifier(identifier, excludedVocabularyFolderIds);
        } catch (Exception e) {
            throw new ServiceException("Failed to check unique vocabulary identifier: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId)
            throws ServiceException {
        try {
            return vocabularyConceptDAO.isUniqueConceptIdentifier(identifier, vocabularyFolderId, vocabularyConceptId);
        } catch (Exception e) {
            throw new ServiceException("Failed to check unique concept identifier: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reserveFreeSiteCodes(int vocabularyFolderId, int amount, int startIdentifier) throws ServiceException {
        try {
            VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (!vf.isWorkingCopy()) {
                throw new IllegalStateException("Vocabulary folder must be checked out");
            }
            if (!vf.isSiteCodeType()) {
                throw new IllegalStateException("Vocabulary folder must be site code type");
            }

            vocabularyConceptDAO.insertEmptyConcepts(vocabularyFolderId, amount, startIdentifier);

        } catch (Exception e) {
            throw new ServiceException("Failed to reserve empty site codes: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextIdentifierValue(int vocabularyFolderId) throws ServiceException {
        try {
            return vocabularyConceptDAO.getNextIdentifierValue(vocabularyFolderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get next concept identifier: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier)
            throws ServiceException {
        try {
            return vocabularyConceptDAO.checkAvailableIdentifiers(vocabularyFolderId, amount, startingIdentifier);
        } catch (Exception e) {
            throw new ServiceException("Failed to check available identifiers: " + e.getMessage(), e);
        }
    }

}
