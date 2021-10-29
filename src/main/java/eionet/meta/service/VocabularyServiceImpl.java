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

import eionet.datadict.errors.ConceptWithoutNotationException;
import eionet.datadict.model.enums.Enumerations;
import eionet.meta.DElemAttribute;
import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.*;
import eionet.meta.dao.domain.*;
import eionet.meta.service.data.*;
import eionet.util.*;
import eionet.web.action.ErrorActionBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Vocabulary service.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
@DependsOn("contextAware")
public class VocabularyServiceImpl implements IVocabularyService {

    /**
     * Logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(VocabularyServiceImpl.class);

    /**
     * Vocabulary folder DAO.
     */
    @Autowired
    private IVocabularyFolderDAO vocabularyFolderDAO;

    /**
     * Vocabulary concept DAO.
     */
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDAO;

    /**
     * Site Code DAO.
     */
    @Autowired
    private ISiteCodeDAO siteCodeDAO;

    /**
     * Attribute DAO.
     */
    @Autowired
    private IAttributeDAO attributeDAO;

    /**
     * Folder DAO.
     */
    @Autowired
    private IFolderDAO folderDAO;

    /**
     * Data element DAO.
     */
    @Autowired
    private IDataElementDAO dataElementDAO;

    /**
     * Rdf namespace DAO.
     */
    @Autowired
    private IRdfNamespaceDAO rdfNamespaceDAO;

    private static final int BATCH_SIZE = 1000;

    /**
     * Skos narrower keyword.
     */
    public static final String SKOS_NARROWER = "skos:narrower";

    /**
     * Adms status keyword
     */
    public static final String ADMS_STATUS = "adms:status";

    /**
     * {@inheritDoc}
     */
    @Override
    public Folder getFolder(int folderId) throws ServiceException {
        try {
            return folderDAO.getFolder(folderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFolderEmpty(int folderId) throws ServiceException {
        try {
            return folderDAO.isFolderEmpty(folderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to check if folder is empty: " + e.getMessage(), e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFolder(int folderId) throws ServiceException {
        try {
            if (!folderDAO.isFolderEmpty(folderId)) {
                throw new IllegalStateException("Folder is not empty");
            }
            folderDAO.deleteFolder(folderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFolder(Folder folder) throws ServiceException {
        try {
            Folder f = folderDAO.getFolder(folder.getId());
            f.setIdentifier(folder.getIdentifier());
            f.setLabel(folder.getLabel());
            folderDAO.updateFolder(f);
        } catch (Exception e) {
            throw new ServiceException("Failed to update folder: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Folder> getFolders(String userName, int... expandedFolders) throws ServiceException {
        try {
            List<Folder> result = folderDAO.getFolders();
            if (expandedFolders != null && expandedFolders.length > 0) {
                for (Folder f : result) {
                    for (int expandedId : expandedFolders) {
                        if (f.getId() == expandedId) {
                            f.setExpanded(true);
                            f.setItems(vocabularyFolderDAO.getVocabularyFolders(expandedId, userName));
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to get folders: " + e.getMessage(), e);
        }
    }

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
    @Transactional(rollbackFor = ServiceException.class)
    public int createVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder, String userName)
            throws ServiceException {
        try {
            if (StringUtils.isEmpty(vocabularyFolder.getContinuityId())) {
                vocabularyFolder.setContinuityId(Util.generateContinuityId(vocabularyFolder));
            }
            // Validate type
            if (vocabularyFolder.isSiteCodeType() && !vocabularyFolder.isNumericConceptIdentifiers()) {
                throw new IllegalArgumentException("Site code type vocabulary must have numeric concept identifiers");
            }

            if (vocabularyFolder.isSiteCodeType() && siteCodeDAO.siteCodeFolderExists()) {
                throw new IllegalStateException("Vocabulary folder with type 'SITE_CODE' already exists");
            }

            if (newFolder != null) {
                int newFolderId = folderDAO.createFolder(newFolder);
                vocabularyFolder.setFolderId(newFolderId);
                vocabularyFolder.setFolderName(newFolder.getIdentifier());
            } else {
                Folder folder = folderDAO.getFolder(vocabularyFolder.getFolderId());
                vocabularyFolder.setFolderName(folder.getIdentifier());
                vocabularyFolder.setType(VocabularyType.COMMON);
            }

            String baseUri = vocabularyFolder.getBaseUri();
            if (StringUtils.isBlank(baseUri)) {
                baseUri = VocabularyFolder.getBaseUri(vocabularyFolder);
            }
            vocabularyFolder.setBaseUri(baseUri);

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
    public VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) throws ServiceException {
        VocabularyFolder result = vocabularyFolderDAO.getVocabularyFolder(folderName, identifier, workingCopy);
        if (result == null) {
            ServiceException se =
                    new ServiceException("Vocabulary set \"" + folderName + "\" or vocabulary identifier \"" + identifier
                            + "\" not found!");
            se.setErrorParameter(ErrorActionBean.ERROR_TYPE_KEY, ErrorActionBean.ErrorType.NOT_FOUND_404);
            throw se;
        }
        // Load attributes
        List<List<SimpleAttribute>> attributes = attributeDAO.getVocabularyFolderAttributes(result.getId(), true);
        result.setAttributes(attributes);

        return result;
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
    @Transactional
    public int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException {
        return createVocabularyConceptNonTransactional(vocabularyFolderId, vocabularyConcept);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createVocabularyConceptNonTransactional(int vocabularyFolderId, VocabularyConcept vocabularyConcept)
            throws ServiceException {
        try {
            VocabularyFolder vocFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
            if (vocFolder != null && vocFolder.isNotationsEqualIdentifiers()) {
                vocabularyConcept.setNotation(vocabularyConcept.getIdentifier());
            }
            if (vocabularyConcept.getStatus() == null) {
                vocabularyConcept.setStatus(StandardGenericStatus.VALID);
                vocabularyConcept.setStatusModified(new java.sql.Date(System.currentTimeMillis()));
            }
            return vocabularyConceptDAO.createVocabularyConcept(vocabularyFolderId, vocabularyConcept);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException("Failed to create vocabulary concept: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void updateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException {
        this.updateVocabularyConceptStatusModifiedIfRequired(vocabularyConcept);
        updateVocabularyConceptNonTransactional(vocabularyConcept, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConceptNonTransactional(VocabularyConcept vocabularyConcept, boolean handleInverse)
            throws ServiceException {
        try {
            quickUpdateVocabularyConcept(vocabularyConcept);
            // updateVocabularyConceptAttributes(vocabularyConcept);
            updateVocabularyConceptDataElementValues(vocabularyConcept, handleInverse);
        } catch (Exception e) {
            throw new ServiceException("Failed to update vocabulary concept: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConceptNonTransactional(VocabularyConcept vocabularyConcept) throws ServiceException {
        updateVocabularyConceptNonTransactional(vocabularyConcept, false);
    }

    /**
     * updates bound element values included related bound elements.
     *
     * @param vocabularyConcept concept
     * @param handleInverse     if to handle inverse automatically
     * @throws ServiceException if update of attributes fails
     */
    private void updateVocabularyConceptDataElementValues(VocabularyConcept vocabularyConcept, boolean handleInverse)
            throws ServiceException {
        List<DataElement> dataElementValues = new ArrayList<DataElement>();
        if (vocabularyConcept.getElementAttributes() != null) {
            for (List<DataElement> values : vocabularyConcept.getElementAttributes()) {
                if (values != null) {
                    for (DataElement value : values) {
                        // @formatter:off
                        if (value != null && (StringUtils.isNotEmpty(value.getAttributeValue())
                                || (value.getRelatedConceptId() != null && value.getRelatedConceptId() != 0))) {

                            //The following code checks if a vocabulary concept url is stored in the ELEMENT_VALUE column and if it exists in the dd db stores it into RELATED_CONCEPT_ID instead
                            // Refs #100490
                            if (StringUtils.isNotEmpty(value.getAttributeValue()) && value.getAttributeValue().startsWith(Props.getProperty(Props.DD_URL) + "/")) {
                                String attrValueWithoutDomain = value.getAttributeValue().replace(Props.getProperty(Props.DD_URL) + "/", "");
                                String[] splittedValue = attrValueWithoutDomain.split("/");
                                if (splittedValue.length == 4 && splittedValue[0].equals("vocabularyconcept")) {
                                    String vocabularySetIdentifier = splittedValue[1];
                                    String vocabularyIdentifier = splittedValue[2];
                                    String vocabularyConceptIdentifier = splittedValue[3];

                                    VocabularyConcept vc = vocabularyConceptDAO.getVocabularyConceptByIdentifiers(vocabularySetIdentifier, vocabularyIdentifier, vocabularyConceptIdentifier);
                                    if (vc != null) {
                                        value.setRelatedConceptId(vc.getId());
                                        value.setAttributeValue(null);
                                    }
                                }
                            }

                            dataElementValues.add(value);
                        }
                        // @formatter:on
                    }
                }
            }
        }
        // fix relations in inverse elems
        //avoid this in importer
        if (handleInverse) {
            fixRelatedLocalRefElements(vocabularyConcept, dataElementValues);
        }
        dataElementDAO.deleteVocabularyConceptDataElementValues(vocabularyConcept.getId());
        if (dataElementValues.size() > 0) {
            dataElementDAO.insertVocabularyConceptDataElementValues(vocabularyConcept.getId(), dataElementValues);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void quickUpdateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException {
        try {
            VocabularyFolder vocFolder = vocabularyFolderDAO.getVocabularyFolderOfConcept(vocabularyConcept.getId());
            if (vocFolder != null && vocFolder.isNotationsEqualIdentifiers()) {
                vocabularyConcept.setNotation(vocabularyConcept.getIdentifier());
            }
            if (!checkIfConceptShouldBeAddedWhenBoundToElement(vocFolder.getId(), vocabularyConcept.getNotation())) {
                String errorMsg = "Concept without notation can not exist for this vocabulary because it is referenced by data elements";
                throw new ConceptWithoutNotationException(errorMsg);
            }
            vocabularyConceptDAO.updateVocabularyConcept(vocabularyConcept);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    protected void updateVocabularyConceptStatusModifiedIfRequired(VocabularyConcept vocabularyConcept) throws ServiceException {
        VocabularyConcept existingVocabularyConcept = this.getVocabularyConcept(vocabularyConcept.getId());
        if (existingVocabularyConcept.getStatus() != vocabularyConcept.getStatus()) {
            vocabularyConcept.setStatusModified(new Date());
        }
    }

    /**
     * As a last step when updating vocabulary concept, this method checks all the bound localref elements that represent relations
     * and makes sure that the concepts are related in both sides (A related with B -> B related with A). Also when relation gets
     * deleted from one side, then we make sure to deleted it also from the other side of the relation.
     *
     * @param vocabularyConcept Concept to be updated
     * @param dataElementValues bound data elements with values
     * @throws eionet.meta.service.ServiceException if fails
     */
    private void fixRelatedLocalRefElements(VocabularyConcept vocabularyConcept, List<DataElement> dataElementValues)
            throws ServiceException {
        try {

            // delete all element inversions existing in old copy as well:
            List<DataElement> originalElementValues =
                    dataElementDAO.getVocabularyDataElements(vocabularyConcept.getVocabularyId());

            for (DataElement elem : originalElementValues) {
                dataElementDAO.deleteInverseElemsOfConcept(vocabularyConcept.getId(), elem);
            }

            if (dataElementValues != null) {
                dataElementDAO.deleteReferringInverseElems(vocabularyConcept.getId(), dataElementValues);

                for (DataElement elem : dataElementValues) {

                    //for localref elements and reference elements which reside in the same vocabulary ,
                    // create inverse links immediately to show them in the working copy as well:
                    Integer relatedConceptId = elem.getRelatedConceptId();
                    if (elem.getRelatedConceptId() != null && elem.getRelatedConceptId() != 0) {

                        String elemType = dataElementDAO.getDataElementDataType(elem.getId());
                        if ("localref".equals(elemType)
                                || ("reference".equals(elemType)
                                && getVocabularyConcept(relatedConceptId).getVocabularyId() == vocabularyConcept.getVocabularyId())) {
                            dataElementDAO.createInverseElements(elem.getId(), vocabularyConcept.getId(), elem.getRelatedConceptId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException("Handling related element bindings failed " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder) throws ServiceException {
        try {
            VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolder.getId());

            vf.setIdentifier(vocabularyFolder.getIdentifier());
            vf.setLabel(vocabularyFolder.getLabel());
            vf.setRegStatus(vocabularyFolder.getRegStatus());
            vf.setNumericConceptIdentifiers(vocabularyFolder.isNumericConceptIdentifiers());
            vf.setNotationsEqualIdentifiers(vocabularyFolder.isNotationsEqualIdentifiers());
            vf.setBaseUri(vocabularyFolder.getBaseUri());

            if (newFolder != null) {
                int newFolderId = folderDAO.createFolder(newFolder);
                newFolder.setId(newFolderId);
                vf.setFolderId(newFolderId);
                vf.setFolderName(newFolder.getIdentifier());
            } else if (vf.getFolderId() != vocabularyFolder.getFolderId()) {
                vf.setFolderId(vocabularyFolder.getFolderId());
                Folder folder = folderDAO.getFolder(vocabularyFolder.getFolderId());
                vf.setFolderName(folder.getIdentifier());
            }

            // vf.setBaseUri(vocabularyFolder.getBaseUri());
            String baseUri = vocabularyFolder.getBaseUri();
            if (StringUtils.isBlank(baseUri)) {
                baseUri = VocabularyFolder.getBaseUri(vf);
            }
            vf.setBaseUri(baseUri);

            vocabularyFolderDAO.updateVocabularyFolder(vf);
            vocabularyFolder.setBaseUri(vf.getBaseUri());
            vocabularyFolder.setFolderName(vf.getFolderName());
            vocabularyFolder.setFolderId(vf.getFolderId());

            attributeDAO.updateSimpleAttributes(vocabularyFolder.getId(), DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                    vocabularyFolder.getAttributes());

            if (vf.isNotationsEqualIdentifiers()) {
                LOGGER.debug("Forcing all concept notations to be equal with identifiers!");
                vocabularyFolderDAO.forceNotationsToIdentifiers(vf.getId());
            }
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

    public void deleteVocabularyConcepts(int vocabularyFolderId) {
        LOGGER.info("Deleting all concepts from vocabulary folder: " + vocabularyFolderId);
        vocabularyConceptDAO.deleteVocabularyConcepts(vocabularyFolderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markConceptsInvalid(List<Integer> ids) throws ServiceException {
        try {
            vocabularyConceptDAO.markConceptsInvalid(ids);
        } catch (Exception e) {
            throw new ServiceException("Failed to mark the concepts obsolete: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markConceptsValid(List<Integer> ids) throws ServiceException {
        try {
            vocabularyConceptDAO.markConceptsValid(ids);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete remove obsolete status: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyFolders(List<Integer> ids, boolean keepRelatedValues) throws ServiceException {
        try {

            if (keepRelatedValues) {
                vocabularyFolderDAO.updateRelatedConceptValueToUri(ids);
            }
            vocabularyFolderDAO.deleteVocabularyFolders(ids, keepRelatedValues);
            attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());
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
            VocabularyFolder result = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            // Attributes
            List<List<SimpleAttribute>> attributes = attributeDAO.getVocabularyFolderAttributes(result.getId(), true);
            result.setAttributes(attributes);

            return result;

        } catch (Exception e) {
            String parameters = "id=" + vocabularyFolderId;
            throw new ServiceException("Failed to get vocabulary folder ( " + parameters + "): " + e.getMessage(), e);
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
            LOGGER.info(String.format("Checking out vocabulary #%d for user %s.", vocabularyFolderId, userName));
            StopWatch timer = new StopWatch();
            timer.start();
            VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Cannot check out a working copy!");
            }

            if (StringUtils.isNotBlank(vocabularyFolder.getWorkingUser())) {
                throw new ServiceException("Cannot check out an already checked-out vocabulary folder!");
            }

            // Update existing working user
            vocabularyFolder.setWorkingUser(userName);
            vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);

            // Make new copy of vocabulary folder
            vocabularyFolder.setCheckedOutCopyId(vocabularyFolderId);
            vocabularyFolder.setWorkingCopy(true);
            int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);
            LOGGER.info(String.format("Vocabulary #%d has new id: #%d.", vocabularyFolderId, newVocabularyFolderId));

            // Copy simple attributes.
            attributeDAO.copySimpleAttributes(vocabularyFolderId, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                    newVocabularyFolderId);

            // Copy the vocabulary concepts under new vocabulary folder
            vocabularyConceptDAO.copyVocabularyConcepts(vocabularyFolderId, newVocabularyFolderId);

            dataElementDAO.checkoutVocabularyConceptDataElementValues(newVocabularyFolderId);
            // dataElementDAO.updateRelatedConceptIds(newVocabularyFolderId);

            LOGGER.info(String.format("Vocabulary concepts have been copied for vocabulary #%d", newVocabularyFolderId));

            // Copy data element relations
            dataElementDAO.copyVocabularyDataElements(vocabularyFolderId, newVocabularyFolderId);
            LOGGER.info(String.format("Vocabulary data elements have been copied for vocabulary #%d", newVocabularyFolderId));
            timer.stop();
            LOGGER.info("Check-out lasted: " + timer.toString());
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
    public synchronized int checkInVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            LOGGER.info(String.format("Checking in vocabulary #%d for user %s.", vocabularyFolderId, userName));
            StopWatch timer = new StopWatch();
            timer.start();
            VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (!vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Vocabulary is not a working copy.");
            }

            if (!StringUtils.equals(userName, vocabularyFolder.getWorkingUser())) {
                throw new ServiceException("Check-in user is not the current working user.");
            }

            int originalVocabularyFolderId = vocabularyFolder.getCheckedOutCopyId();

            List<VocabularyConcept> concepts = getAllConceptsWithAttributes(originalVocabularyFolderId);

            Map<Integer, DataElement> conceptAttributes = vocabularyConceptDAO.getVocabularyConceptAttributes(originalVocabularyFolderId);
            if (conceptAttributes != null) {
                LOGGER.info(String.format("Concept attributes %s for vocabulary #%d have been retrieved.", conceptAttributes.toString(), originalVocabularyFolderId));
            } else {
                LOGGER.info(String.format("No concept attributes were found for vocabulary #%d.", originalVocabularyFolderId));
            }
            // delete relations for inverse attibute values
            deleteInverseElementsRelations(conceptAttributes.values(), concepts);

            // referenced attribute values in this vocabulary must get new id's
            vocabularyConceptDAO.updateReferringReferenceConcepts(originalVocabularyFolderId);
            LOGGER.info(String.format("Referring reference concepts for old vocabulary #%d has been updated.", originalVocabularyFolderId));

            // remove old vocabulary concepts
            vocabularyConceptDAO.deleteVocabularyConcepts(originalVocabularyFolderId);
            LOGGER.info(String.format("Vocabulary concepts for old vocabulary #%d have been deleted.", originalVocabularyFolderId));

            // remove old data element relations
            dataElementDAO.deleteVocabularyDataElements(originalVocabularyFolderId);
            LOGGER.info(String.format("Vocabulary data elements for old vocabulary #%d have been deleted.", originalVocabularyFolderId));

            // update ch3 element reference
            dataElementDAO.moveVocabularySources(originalVocabularyFolderId, vocabularyFolderId);
            LOGGER.info(String.format("Vocabulary sources have been moved from vocabulary #%d to vocabulary #%d", originalVocabularyFolderId, vocabularyFolderId));

            // update original vocabulary folder
            vocabularyFolder.setCheckedOutCopyId(0);
            vocabularyFolder.setId(originalVocabularyFolderId);
            vocabularyFolder.setUserModified(userName);
            vocabularyFolder.setDateModified(new Date());
            vocabularyFolder.setWorkingCopy(false);
            vocabularyFolder.setWorkingUser(null);

            vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);
            LOGGER.info(String.format("Vocabulary folder #%d was updated.", vocabularyFolder.getId()));

            /*TODO
                insert bound elements values ? sitecodes_CC_ISO2, sitecodes_DATE_CREATED
             */
            // move new vocabulary concepts to folder
            vocabularyConceptDAO.moveVocabularyConcepts(vocabularyFolderId, originalVocabularyFolderId);
            LOGGER.info(String.format("Vocabulary concepts were moved from vocabulary #%d to vocabulary #%d.", vocabularyFolderId, originalVocabularyFolderId));

            // move bound data elements to new vocabulary
            dataElementDAO.moveVocabularyDataElements(vocabularyFolderId, originalVocabularyFolderId);
            LOGGER.info(String.format("Vocabulary data elements were moved from vocabulary #%d to vocabulary #%d.", vocabularyFolderId, originalVocabularyFolderId));

            concepts = getAllConceptsWithAttributes(originalVocabularyFolderId);

            conceptAttributes = vocabularyConceptDAO.getVocabularyConceptAttributes(originalVocabularyFolderId);

            if (conceptAttributes != null) {
                LOGGER.info(String.format("Concept attributes %s for vocabulary #%d have been retrieved.", conceptAttributes.toString(), originalVocabularyFolderId));
            } else {
                LOGGER.info(String.format("No concept attributes were found for vocabulary #%d.", originalVocabularyFolderId));
            }

            // create relations for inverse attibute values
            createInverseElementsRelations(conceptAttributes.values(), concepts);

            // delete old attributes first and then change the parent ID of the new ones
            attributeDAO.deleteAttributes(Collections.singletonList(originalVocabularyFolderId),
                    DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());

            attributeDAO.replaceParentId(vocabularyFolderId, originalVocabularyFolderId,
                    DElemAttribute.ParentType.VOCABULARY_FOLDER);
            LOGGER.info(String.format("Parent id was replaced from vocabulary #%d to vocabulary #%d.", vocabularyFolderId, originalVocabularyFolderId));

            // delete checked out version
            vocabularyFolderDAO.deleteVocabularyFolders(Collections.singletonList(vocabularyFolderId), false);

            timer.stop();
            LOGGER.info("Check-in lasted: " + timer.toString());
            return originalVocabularyFolderId;
        } catch (Exception e) {
            throw new ServiceException("Failed to check-in vocabulary folder: " + e.getMessage(), e);
        }
    }

    private List<Integer> getReferenceDataElementIds(Collection<DataElement> dataElements) {
        List<Integer> referenceDataElementIds = new ArrayList<Integer>();
        if (!dataElements.isEmpty()) {
            for (Iterator<DataElement> it = dataElements.iterator(); it.hasNext(); ) {
                DataElement element = it.next();
                if (element.getDatatype().equals("reference")) {
                    referenceDataElementIds.add(element.getId());
                }
            }
        }
        return referenceDataElementIds;
    }

    private void deleteInverseElementsRelations(Collection<DataElement> dataElements, List<VocabularyConcept> concepts) {
        List<Integer> referenceDataElementIds = getReferenceDataElementIds(dataElements);

        if (!referenceDataElementIds.isEmpty()) {
            Map<Integer, Collection<Integer>> inverseElementIdsToConceptIds = new HashMap<Integer, Collection<Integer>>();
            Map<Integer, Integer> elementIdToInverseElementId = new HashMap<Integer, Integer>();

            // find inverse data elements
            for (Iterator<Integer> it = referenceDataElementIds.iterator(); it.hasNext(); ) {
                int elementId = it.next();
                int inverseElementId = dataElementDAO.getInverseElementID(elementId);
                if (inverseElementId > 0) {
                    elementIdToInverseElementId.put(elementId, inverseElementId);
                    inverseElementIdsToConceptIds.put(inverseElementId, new ArrayList<Integer>());
                }
            }

            if (!inverseElementIdsToConceptIds.isEmpty()) {
                for (VocabularyConcept concept : concepts) {
                    List<List<DataElement>> elementAttributes = concept.getElementAttributes();
                    for (List<DataElement> elementAttribute : elementAttributes) {
                        int elementId = elementAttribute.get(0).getId();
                        if (elementIdToInverseElementId.containsKey(elementId)) {
                            int inverseElementId = elementIdToInverseElementId.get(elementId);
                            inverseElementIdsToConceptIds.get(inverseElementId).add(concept.getId());
                        }
                    }
                }

                // delete relations for inverse data elements
                for (Integer inverseElementId : inverseElementIdsToConceptIds.keySet()) {
                    dataElementDAO.deleteRelatedConcepts(inverseElementId, inverseElementIdsToConceptIds.get(inverseElementId));
                }
            }
        }
    }

    private void createInverseElementsRelations(Collection<DataElement> dataElements, List<VocabularyConcept> concepts) {
        List<Integer> referenceDataElementIds = getReferenceDataElementIds(dataElements);

        if (!referenceDataElementIds.isEmpty()) {
            Map<Integer, Integer> elementIdToInverseElementId = new HashMap<Integer, Integer>();

            // find inverse data elements
            for (Iterator<Integer> it = referenceDataElementIds.iterator(); it.hasNext(); ) {
                Integer elementId = it.next();
                int inverseElementId = dataElementDAO.getInverseElementID(elementId);
                if (inverseElementId > 0) {
                    elementIdToInverseElementId.put(elementId, inverseElementId);
                }
            }

            if (!elementIdToInverseElementId.isEmpty()) {
                List<Triple<Integer, Integer, Integer>> relatedReferenceElements = new ArrayList<Triple<Integer, Integer, Integer>>();
                Map<Integer, Set<Integer>> inverseElementIdsToRelatedConceptIds = new HashMap<Integer, Set<Integer>>();

                // create concepts relations
                for (VocabularyConcept concept : concepts) {
                    List<List<DataElement>> elems = concept.getElementAttributes();
                    for (List<DataElement> elemMeta : elems) {
                        Integer elementId = elemMeta.get(0).getId();
                        if (elementIdToInverseElementId.keySet().contains(elementId)) {
                            Integer inverseElementId = elementIdToInverseElementId.get(elementId);
                            for (DataElement elem : elemMeta) {
                                if (elem.getRelatedConceptId() != null && elem.getRelatedConceptId() > 0) {
                                    Triple<Integer, Integer, Integer> triple = new Triple<Integer, Integer, Integer>(elem.getRelatedConceptId(), inverseElementId, concept.getId());
                                    relatedReferenceElements.add(triple);

                                    if (inverseElementIdsToRelatedConceptIds.get(inverseElementId) == null) {
                                        Set<Integer> relatedConceptIds = new HashSet<Integer>();
                                        relatedConceptIds.add(elem.getRelatedConceptId());
                                        inverseElementIdsToRelatedConceptIds.put(inverseElementId, relatedConceptIds);
                                    } else {
                                        inverseElementIdsToRelatedConceptIds.get(inverseElementId).add(elem.getRelatedConceptId());
                                    }
                                }
                            }
                        }
                    }
                }

                List<Pair<Integer, Integer>> vocabularyIdToDataElementId = new ArrayList<Pair<Integer, Integer>>();
                List<Triple<Integer, Integer, Integer>> inverseRelatedReferenceElements = new ArrayList<Triple<Integer, Integer, Integer>>();

                for (Iterator<Integer> it = inverseElementIdsToRelatedConceptIds.keySet().iterator(); it.hasNext(); ) {
                    final Integer inverseElementId = it.next();
                    Set<Integer> relatedConceptIds = inverseElementIdsToRelatedConceptIds.get(inverseElementId);

                    // get the distinct vocabulary ids of the related concepts
                    Collection<Integer> relatedConceptsVocabularyIds = vocabularyFolderDAO.getVocabularyIds(relatedConceptIds);
                    for (Iterator<Integer> it1 = relatedConceptsVocabularyIds.iterator(); it1.hasNext(); ) {
                        vocabularyIdToDataElementId.add(new Pair<Integer, Integer>(it1.next(), inverseElementId));
                    }

                    // get the checked out vocabulary ids from the related concepts vocabularies
                    Collection<Integer> workingCopyVocabularyIds = vocabularyFolderDAO.getWorkingCopyIds(relatedConceptsVocabularyIds);
                    if (!workingCopyVocabularyIds.isEmpty()) {
                        for (Iterator<Integer> it1 = workingCopyVocabularyIds.iterator(); it1.hasNext(); ) {
                            vocabularyIdToDataElementId.add(new Pair<Integer, Integer>(it1.next(), inverseElementId));
                        }

                        // get the checked out to original id mappings for the related concepts
                        final Map<Integer, Integer> checkedOutToOriginalMappings = vocabularyConceptDAO.getCheckedOutToOriginalMappings(relatedConceptIds);

                        for (Iterator<Integer> it1 = checkedOutToOriginalMappings.keySet().iterator(); it1.hasNext(); ) {
                            final Integer checkedOutConceptId = it1.next();

                            // create checked out concepts relations
                            for (Iterator<Triple<Integer, Integer, Integer>> it2 = relatedReferenceElements.iterator(); it2.hasNext(); ) {
                                Triple<Integer, Integer, Integer> triple = it2.next();
                                if (triple.getCentral().equals(inverseElementId) && triple.getLeft().equals(checkedOutToOriginalMappings.get(checkedOutConceptId))) {
                                    inverseRelatedReferenceElements.add(new Triple<Integer, Integer, Integer>(checkedOutConceptId, inverseElementId, triple.getRight()));
                                }
                            }
                        }
                    }
                }
                // add the checked out concepts relations to the original concepts list
                relatedReferenceElements.addAll(inverseRelatedReferenceElements);

                dataElementDAO.batchCreateVocabularyBoundElements(vocabularyIdToDataElementId, BATCH_SIZE);
                dataElementDAO.batchCreateInverseRelations(relatedReferenceElements, BATCH_SIZE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName,
                                          Folder newFolder) throws ServiceException {
        try {
            VocabularyFolder originalVocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (originalVocabularyFolder.isSiteCodeType()) {
                throw new IllegalArgumentException("Cannot make copy of vocabulary with type 'SITE_CODE'");
            }

            if (newFolder != null) {
                int newFolderId = folderDAO.createFolder(newFolder);
                vocabularyFolder.setFolderId(newFolderId);
                vocabularyFolder.setFolderName(newFolder.getIdentifier());
            } else if (originalVocabularyFolder.getFolderId() != vocabularyFolder.getFolderId()) {
                Folder folder = folderDAO.getFolder(vocabularyFolder.getFolderId());
                vocabularyFolder.setFolderName(folder.getIdentifier());
            } else {
                vocabularyFolder.setFolderName(originalVocabularyFolder.getFolderName());
            }

            // vf.setBaseUri(vocabularyFolder.getBaseUri());
            String baseUri = originalVocabularyFolder.getBaseUri();
            if (StringUtils.isBlank(baseUri)) {
                baseUri = VocabularyFolder.getBaseUri(vocabularyFolder);
            }
            vocabularyFolder.setBaseUri(baseUri);

            vocabularyFolder.setContinuityId(originalVocabularyFolder.getContinuityId());
            vocabularyFolder.setRegStatus(originalVocabularyFolder.getRegStatus());
            vocabularyFolder.setUserModified(userName);
            int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);

            // Copy simple attributes.
            attributeDAO.copySimpleAttributes(vocabularyFolderId, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                    newVocabularyFolderId);

            dataElementDAO.copyVocabularyDataElements(vocabularyFolderId, newVocabularyFolderId);

            List<VocabularyConcept> concepts = vocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
            for (VocabularyConcept vc : concepts) {
                vocabularyConceptDAO.createVocabularyConcept(newVocabularyFolderId, vc);
            }
            dataElementDAO.copyVocabularyConceptDataElementValues(vocabularyFolderId, newVocabularyFolderId);

            List<VocabularyConcept> newConcepts = vocabularyConceptDAO.getVocabularyConcepts(newVocabularyFolderId);
            injectDataElementValuesInConcepts(newConcepts, newVocabularyFolderId);
            fixRelatedReferenceElements(newVocabularyFolderId, newConcepts);

            return newVocabularyFolderId;
        } catch (Exception e) {
            throw new ServiceException("Failed to create vocabulary folder copy: " + e.getMessage(), e);
        }
    }

    private void injectDataElementValuesInConcepts(List<VocabularyConcept> concepts, int vocabularyFolderId) {
        if (!concepts.isEmpty()) {
            List<Integer> conceptIds = new ArrayList<Integer>();
            for (VocabularyConcept concept : concepts) {
                conceptIds.add(concept.getId());
            }
            Map<Integer, List<List<DataElement>>> conceptsDataElementValues = getVocabularyConceptsDataElementValues(
                    vocabularyFolderId, ArrayUtils.toPrimitive(conceptIds.toArray(new Integer[conceptIds.size()])), false);

            for (VocabularyConcept concept : concepts) {
                concept.setElementAttributes(conceptsDataElementValues.get(concept.getId()));
            }
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
    public int undoCheckOut(int vocabularyFolderId, String userName) throws ServiceException {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            LOGGER.info(String.format("Undoing check out for vocabulary #%d for user %s.", vocabularyFolderId, userName));
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
            LOGGER.info(String.format("Vocabulary %d was updated.", originalVocabularyFolder.getId()));

            // Delete checked out version
            vocabularyFolderDAO.deleteVocabularyFolders(Collections.singletonList(vocabularyFolderId), false);
            attributeDAO.deleteAttributes(Collections.singletonList(vocabularyFolderId),
                    DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());

            LOGGER.info(String.format("Undoing check out for vocabulary #%d by user %s was completed successfully.", vocabularyFolderId, userName));
            return originalVocabularyFolderId;
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
    public boolean isUniqueFolderIdentifier(String identifier, int excludedId) throws ServiceException {
        try {
            return folderDAO.isFolderUnique(identifier, excludedId);
        } catch (Exception e) {
            throw new ServiceException("Failed to check unique folder identifier: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds)
            throws ServiceException {
        try {
            return vocabularyFolderDAO.isUniqueVocabularyFolderIdentifier(folderId, identifier, excludedVocabularyFolderIds);
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
    @Transactional(rollbackFor = ServiceException.class)
    public void reserveFreeSiteCodes(int vocabularyFolderId, int amount, int startIdentifier, String userName)
            throws ServiceException {
        try {
            VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            if (vf.isWorkingCopy()) {
                throw new IllegalStateException("Vocabulary folder cannot be checked out");
            }
            if (!vf.isSiteCodeType()) {
                throw new IllegalStateException("Vocabulary folder must be site code type");
            }

            String definition = "Added by " + userName + " on " + Util.formatDateTime(new Date());
            String label = "<" + SiteCodeStatus.AVAILABLE.toString().toLowerCase() + ">";

            // Insert empty concepts
            vocabularyConceptDAO.insertEmptyConcepts(vocabularyFolderId, amount, startIdentifier, label, definition);

            // Get added concepts
            VocabularyConceptFilter filter = new VocabularyConceptFilter();
            filter.setVocabularyFolderId(vf.getId());
            filter.setDefinition(definition);
            filter.setLabel(label);
            filter.setUsePaging(false);

            VocabularyConceptResult newConceptsResult = vocabularyConceptDAO.searchVocabularyConcepts(filter);

            // Insert values for bound elements user and date created for Site code records
            siteCodeDAO.insertAvailableSiteCodes(newConceptsResult.getList(), userName);
            LOGGER.info(userName + " created " + amount + " new site codes.");

            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            Date allocationTime = c.getTime();
            vocabularyFolderDAO.updateDateAndUserModified(allocationTime, userName, vocabularyFolderId);
            LOGGER.info("Updated dateModified field for vocabulary " + vocabularyFolderId);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier, boolean emptyAttributes)
            throws ServiceException {
        try {
            VocabularyConcept result = vocabularyConceptDAO.getVocabularyConcept(vocabularyFolderId, conceptIdentifier);

            int conceptId = result.getId();
            Map<Integer, List<List<DataElement>>> vocabularyConceptsDataElementValues =
                    dataElementDAO.getVocabularyConceptsDataElementValues(vocabularyFolderId, new int[]{conceptId},
                            emptyAttributes);
            result.setElementAttributes(vocabularyConceptsDataElementValues.get(conceptId));
            return result;
        } catch (IncorrectResultSizeDataAccessException e) {
            ServiceException se = new ServiceException("Vocabulary concept \"" + conceptIdentifier + "\" not found!", e);
            se.setErrorParameter(ErrorActionBean.ERROR_TYPE_KEY, ErrorActionBean.ErrorType.NOT_FOUND_404);
            throw se;
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getVocabularyConceptIds(int vocabularyFolderId) {
        return vocabularyConceptDAO.getVocabularyConceptIds(vocabularyFolderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyConceptBoundElementFilter getVocabularyConceptBoundElementFilter(int dataElementId, List<Integer> vocabularyConceptIds) {
        return dataElementDAO.getVocabularyConceptBoundElementFilter(dataElementId, vocabularyConceptIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyConcept getVocabularyConcept(int vocabularyConceptId) throws ServiceException {
        try {
            return vocabularyConceptDAO.getVocabularyConcept(vocabularyConceptId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VocabularyConcept> getAllConceptsWithAttributes(int vocabularyFolderId) throws ServiceException {
        try {
            return vocabularyConceptDAO.getConceptsWithAttributeValues(vocabularyFolderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary concepts: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyConcept> getConceptsWithAttributes(int vocabularyFolderId, String conceptIdentifier, String label, boolean acceptedOnly) throws ServiceException {
        try {
            StandardGenericStatus conceptStatus = acceptedOnly ? StandardGenericStatus.ACCEPTED : null;

            return this.vocabularyConceptDAO.getConceptsWithAttributeValues(vocabularyFolderId, conceptStatus, conceptIdentifier, label);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pingCrToReharvestVocabulary(int vocabularyFolderId) throws ServiceException {

        String crPingUrl = Props.getProperty(PropsIF.CR_PING_URL);

        if (!Util.isEmpty(crPingUrl)) {

            VocabularyFolder vocabluary = getVocabularyFolder(vocabularyFolderId);
            StringBuilder rdfUrl = new StringBuilder(Props.getProperty(PropsIF.DD_URL));

            if (!Props.getProperty(PropsIF.DD_URL).endsWith("/")) {
                rdfUrl.append("/");
            }
            rdfUrl.append("vocabulary/");
            rdfUrl.append(vocabluary.getFolderName());
            rdfUrl.append("/");
            rdfUrl.append(vocabluary.getIdentifier());
            rdfUrl.append("/rdf");

            try {
                crPingUrl = String.format(crPingUrl, URLEncoder.encode(rdfUrl.toString(), "UTF-8"));

                if (Util.isURI(crPingUrl)) {
                    URL url = new URL(crPingUrl);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    int status = httpConn.getResponseCode();
                    if (status >= 400) {
                        LOGGER.error("Unable to ping CR (responseCode: " + status + ") for reharvesting vocabulary folder: "
                                + crPingUrl);
                    } else {
                        LOGGER.debug("Ping request (responseCode: " + status
                                + ") was sent to CR for reharvesting vocabulary folder: " + crPingUrl);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Unable to ping CR: " + crPingUrl);
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getWorkingCopies(String userName) throws ServiceException {
        try {
            return vocabularyFolderDAO.getWorkingCopies(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary folder working copies: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleAttribute> getVocabularyFolderAttributesMetadata() throws ServiceException {
        try {
            return attributeDAO.getAttributesMetadata(DElemAttribute.typeWeights.get("VCF"));
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabulary folder attribute metadata: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) throws ServiceException {
        try {
            return vocabularyFolderDAO.getReleasedVocabularyFolders(folderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get released vocabulary folders: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Folder getFolderByIdentifier(String folderIdentifier) throws ServiceException {
        try {
            return folderDAO.getFolderByIdentifier(folderIdentifier);
        } catch (Exception e) {
            throw new ServiceException("Failed to get folder by identifier: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException {
        try {
            dataElementDAO.addDataElement(vocabularyFolderId, dataElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to add data element: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException {
        try {
            dataElementDAO.removeDataElement(vocabularyFolderId, dataElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to remove data element: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataElement> getVocabularyDataElements(int vocabularyFolderId) throws ServiceException {
        try {
            return dataElementDAO.getVocabularyDataElements(vocabularyFolderId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data elements: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean vocabularyHasDataElementBinding(int vocabularyFolderId, int dataElementId) throws ServiceException {
        try {
            return dataElementDAO.vocabularyHasElemendBinding(vocabularyFolderId, dataElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to perform element binding existence check: " + e.getMessage(), e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyConcept> getConceptsWithElementValue(int dataElementId, int vocabularyId) throws ServiceException {
        try {
            return vocabularyConceptDAO.getConceptsWithValuedElement(dataElementId, vocabularyId);
        } catch (Exception e) {
            throw new ServiceException("Failed to perform bound element values existence check: " + e.getMessage(), e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfNamespace> getVocabularyNamespaces(List<VocabularyFolder> vocabularyFolders) throws ServiceException {
        List<RdfNamespace> nameSpaces = new ArrayList<RdfNamespace>();
        // String baseUri = Props.getRequiredProperty(PropsIF.RDF_DATAELEMENTS_BASE_URI);

        try {
            for (VocabularyFolder vocabulary : vocabularyFolders) {
                List<DataElement> elems = getVocabularyDataElements(vocabulary.getId());
                for (DataElement elem : elems) {
                    RdfNamespace ns;
                    if (elem.isExternalSchema()) {
                        ns = rdfNamespaceDAO.getNamespace(elem.getNameSpacePrefix());
                        if (!nameSpaces.contains(ns)) {
                            nameSpaces.add(ns);
                        }
                    }
                }

            }

            return nameSpaces;

        } catch (DAOException daoe) {
            throw new ServiceException("Failed to get vocabulary namespaces " + daoe.getMessage(), daoe);
        }
    }

    @Override
    public boolean isReferenceElement(int elementId) {

        DataElement elem = dataElementDAO.getDataElement(elementId);
        Map<String, List<String>> elemAttributeValues =
                attributeDAO.getAttributeValues(elem.getId(), ParentType.ELEMENT.toString());

        elem.setElemAttributeValues(elemAttributeValues);

        return elem.getDatatype().equals("reference");
    }

    @Override
    public List<Triple<String, String, Integer>> getVocabularyBoundElementNamesByLanguage(List<VocabularyConcept> concepts) {
        List<Triple<String, String, Integer>> boundElementNamesByLanguage = new ArrayList<Triple<String, String, Integer>>();
        for (VocabularyConcept concept : concepts) {
            Map<String, Integer> boundElementsWithLanguageToMaxValuesCount = new HashMap<String, Integer>();
            for (Iterator<List<DataElement>> it = concept.getElementAttributes().iterator(); it.hasNext(); ) {
                List<DataElement> elementAttributes = it.next();
                for (DataElement elementAttribute : elementAttributes) {
                    String key = elementAttribute.getIdentifier() + "~" + StringUtils.trimToEmpty(elementAttribute.getAttributeLanguage()); // e.g. skos:prefLabel~el
                    if (boundElementsWithLanguageToMaxValuesCount.containsKey(key)) {
                        boundElementsWithLanguageToMaxValuesCount.put(key, boundElementsWithLanguageToMaxValuesCount.get(key) + 1);
                    } else {
                        boundElementsWithLanguageToMaxValuesCount.put(key, 1);
                    }
                }
            }

            for (String boundElementsWithLanguageKey : boundElementsWithLanguageToMaxValuesCount.keySet()) {
                boolean boundElementExists = false;
                String identifier = boundElementsWithLanguageKey.split("~")[0];
                String language = boundElementsWithLanguageKey.split("~").length > 1 ? boundElementsWithLanguageKey.split("~")[1] : "";
                Integer maxValuesCount = boundElementsWithLanguageToMaxValuesCount.get(boundElementsWithLanguageKey);

                for (Triple<String, String, Integer> boundElementWithLanguage : boundElementNamesByLanguage) {
                    if (StringUtils.equals(boundElementWithLanguage.getLeft(), identifier) && StringUtils.equals(boundElementWithLanguage.getCentral(), language)) {
                        if (maxValuesCount > boundElementWithLanguage.getRight()) {
                            boundElementWithLanguage.setRight(maxValuesCount);
                        }
                        boundElementExists = true;
                        break;
                    }
                }
                if (!boundElementExists) {
                    boundElementNamesByLanguage.add(new Triple<String, String, Integer>(identifier, language, maxValuesCount));
                }
            }
        }
        return boundElementNamesByLanguage;
    }

    @Override
    public VocabularyResult searchVocabularies(VocabularyFilter filter) throws ServiceException {
        try {
            return vocabularyFolderDAO.searchVocabularies(filter);
        } catch (Exception e) {
            throw new ServiceException("Failed to get vocabularies: " + e.getMessage(), e);
        }
    }

    @Override
    public VocabularyResult checkIfVocabulariesCanBeBoundToElements(VocabularyResult vocabularyResult) throws ServiceException {
        try {
            List<VocabularyFolder> vocabularies = vocabularyResult.getList();
            for (VocabularyFolder vocabulary : vocabularies) {
                if (vocabulary.isNotationsEqualIdentifiers() == true) {
                    vocabulary.setCanBeBoundToElements(true);
                } else {
                    Boolean result = vocabularyConceptDAO.checkIfConceptsWithoutNotationExist(vocabulary.getId());
                    vocabulary.setCanBeBoundToElements(result);
                }
            }
            return vocabularyResult;
        } catch (Exception e) {
            throw new ServiceException("Failed to check if vocabularies can be bound to elements: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VocabularyConceptData> searchAllVocabularyConcept(VocabularyConceptFilter filter) throws ServiceException {
        try {
            VocabularyConceptResult vocabularyConceptResult = vocabularyConceptDAO.searchVocabularyConcepts(filter);
            List<VocabularyConceptData> result = new ArrayList<VocabularyConceptData>();

            for (VocabularyConcept concept : vocabularyConceptResult.getList()) {
                VocabularyFolder vocabulary = vocabularyFolderDAO.getVocabularyFolder(concept.getVocabularyId());

                VocabularyConceptData data = new VocabularyConceptData();
                data.setId(concept.getId());
                data.setIdentifier(concept.getIdentifier());
                data.setLabel(concept.getLabel());
                data.setUserName(vocabulary.getWorkingUser());
                data.setVocabularyIdentifier(vocabulary.getIdentifier());
                data.setVocabularyLabel(vocabulary.getLabel());
                data.setVocabularySetIdentifier(vocabulary.getFolderName());
                data.setVocabularySetLabel(vocabulary.getFolderLabel());

                data.setVocabularyStatus(vocabulary.getRegStatus());
                data.setWorkingCopy(vocabulary.isWorkingCopy());

                result.add(data);
            }

            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to perform concept search: " + e.getMessage(), e);
        }
    }

    @Override
    public void bindVocabulary(int elementId, int vocabularyId) {
        dataElementDAO.bindVocabulary(elementId, vocabularyId);
    }

    @Override
    public VocabularyFolder getVocabularyWithConcepts(String identifier, String vocSet) {
        VocabularyFolder vocabulary = vocabularyFolderDAO.getVocabularyFolder(vocSet, identifier, false);
        if (vocabulary != null) {
            List<VocabularyConcept> concepts = vocabularyConceptDAO.getVocabularyConcepts(vocabulary.getId());
            vocabulary.setConcepts(concepts);
        }

        return vocabulary;
    }

    @Override
    public void fixRelatedReferenceElements(int vocabularyId, List<VocabularyConcept> concepts) {
        for (VocabularyConcept concept : concepts) {
            List<List<DataElement>> elems = concept.getElementAttributes();
            for (List<DataElement> elemMeta : elems) {
                if (!elemMeta.isEmpty() && "reference".equals(elemMeta.get(0).getDatatype())) {
                    for (DataElement elem : elemMeta) {
                        if (elem.getRelatedConceptId() != null && elem.getRelatedConceptId() != 0) {
                            dataElementDAO.createInverseElements(elem.getId(), concept.getId(), elem.getRelatedConceptId());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fixRelatedLocalRefElementsForImport(int vocabularyId, List<VocabularyConcept> toBeUpdatedConcepts) throws ServiceException {
        if (toBeUpdatedConcepts == null) {
            return;
        }

        for (VocabularyConcept concept : toBeUpdatedConcepts) {
            List<List<DataElement>> conceptElementAttributes = concept.getElementAttributes();

            if (conceptElementAttributes == null) {
                continue;
            }

            for (List<DataElement> dataElementValues : conceptElementAttributes) {
                if (dataElementValues == null) {
                    continue;
                }
                for (DataElement elem : dataElementValues) {
                    //for localref elements and reference elements which reside in the same vocabulary ,
                    // create inverse links immediately to show them in the working copy as well:
                    Integer relatedConceptId = elem.getRelatedConceptId();
                    if (elem.getRelatedConceptId() != null && elem.getRelatedConceptId() != 0) {

                        String elemType = dataElementDAO.getDataElementDataType(elem.getId());
                        if ("localref".equals(elemType) || ("reference".equals(elemType) && getVocabularyConcept(relatedConceptId).getVocabularyId() == vocabularyId)) {
                            dataElementDAO.createInverseElements(elem.getId(), concept.getId(), elem.getRelatedConceptId());
                        }
                    }
                }
            }//end of inner for loop
        }//end of outer for loop
    }//end of method fixRelatedLocalRefElementsForImport

    @Override
    public int populateEmptyBaseUris(String prefix) throws ServiceException {
        try {
            return vocabularyFolderDAO.populateEmptyBaseUris(prefix);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    } // end of method populateEmptyBaseUris

    @Override
    public int changeSitePrefix(String oldSitePrefix, String newSitePrefix) throws ServiceException {
        try {
            return vocabularyFolderDAO.changeSitePrefix(oldSitePrefix, newSitePrefix);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    } // end of method changeSitePrefix

    @Override
    public List<VocabularyFolder> getRecentlyReleasedVocabularyFolders(int limit) throws ServiceException {
        try {
            return vocabularyFolderDAO.getRecentlyReleasedVocabularyFolders(limit);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    } // end of method getRecentlyReleasedVocabularyFolders

    @Override
    public Map<Integer, List<List<DataElement>>> getVocabularyConceptsDataElementValues(int vocabularyFolderId,
                                                                                        int[] vocabularyConceptIds, boolean emptyAttributes) {
        return dataElementDAO.getVocabularyConceptsDataElementValues(vocabularyFolderId, vocabularyConceptIds, emptyAttributes);
    }

    /*
     * (non-Javadoc)
     * @see eionet.meta.service.IVocabularyService#vocabularyConceptExists(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean vocabularyConceptExists(String vocabularySet, String vocabularyIdentifier, String conceptIdentifier) throws ServiceException {

        VocabularyFolder vocabulary = getVocabularyFolder(vocabularySet, vocabularyIdentifier, false);
        if (vocabulary != null) {
            VocabularyConcept concept = getVocabularyConcept(vocabulary.getId(), conceptIdentifier, false);
            return concept != null;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void markVocabularyFolderToBeDeleted(Integer vocabularyFolderId) throws ServiceException {

        try {
            VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);

            vocabularyFolder.setRegStatus(RegStatus.DEPRECATED);

            vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);

        } catch (Exception e) {
            throw new ServiceException("Failed to mark vocabulary folder as deprecated: " + e.getMessage(), e);
        }

    }

    @Override
    public List<Integer> batchCreateVocabularyConcepts(List<VocabularyConcept> vocabularyConcepts, int batchSize)
            throws ServiceException {
        try {
            return vocabularyConceptDAO.batchCreateVocabularyConcepts(vocabularyConcepts, batchSize);
        } catch (Exception e) {
            throw new ServiceException("Failed to batch insert vocabulary concepts: " + e.getMessage(), e);
        }
    }

    @Override
    public int[][] batchUpdateVocabularyConcepts(List<VocabularyConcept> vocabularyConcepts, int batchSize) throws ServiceException {
        try {
            for (VocabularyConcept vocConcept : vocabularyConcepts
            ) {
                this.updateVocabularyConceptStatusModifiedIfRequired(vocConcept);
            }
            return vocabularyConceptDAO.batchUpdateVocabularyConcepts(vocabularyConcepts, batchSize);
        } catch (Exception e) {
            throw new ServiceException("Failed to batch update vocabulary concepts: " + e.getMessage(), e);
        }
    }

    @Override
    public void batchUpdateVocabularyConceptsDataElementValues(List<VocabularyConcept> vocabularyConcepts, int batchSize)
            throws ServiceException {
        List<Integer> vocabularyConceptIds = new ArrayList<Integer>();
        for (VocabularyConcept vocabularyConcept : vocabularyConcepts) {
            vocabularyConceptIds.add(vocabularyConcept.getId());
        }

        dataElementDAO.deleteVocabularyConceptDataElementValues(vocabularyConceptIds);
        dataElementDAO.batchInsertVocabularyConceptDataElementValues(vocabularyConcepts, batchSize);
    }

    @Override
    public void batchFixRelatedReferenceElements(List<VocabularyConcept> concepts, int batchSize) {
        Set<Integer> dataElementsIds = new HashSet<Integer>();
        for (VocabularyConcept concept : concepts) {
            List<List<DataElement>> elems = concept.getElementAttributes();
            for (List<DataElement> elemMeta : elems) {
                dataElementsIds.add(elemMeta.get(0).getId());
            }
        }

        if (dataElementsIds.isEmpty()) {
            return;
        }

        Map<Integer, String> dataElementDataTypes = dataElementDAO.getDataElementDataTypes(dataElementsIds);

        List<Triple<Integer, Integer, Integer>> relatedReferenceElements = new ArrayList<Triple<Integer, Integer, Integer>>();
        for (VocabularyConcept concept : concepts) {
            List<List<DataElement>> elems = concept.getElementAttributes();
            for (List<DataElement> elemMeta : elems) {
                for (DataElement elem : elemMeta) {
                    if (elem.getRelatedConceptId() != null && elem.getRelatedConceptId() != 0) {
                        String elemType = dataElementDataTypes.get(elem.getId());
                        if ("localref".equals(elemType) || "reference".equals(elemType)) {
                            Triple<Integer, Integer, Integer> triple = new Triple<Integer, Integer, Integer>(elem.getId(), concept.getId(), elem.getRelatedConceptId());
                            relatedReferenceElements.add(triple);
                        }
                    }
                }
            }
        }
        dataElementDAO.batchCreateInverseElements(relatedReferenceElements, batchSize);
    }

    @Override
    public boolean hasVocabularyWorkingCopy(String folderName, String identifier) {
        VocabularyFolder vocabulary = vocabularyFolderDAO.getVocabularyFolder(folderName, identifier, true);
        return (vocabulary != null) ? true : false;
    }

    @Override
    public Boolean checkIfVocabularyIsBoundToElement(Integer vocabularyId) {
        return vocabularyFolderDAO.isVocabularyBoundToElement(vocabularyId);
    }

    @Override
    public Integer getCheckedOutCopyIdForVocabulary(Integer vocabularyId) {
        return vocabularyFolderDAO.getWorkingCopyByVocabularyId(vocabularyId);
    }

    @Override
    public Boolean checkIfConceptShouldBeAddedWhenBoundToElement(Integer newVocabularyId, String notation) {

        VocabularyFolder vocabulary = vocabularyFolderDAO.getVocabularyFolder(newVocabularyId);
        if (!vocabulary.isNotationsEqualIdentifiers()) {
            //find original VOCABULARY id
            Integer originalVocabularyId = getCheckedOutCopyIdForVocabulary(newVocabularyId);
            if (originalVocabularyId == null || originalVocabularyId == 0) {
                originalVocabularyId = newVocabularyId;
            }
            //check if vocabulary is bound to element and the concept does not have notation
            if (checkIfVocabularyIsBoundToElement(originalVocabularyId)) {
                if (Util.isEmpty(notation)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void updateAcceptedNotAcceptedDate(VocabularyConcept vocabularyConcept) throws ServiceException {
        List<List<DataElement>> elements = vocabularyConcept.getElementAttributes();
        if (elements != null) {
            for (List<DataElement> values : elements) {
                if (values!=null) {
                    for (DataElement element : values) {
                        if (element != null && element.getIdentifier() != null && element.getIdentifier().equals(ADMS_STATUS) && StringUtils.isNotEmpty(element.getAttributeValue()) && element.getAttributeValue().contains("accepted")) {
                            vocabularyConcept.setAcceptedDate(new Date());
                            return;
                        } else if (element != null && element.getIdentifier() != null && element.getIdentifier().equals(ADMS_STATUS) && StringUtils.isNotEmpty(element.getAttributeValue()) && element.getAttributeValue().contains("notAccepted")) {
                            vocabularyConcept.setNotAcceptedDate(new Date());
                            return;
                        } else if (element != null && element.getIdentifier() != null && element.getIdentifier().equals(SKOS_NARROWER) && element.getRelatedConceptId() != null) {
                            VocabularyConcept relatedConcept = this.getVocabularyConcept(element.getRelatedConceptId());

                            VocabularyFolder relConceptFolder = vocabularyFolderDAO.getVocabularyFolderOfConcept(relatedConcept.getId());
                            int conceptId = relatedConcept.getId();
                            Map<Integer, List<List<DataElement>>> vocabularyConceptsDataElementValues =
                                    dataElementDAO.getVocabularyConceptsDataElementValues(relConceptFolder.getId(), new int[]{conceptId}, false);
                            relatedConcept.setElementAttributes(vocabularyConceptsDataElementValues.get(conceptId));

                            List<List<DataElement>> relatedConceptElements = relatedConcept.getElementAttributes();
                            if (relatedConceptElements!=null) {
                                for (List<DataElement> elemList : relatedConceptElements) {
                                    if (elemList!=null) {
                                        for (DataElement relConceptElement : elemList) {
                                            if (relConceptElement != null && relConceptElement.getIdentifier() != null && relConceptElement.getIdentifier().equals(ADMS_STATUS) && StringUtils.isNotEmpty(relConceptElement.getAttributeValue())) {
                                                for (String status : Enumerations.StatusesForNotAcceptedDate.getEnumValues()) {
                                                    if (relConceptElement.getAttributeValue().contains(status)) {
                                                        vocabularyConcept.setNotAcceptedDate(new Date());
                                                        return;
                                                    }
                                                }
                                                for (String status : Enumerations.StatusesForAcceptedDate.getEnumValues()) {
                                                    if (relConceptElement.getAttributeValue().contains(status)) {
                                                        vocabularyConcept.setAcceptedDate(new Date());
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
