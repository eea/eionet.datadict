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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.model.ContactDetails;
import eionet.datadict.model.DataElementAttribute;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetAttribute;
import eionet.datadict.services.data.ContactService;
import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.VersionManager;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.savers.DataElementHandler;
import eionet.meta.savers.DatasetHandler;
import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.*;
import eionet.util.sql.ConnectionUtil;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.util.UriUtils;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Vocabulary concept action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabularyconcept/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{vocabularyConcept.identifier}/{$event}")
public class VocabularyConceptActionBean extends AbstractActionBean {

    /**
     * JSP pages.
     */
    private static final String VIEW_VOCABULARY_CONCEPT_JSP = "/pages/vocabularies/viewVocabularyConcept.jsp";
    /**
     * JSP page for edit screen.
     */
    private static final String EDIT_VOCABULARY_CONCEPT_JSP = "/pages/vocabularies/editVocabularyConcept.jsp";

    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;

    /**
     * data service.
     */
    @SpringBean
    private IDataService dataService;

    /**
     * attributeValueDataService
     */
    @SpringBean
    private ContactService contactService;

    /**
     * Attribute DAO.
     */
    @SpringBean
    private IAttributeDAO attributeDao;

    @SpringBean
    private IVocabularyConceptDAO vocabularyConceptDAO;

    @SpringBean
    private DataElementDao dataElementDao;

    @SpringBean
    private DatasetDao datasetDao;

    /**
     * Vocabulary folder.
     */
    private VocabularyFolder vocabularyFolder;

    /**
     * Vocabulary concept to add/edit.
     */
    private VocabularyConcept vocabularyConcept;


    /**
     * Other vocabulary concepts in the vocabulary folder.
     */
    private List<VocabularyConcept> vocabularyConcepts;

    /**
     * private helper property for vocabulary concept identifier .
     */
    private String conceptIdentifier;

    /**
     * To be used in linking related concepts.
     */
    private VocabularyConceptFilter relatedConceptsFilter;

    /**
     * search filter for vocabularies.
     */
    private VocabularyFilter vocabularyFilter;

    /**
     * search filter for vocabularies that are browsed when adding a related concept.
     */
    private VocabularyResult vocabularies;

    /**
     * Related Vocabulary concepts.
     */
    private VocabularyConceptResult relatedVocabularyConcepts;

    /**
     * selected vocabulary for reference element.
     */
    private VocabularyFolder relatedVocabulary;

    /**
     * ch3 element related vocabulary names.
     */
    private List<String> elemVocabularyNames;

    /**
     * Popup div id to keep open, when validation error occur.
     */
    private String editDivId;

    /**
     * Element Id that is currently active when manipulating with popups.
     */
    private String elementId;

    /**
     * which event the sorting in the table has to submit.
     */
    private String searchEventName = "searchConcepts";

    /**
     * vocabulary set IDs to be excluded from search.
     */
    private List<Integer> excludedVocSetIds;

    /**
     * vocabulary set labels excluded manually from the search.
     */
    private List<String> excludedVocSetLabels;

    /**
     * contact details list
     */
    private Set<ContactDetails> contactDetails;

    /**
     *
     */
    private String contactDetailsString;

    /**
     *
     */
    private boolean usrLoggedIn;

    /**
     * View action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException, JsonProcessingException {
        // to be removed if Stripes is upgraded to 1.5.8
        handleConceptIdentifier();

        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), false);

        setContactDetails(getContactDatasetAndDataElements());
        ObjectMapper mapper = new ObjectMapper();
        setContactDetailsString(mapper.writeValueAsString(contactDetails));

        validateView();

        // LOGGER.debug("Element attributes: " + vocabularyConcept.getElementAttributes().size());

        setUsrLoggedIn(isUserLoggedIn());
        return new ForwardResolution(VIEW_VOCABULARY_CONCEPT_JSP);
    }

    private Set<ContactDetails> getContactDatasetAndDataElements() {
        List<Integer> acceptedMAttributes = new ArrayList<>(Arrays.asList(61, 62, 63, 64));
        contactDetails = contactService.getAllByValue(Integer.toString(vocabularyConcept.getId()));
        return contactDetails.stream().filter(contactDetails -> acceptedMAttributes.contains(contactDetails.getmAttributeId())).collect(Collectors.toSet());
    }

    /**
     * Stripes has a bug that double-decodes the request path info. "+" is converted to " " that makes impossible to distinguish
     * space and '+' This method extracts the original conceptId.
     */
    private void handleConceptIdentifier() {
        String realRequestPath = getRequestedPath(getContext().getRequest());
        // vocabularyconcept/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{vocabularyConcept.identifier}/{$event}
        String[] params = realRequestPath.split("\\/", -1);
        String identifier = params[4];
        String decodedIdentifier = UriUtils.decode(identifier, "utf-8");
        if (decodedIdentifier != null) {
            setConceptIdentifier(decodedIdentifier);
        } else {
            LOGGER.warn("Unsupported Encoding Exception for identifier: " + identifier);
            setConceptIdentifier(identifier);
        }
    }

    /**
     * Display edit form action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution edit() throws ServiceException {
        // to be removed if Stripes is upgraded to 1.5.8
        handleConceptIdentifier();

        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), true);

        validateView();
        initElemVocabularyNames();
        initBeans();

        editDivId = null;

        return new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
    }

    private static Pair<Integer, String> getPairOfDataElemIdAndParentType(ContactDetails contactDetails) {
        return Pair.of(contactDetails.getDataElemId(), contactDetails.getParentType());
    }

    /**
     * Action for saving concept.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveConcept() throws Exception {
        Thread.currentThread().setName("SAVE-VOCABULARY-CONCEPT");
        ActionMethodUtils.setLogParameters(getContext());
        vocabularyConcept.setIdentifier(getConceptIdentifier());
        vocabularyService.updateVocabularyConcept(vocabularyConcept);
        relatedVocabulary = null;
        relatedVocabularyConcepts = null;

        addSystemMessage("Vocabulary concept saved successfully");

        RedirectResolution resolution = new RedirectResolution(getClass(), "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        resolution.addParameter("vocabularyConcept.identifier", Util.encodeURLPath(vocabularyConcept.getIdentifier()));

        resolution.addParameter("editDivId", editDivId);
        resolution.addParameter("elementId", elementId);

        // initElemVocabularyNames();
        return resolution;
    }

    public Resolution deleteContactFromAllElements() throws Exception {
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), false);


        contactDetailsString = getContactDetailsString();
        ObjectMapper mapper = new ObjectMapper();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(contactDetailsString)) {
            contactDetails = mapper.readValue(contactDetailsString, new TypeReference<Set<ContactDetails>>(){});
        }

        DDUser user = SecurityUtil.getUser(getContext().getRequest());
        Connection conn = ConnectionUtil.getConnection();
        VersionManager verMan = setVersionManager(user, conn);

        List<ContactDetails> commonElements = contactDetails.stream().filter(contactDetail -> contactDetail.getParentType().equals("DataElement") && (contactDetail.getDataElemParentNs() == null || contactDetail.getDataElemParentNs().equals(0))).collect(Collectors.toList());
        Map<Integer, List<ContactDetails>> commonElementsMap = commonElements.stream().collect(groupingBy(ContactDetails::getDataElemId));
        commonElements.stream().forEach(element -> contactDetails.remove(element));
        Map<Pair<Integer, String>, List<ContactDetails>> groupByDataElemIdAndParentType = contactDetails.stream().collect(groupingBy(VocabularyConceptActionBean::getPairOfDataElemIdAndParentType));
        Map<Integer, Pair<List<DatasetAttribute>, List<DataElementAttribute>>> map = new HashMap<>();
        for (Pair<Integer, String> pair : groupByDataElemIdAndParentType.keySet()) {
            createDatasetAndElementAttributes(groupByDataElemIdAndParentType, map, pair);
        }

        for (Integer datasetId : map.keySet()) {
            List<DatasetAttribute> datasetAttributes = map.get(datasetId).getLeft();
            List<DataElementAttribute> dataElementAttributes = map.get(datasetId).getRight();
            String[] dsIds = new String[5];
            String dsCopyID = null;
            try {
                DataSet dataSet = datasetDao.getById(datasetId);
                dsCopyID = verMan.checkOut(String.valueOf(datasetId), "dst");
                dsIds[0] = dsCopyID;
                for (DatasetAttribute datasetAttribute : datasetAttributes) {
                    attributeDao.deleteAttribute(datasetAttribute.getAttributeId(), Integer.parseInt(dsCopyID), datasetAttribute.getValue());
                }
                for (DataElementAttribute dataElementAttribute : dataElementAttributes) {
                    String datElemCopyID = String.valueOf(dataElementDao.getDataElemCheckoutOutId(Integer.valueOf(dsCopyID), dataElementAttribute.getIdentifier(), dataElementAttribute.getTableIdentifier()));
                    attributeDao.deleteAttribute(dataElementAttribute.getAttributeId(), Integer.valueOf(datElemCopyID), dataElementAttribute.getValue());
                }
                checkInDataset(user, conn, dsCopyID, dsIds, String.valueOf(dataSet.getId()), dataSet.getRegStatus().getName(), dataSet.getShortName(), dataSet.getIdentifier());
            } catch (Exception e) {
                if (dsCopyID != null) {
                    undoCheckoutDataset(user, conn, dsCopyID, dsIds);
                }
                throw new Exception(e.getMessage());
            }
        }
        for (Integer dataElemId : commonElementsMap.keySet()) {
            String datElemCopyID = null;
            List<ContactDetails> commonElementsContactDet = commonElementsMap.get(dataElemId);
            try {
                eionet.datadict.model.DataElement dataElement = dataElementDao.getById(dataElemId);
                datElemCopyID = verMan.checkOut(String.valueOf(dataElemId), "elm");
                for (ContactDetails contactDetail : commonElementsContactDet) {
                    attributeDao.deleteAttribute(contactDetail.getmAttributeId(), Integer.valueOf(datElemCopyID), contactDetail.getValue());
                }
                checkInDataElement(conn, user, datElemCopyID, dataElement.getRegStatus().getName(), dataElemId, dataElement.getShortName(), dataElement.getIdentifier());
            } catch (Exception e) {
                if (datElemCopyID != null) {
                    undoCheckoutDataElement(user, conn, datElemCopyID);
                }
                throw new Exception(e.getMessage());
            }
        }

        contactDetails = getContactDatasetAndDataElements();

        validateView();
        return new ForwardResolution(VIEW_VOCABULARY_CONCEPT_JSP);
    }

    private VersionManager setVersionManager(DDUser user, Connection conn) {
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);
        return new VersionManager(conn, searchEngine, user);
    }

    private void createDatasetAndElementAttributes(Map<Pair<Integer, String>, List<ContactDetails>> groupByDataElemIdAndParentType, Map<Integer, Pair<List<DatasetAttribute>, List<DataElementAttribute>>> map, Pair<Integer, String> pair) {
        List<DatasetAttribute> datasetAttributes = new ArrayList<>();
        List<DataElementAttribute> dataElementAttributes = new ArrayList<>();
        List<ContactDetails> contactDetailsList = groupByDataElemIdAndParentType.get(pair);
        for (ContactDetails contactDetail : contactDetailsList) {
            Integer datasetId = contactDetail.getDataElementDatasetId() != null ? contactDetail.getDataElementDatasetId() : contactDetail.getDataElemId();
            Pair<List<DatasetAttribute>, List<DataElementAttribute>> contactHolder = map.get(datasetId);
            if (contactHolder != null) {
                datasetAttributes = contactHolder.getLeft();
                dataElementAttributes = contactHolder.getRight();
            } else {
                map.put(datasetId, Pair.of(datasetAttributes, dataElementAttributes));
            }
            if (contactDetail.getParentType().equals("Dataset")) {
                DatasetAttribute datasetAttribute = createDatasetAttribute(contactDetail);
                datasetAttributes.add(datasetAttribute);
            } else if (contactDetail.getParentType().equals("DataElement")) {
                DataElementAttribute dataElementAttribute = createDataElementAttribute(contactDetail);
                dataElementAttributes.add(dataElementAttribute);
            }
        }
    }

    private DatasetAttribute createDatasetAttribute(ContactDetails entry) {
        DatasetAttribute datasetAttribute = new DatasetAttribute();
        datasetAttribute.setDataElemId(entry.getDataElemId()).setValue(entry.getValue()).setAttributeId(entry.getmAttributeId()).setIdentifier(entry.getDatasetIdentifier()).setShortName(entry.getDatasetShortName());
        datasetAttribute.setRegStatus(entry.getDatasetRegStatus());
        return datasetAttribute;
    }

    private DataElementAttribute createDataElementAttribute(ContactDetails entry) {
        DataElementAttribute dataElementAttribute = new DataElementAttribute();
        dataElementAttribute.setDataElemId(entry.getDataElemId()).setAttributeId(entry.getmAttributeId()).setValue(entry.getValue()).setIdentifier(entry.getDataElementIdentifier()).setShortName(entry.getDataElementShortName());
        dataElementAttribute.setTableId(entry.getDataElemTableId());
        dataElementAttribute.setTableIdentifier(entry.getDataElemTableIdentifier());
        dataElementAttribute.setParentNs(entry.getDataElemParentNs());
        dataElementAttribute.setTopNs(entry.getDataElemTopNs());
        dataElementAttribute.setType(entry.getDataElemType());
        return dataElementAttribute;
    }

    private void checkInDataElement(Connection conn, DDUser user, String datElemCopyID, String regStatus, Integer dataElemId, String shortName, String identifier) throws Exception {
        eionet.meta.savers.Parameters datElemHandlerParams = new eionet.meta.savers.Parameters();
        datElemHandlerParams.addParameterValue("mode", "edit");
        datElemHandlerParams.addParameterValue("common", "true");
        datElemHandlerParams.addParameterValue("switch_type", "false");
        datElemHandlerParams.addParameterValue("check_in", "true");
        datElemHandlerParams.addParameterValue("delem_id", datElemCopyID);
        datElemHandlerParams.addParameterValue("checkedout_copy_id", String.valueOf(dataElemId));
        if (regStatus.equals("Incomplete")) {
            datElemHandlerParams.addParameterValue("upd_version", "false");
        } else {
            datElemHandlerParams.addParameterValue("upd_version", "true");
        }
        datElemHandlerParams.addParameterValue("delem_name", shortName);
        datElemHandlerParams.addParameterValue("idfier", identifier);
        DataElementHandler elementHandler = new DataElementHandler(conn, datElemHandlerParams, getContext().getServletContext());
        elementHandler.setUser(user);
        elementHandler.execute_();
    }

    private void undoCheckoutDataElement(DDUser user, Connection conn, String datElemCopyID) throws Exception {
        eionet.meta.savers.Parameters datElemHandlerParams = new eionet.meta.savers.Parameters();
        datElemHandlerParams.addParameterValue("mode", "delete");
        datElemHandlerParams.addParameterValue("delem_id", datElemCopyID);
        DataElementHandler elementHandler = new DataElementHandler(conn, datElemHandlerParams, getContext().getServletContext());
        elementHandler.setUser(user);
        elementHandler.execute_();
    }

    private void undoCheckoutDataset(DDUser user, Connection conn, String dsCopyID, String[] dsIds) throws Exception {
        eionet.meta.savers.Parameters dsHandlerParams = new eionet.meta.savers.Parameters();
        dsHandlerParams.addParameterValue("mode", "delete");
        dsHandlerParams.addParameterValue("complete", "true");
        dsHandlerParams.addParameterValue("useForce", "false");
        dsHandlerParams.addParameterValue("ds_id", dsCopyID);
        dsHandlerParams.addParameterValue("ds_ids", dsIds.toString());
        DatasetHandler dsHandler = new DatasetHandler(conn, dsHandlerParams, getContext().getServletContext());
        dsHandler.setUser(user);
        dsHandler.execute();
    }

    private void checkInDataset(DDUser user, Connection conn, String dsCopyID, String[] dsIds, String elemId, String datasetRegStatus, String datasetShortName, String datasetIdentifier) throws Exception {
        eionet.meta.savers.Parameters dsHandlerParams = new eionet.meta.savers.Parameters();
        dsHandlerParams.addParameterValue("mode", "edit");
        dsHandlerParams.addParameterValue("ds_id", dsCopyID);
        dsHandlerParams.addParameterValue("check_in", "true");
        dsHandlerParams.addParameterValue("checkedout_copy_id", elemId);
        if (datasetRegStatus.equals("Incomplete")) {
            dsHandlerParams.addParameterValue("upd_version", "false");
        } else {
            dsHandlerParams.addParameterValue("upd_version", "true");
        }
        dsHandlerParams.addParameterValue("ds_ids", dsIds.toString());
        dsHandlerParams.addParameterValue("ds_name", datasetShortName);
        dsHandlerParams.addParameterValue("idfier", datasetIdentifier);
        DatasetHandler dsHandler = new DatasetHandler(conn, dsHandlerParams, getContext().getServletContext());
        dsHandler.setUser(user);
        dsHandler.execute();
    }

    /**
     * Marks vocabulary concept obsolete.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution markConceptObsolete() throws ServiceException {
        Thread.currentThread().setName("MARK-VOCABULARY-CONCEPT-OBSOLETE");
        ActionMethodUtils.setLogParameters(getContext());
        vocabularyConcept.setIdentifier(getConceptIdentifier());
        vocabularyService.markConceptsInvalid(Collections.singletonList(vocabularyConcept.getId()));

        addSystemMessage("Vocabulary concept marked obsolete");

        RedirectResolution resolution = new RedirectResolution(getClass(), "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        resolution.addParameter("vocabularyConcept.identifier", Util.encodeURLPath(vocabularyConcept.getIdentifier()));
        return resolution;
    }

    /**
     * Removes the obsolete status from concept.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution unMarkConceptObsolete() throws ServiceException {
        Thread.currentThread().setName("UNMARK-VOCABULARY-CONCEPT-OBSOLETE");
        ActionMethodUtils.setLogParameters(getContext());
        vocabularyConcept.setIdentifier(getConceptIdentifier());
        vocabularyService.markConceptsValid(Collections.singletonList(vocabularyConcept.getId()));

        addSystemMessage("Obsolete status removed from vocabulary concept");

        RedirectResolution resolution = new RedirectResolution(getClass(), "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        resolution.addParameter("vocabularyConcept.identifier", Util.encodeURLPath(vocabularyConcept.getIdentifier()));
        return resolution;
    }

    /**
     * Validates save concept.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"saveConcept"})
    public void validateSaveConcept() throws ServiceException {
        if (!isUpdateRight()) {
            addGlobalValidationError("No permission to modify vocabulary");
        }

        if (StringUtils.isEmpty(getConceptIdentifier())) {
            addGlobalValidationError("Vocabulary concept identifier is missing");
        } else {
            if (vocabularyFolder.isNumericConceptIdentifiers()) {
                if (!Util.isNumericID(getConceptIdentifier())) {
                    addGlobalValidationError("Vocabulary concept identifier must be numeric value");
                }
            } else {
                if (!Util.isValidIdentifier(getConceptIdentifier())) {
                    addGlobalValidationError("Vocabulary concept identifier contains illegal characters (/%?#:\\)");
                }
                if (VocabularyFolderActionBean.RESERVED_VOCABULARY_EVENTS.contains(getConceptIdentifier())) {
                    addGlobalValidationError("This vocabulary concept identifier is reserved value and cannot be used");
                }
            }
        }
        if (StringUtils.isEmpty(vocabularyConcept.getLabel())) {
            addGlobalValidationError("Vocabulary concept label is missing");
        }

        //check for dates, they cannot be set to future
        Date today = new Date(System.currentTimeMillis());

        if (vocabularyConcept.getStatusModified() != null && today.before(vocabularyConcept.getStatusModified())) {
            addGlobalValidationError("Status modified date cannot be set to future");
        }

        if (vocabularyConcept.getAcceptedDate() != null && today.before(vocabularyConcept.getAcceptedDate())) {
            addGlobalValidationError("Accepted date cannot be set to future");
        }

        if (vocabularyConcept.getNotAcceptedDate() != null && today.before(vocabularyConcept.getNotAcceptedDate())) {
            addGlobalValidationError("Not accepted date cannot be set to future");
        }

        // Validate unique identifier
        if (!vocabularyService.isUniqueConceptIdentifier(getConceptIdentifier(), vocabularyFolder.getId(),
                vocabularyConcept.getId())) {
            addGlobalValidationError("Vocabulary concept identifier is not unique");
        }

        List<String> uniqueValues = new ArrayList<String>();

        if (vocabularyConcept.getElementAttributes() != null) {
            for (List<DataElement> elems : vocabularyConcept.getElementAttributes()) {
                uniqueValues.clear();
                if (elems != null) {
                    DataElement metaInfo = elems.get(0);
                    for (DataElement elem : elems) {
                        if (elem != null) {
                            if (vocabularyService.isReferenceElement(elem.getId()) && elem.getRelatedConceptId() == null) {
                                if (elem.getAttributeValue() != null && !Util.isValidUri(elem.getAttributeValue())) {
                                    addGlobalValidationError("Related match to an external vocabulary \"" + metaInfo.getName()
                                            + "\" value \"" + elem.getAttributeValue()
                                            + "\" is not a valid URI. \n The allowed schemes are: "
                                            + "http, https, ftp, mailto, tel and urn");
                                }
                            }

                            if (uniqueValues.contains(elem.getUniqueValueHash())) {
                                addGlobalValidationError("'" + metaInfo.getName() + "'" + " has the same value more than once: "
                                        + elem.getValueText());
                            }
                            uniqueValues.add(elem.getUniqueValueHash());
                        }
                    }
                }
            }
        }

        if (isValidationErrors()) {
            initBeans();
            addElementMetadata();
            initElemVocabularyNames();
            editDivId = null;
            Resolution resolution = new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     * search concepts to be added as reference element.
     *
     * @return stripes resolution
     * @throws ServiceException if search fails
     */
    public Resolution searchConcepts() throws ServiceException {

        String realRequestPath = getRequestedPath(getContext().getRequest());
        setConceptIdentifier(vocabularyConcept.getIdentifier());

        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());

        // current editable concept:
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), true);
        validateView();
        initBeans();
        initSearchFilters();

        //exclude concepts of original vocabulary from the search to get the attribute related with checked out concept
        List<Integer> excludedConceptIds = getExcludedIdsOfOriginalFolder();
        excludedConceptIds.add(vocabularyConcept.getId());
        relatedConceptsFilter.setExcludedIds(excludedConceptIds);

        // this is needed because of "limit " clause in the SQL. if this remains true, paging does not work in display:table
        relatedConceptsFilter.setUsePaging(false);
        //relatedConceptsFilter.setObsoleteStatus(ObsoleteStatus.ALL);

        // vocabulary is selected in step 1 (non CH3)
        String vocabularyId = getContext().getRequestParameter("folderId");

        boolean isCH3RelationalElem = false;
        // check if it is a reference element to another vocabulary
        if (StringUtils.isBlank(vocabularyId)) {
            vocabularyId = getContext().getRequestParameter("elemVocabularyId");
            isCH3RelationalElem = StringUtils.isNotBlank(vocabularyId);
        }

        if (!StringUtils.isBlank(vocabularyId)) {
            int folderId = Integer.valueOf(vocabularyId);
            relatedConceptsFilter.setVocabularyFolderId(folderId);
            relatedVocabulary = vocabularyService.getVocabularyFolder(Integer.valueOf(vocabularyId));
        }

        // In case exclude voc set is clicked exclude another vocabulary set
        String excludeVocSetId = getContext().getRequestParameter("excludeVocSetId");
        String excludeVocSetLabel = getContext().getRequestParameter("excludeVocSetLabel");

        if (!StringUtils.isBlank(excludeVocSetId)) {
            excludedVocSetIds.add(Integer.valueOf(excludeVocSetId));
            excludedVocSetLabels.add(excludeVocSetLabel);
        }

        if (excludedVocSetIds.size() > 0) {
            relatedConceptsFilter.setExcludedVocabularySetIds(excludedVocSetIds);
        }

        relatedVocabularyConcepts = vocabularyService.searchVocabularyConcepts(relatedConceptsFilter);

        elementId = getContext().getRequestParameter("elementId");
        editDivId = isCH3RelationalElem ? "addCH3ConceptDiv" : "addConceptDiv";
        initElemVocabularyNames();

        return new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
    }

    /**
     * Adds internal reference concept chosen from popup to the ui.
     *
     * @return stripes resolution
     * @throws ServiceException if error appears
     */
    public Resolution addRelatedConcept() throws ServiceException {
        setConceptIdentifier(vocabularyConcept.getIdentifier());
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), true);

        int conceptId = Integer.valueOf(getContext().getRequestParameter("conceptId"));
        int refElementId = Integer.valueOf(getContext().getRequestParameter("elementId"));

        // vocabularyService.
        List<List<DataElement>> allElements = vocabularyConcept.getElementAttributes();
        List<DataElement> relatedElements = null;

        for (List<DataElement> elems : allElements) {
            DataElement elemMeta = elems.get(0);
            if (elemMeta.getId() == refElementId) {
                relatedElements = elems;
                break;
            }
        }

        if (relatedElements != null) {
            DataElement referenceElement = dataService.getDataElement(refElementId);
            referenceElement.setRelatedConceptId(conceptId);
            setReferenceElementAttrs(referenceElement);
            relatedElements.add(referenceElement);
        }

        // close all popups
        editDivId = null;
        relatedVocabulary = null;
        relatedVocabularyConcepts = null;
        initBeans();
        initElemVocabularyNames();

        return new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
    }

    /**
     * First dialog for searching vocabularies in the adding dialog. - If concept field is filled in the search form display
     * concepts, - if only vocabulary is searched show vocabularies.
     *
     * @return Resolution
     * @throws ServiceException if call fails
     */
    public Resolution searchVocabularies() throws ServiceException {
        setConceptIdentifier(vocabularyConcept.getIdentifier());

        // determine if concept is searched if it is redirect to search vocabularies:
        boolean conceptSearched = vocabularyFilter != null && !StringUtils.isBlank(vocabularyFilter.getConceptText());
        // redirect to step 2 immediately if concept is entered in search dialogue
        if (conceptSearched) {
            if (relatedConceptsFilter == null) {
                relatedConceptsFilter = new VocabularyConceptFilter();
            }

            // something is also entered into vocabulary field
            // TODO make a general FreeTextSearhFilter instead of assigning properties like this
            relatedConceptsFilter.setVocabularyText(vocabularyFilter.getText());
            relatedConceptsFilter.setText(vocabularyFilter.getConceptText());
            relatedConceptsFilter.setExactMatch(vocabularyFilter.isExactMatch());
            relatedConceptsFilter.setWordMatch(vocabularyFilter.isWordMatch());

            // Redirect to search concepts - sorting on table has to use the searchVocabularies
            searchEventName = "searchVocabularies";
            return searchConcepts();

        }

        // concept text is not entered, search vocabularies:
        elementId = getContext().getRequestParameter("elementId");
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), true);

        validateView();
        initBeans();

        if (vocabularyFilter == null) {
            vocabularyFilter = new VocabularyFilter();
        }
        vocabularyFilter.setWorkingCopy(false);
        vocabularyFilter.setVocabularyWorkingCopyId(vocabularyFolder.getId());
        vocabularies = vocabularyService.searchVocabularies(vocabularyFilter);
        editDivId = "findVocabularyDiv";

        return new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
    }

    /**
     * @throws ServiceException
     */
    private void initBeans() throws ServiceException {
        VocabularyConceptFilter filter = new VocabularyConceptFilter();
        filter.setVocabularyFolderId(vocabularyFolder.getId());
        filter.setUsePaging(false);
        filter.setExcludedIds(Collections.singletonList(vocabularyConcept.getId()));
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter).getList();
    }

    /**
     * Validates view action.
     *
     * @throws ServiceException
     */
    private void validateView() throws ServiceException {
        if (vocabularyFolder.isWorkingCopy() || vocabularyFolder.isDraftStatus()) {
            if (getUser() == null) {
                throw new ServiceException("User must be logged in");
            } else {
                if (vocabularyFolder.isWorkingCopy() && !isUserWorkingCopy()) {
                    throw new ServiceException("Illegal user for viewing this working copy");
                }
            }
        }

    }

    /**
     * True, if logged in user is the working user of the vocabulary.
     *
     * @return
     */
    private boolean isUserWorkingCopy() {
        boolean result = false;
        String sessionUser = getUserName();
        if (!StringUtils.isBlank(sessionUser)) {
            if (vocabularyFolder != null) {
                String workingUser = vocabularyFolder.getWorkingUser();
                return vocabularyFolder.isWorkingCopy() && StringUtils.equals(workingUser, sessionUser);
            }
        }

        return result;
    }

    /**
     * True, if user has update right.
     *
     * @return
     */
    private boolean isUpdateRight() {
        if (getUser() != null) {
            return getUser().hasPermission("/vocabularies", "u") || getUser().hasPermission("/vocabularies", "i");
        }
        return false;
    }

    /**
     * Returns concept URI.
     *
     * @return concept uri
     */
    public String getConceptUri() {
        return StringEncoder.encodeToIRI(getUriPrefix() + getConceptIdentifier());
    }

    /**
     * Returns the prefix of the URL for a link to a <em>HTML view</em> of the concept. This must match the @UrlBinding of this
     * class.
     *
     * @return the unescaped URL.
     */
    public String getConceptViewPrefix() {
        return Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabularyconcept/";
    }

    /**
     * Returns concept URI prefix.
     *
     * @return
     */
    public String getUriPrefix() {
        String baseUri = VocabularyFolder.getBaseUri(vocabularyFolder);

        if (!baseUri.endsWith("/") && !baseUri.endsWith("#") && !baseUri.endsWith(":")) {
            baseUri += "/";
        }

        return StringEncoder.encodeToIRI(baseUri);
    }

    /**
     * @return the vocabularyFolder
     */
    public VocabularyFolder getVocabularyFolder() {
        return vocabularyFolder;
    }

    /**
     * @param vocabularyFolder the vocabularyFolder to set
     */
    public void setVocabularyFolder(VocabularyFolder vocabularyFolder) {
        this.vocabularyFolder = vocabularyFolder;
    }

    /**
     * @return the vocabularyConcept
     */
    public VocabularyConcept getVocabularyConcept() {
        return vocabularyConcept;
    }

    /**
     * @param vocabularyConcept the vocabularyConcept to set
     */
    public void setVocabularyConcept(VocabularyConcept vocabularyConcept) {
        this.vocabularyConcept = vocabularyConcept;
    }

    /**
     * @return the vocabularyConcepts
     */
    public List<VocabularyConcept> getVocabularyConcepts() {
        return vocabularyConcepts;
    }

    /**
     * Helper property for concept identifier to make it work properly with tricky charaters: '+' etc.
     *
     * @return vocabularyConcept.identifier
     */
    public String getConceptIdentifier() {
        // return vocabularyConcept.getIdentifier();
        return conceptIdentifier;
    }

    /**
     * Sets concept identifier.
     *
     * @param identifier vocabulary concept identifier
     */
    public void setConceptIdentifier(String identifier) {
        conceptIdentifier = identifier;
    }

    public VocabularyConceptFilter getRelatedConceptsFilter() {
        return relatedConceptsFilter;
    }

    public VocabularyConceptResult getRelatedVocabularyConcepts() {
        return relatedVocabularyConcepts;
    }

    public String getEditDivId() {
        return editDivId;
    }

    public void setEditDivId(String editDivId) {
        this.editDivId = editDivId;
    }

    public void setRelatedConceptsFilter(VocabularyConceptFilter relatedConceptsFilter) {
        this.relatedConceptsFilter = relatedConceptsFilter;
    }

    public VocabularyFilter getVocabularyFilter() {
        return vocabularyFilter;
    }

    public VocabularyResult getVocabularies() {
        return vocabularies;
    }

    public void setVocabularyFilter(VocabularyFilter vocabularyFilter) {
        this.vocabularyFilter = vocabularyFilter;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elemId) {
        elementId = elemId;
    }

    public Set<ContactDetails> getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(Set<ContactDetails> contactDetails) {
        this.contactDetails = contactDetails;
    }

    public String getContactDetailsString() {
        return contactDetailsString;
    }

    public void setContactDetailsString(String contactDetailsString) {
        this.contactDetailsString = contactDetailsString;
    }

    public boolean isUsrLoggedIn() {
        return usrLoggedIn;
    }

    public void setUsrLoggedIn(boolean usrLoggedIn) {
        this.usrLoggedIn = usrLoggedIn;
    }

    /**
     * If validation fails metadata and related elements data has to be added to concept bound elements to make the UI look nice.
     *
     * @throws ServiceException if database query fails
     */
    private void addElementMetadata() throws ServiceException {
        List<List<DataElement>> elementAttributes = vocabularyConcept.getElementAttributes();
        if (elementAttributes != null) {
            for (List<DataElement> elems : elementAttributes) {
                if (elems != null) {
                    // only metainfo has to be added because it is used in the UI
                    DataElement metaInfo = elems.get(0);
                    dataService.setDataElementAttributes(metaInfo);
                    // set relational elements
                    for (DataElement elem : elems) {
                        if (elem.getRelatedConceptId() != null) {
                            setReferenceElementAttrs(elem);
                        }
                    }
                }
            }
        }
    }

    /**
     * gets referenced concept meta from database.
     *
     * @param referenceElement reference elem of type localref or reference
     * @throws ServiceException if error in querying
     */
    private void setReferenceElementAttrs(DataElement referenceElement) throws ServiceException {
        int conceptId = referenceElement.getRelatedConceptId();
        VocabularyConcept relatedConcept = vocabularyService.getVocabularyConcept(conceptId);

        referenceElement.setRelatedConceptId(conceptId);
        referenceElement.setRelatedConceptIdentifier(relatedConcept.getIdentifier());
        referenceElement.setRelatedConceptLabel(relatedConcept.getLabel());

        // referenceElement.setRelatedVocabularyStatus(relatedFolder.getRegStatus().getLabel());
        // referenceElement.setRelatedVocabularyWorkingCopy(relatedFolder.isWorkingCopy());

    }

    public VocabularyFolder getRelatedVocabulary() {
        return relatedVocabulary;
    }

    public String getSearchEventName() {
        return searchEventName;
    }

    public List<Integer> getExcludedVocSetIds() {
        // return excludedVocSetIds == null ? "" : excludedVocSetIds;
        return excludedVocSetIds;
    }

    public void setExcludedVocSetIds(List<Integer> excludedVocSetIds) {
        this.excludedVocSetIds = excludedVocSetIds;
    }

    public List<String> getExcludedVocSetLabels() {
        return excludedVocSetLabels;
    }

    public void setExcludedVocSetLabels(List<String> excludedVocSetLabels) {
        this.excludedVocSetLabels = excludedVocSetLabels;
    }

    /**
     * initializes filters and other helper variables.
     */
    private void initSearchFilters() {
        // related concepts
        if (relatedConceptsFilter == null) {
            relatedConceptsFilter = new VocabularyConceptFilter();
        }

        if (excludedVocSetIds == null) {
            excludedVocSetIds = new ArrayList<Integer>();
        }

        if (excludedVocSetLabels == null) {
            excludedVocSetLabels = new ArrayList<String>();
        }

    }

    public List<String> getElemVocabularyNames() {
        return elemVocabularyNames;
    }

    /**
     * insert vocabulary labels of ch3 elments to the special list.
     *
     * @throws eionet.meta.service.ServiceException if query vocabulary fails
     */
    private void initElemVocabularyNames() throws ServiceException {
        elemVocabularyNames = new ArrayList<String>();
        List<List<DataElement>> elementAttributes = vocabularyConcept.getElementAttributes();
        if (elementAttributes != null) {
            for (List<DataElement> elems : elementAttributes) {
                if (elems != null) {
                    DataElement elemMeta = elems.get(0);
                    String vocName = "";
                    if (elemMeta.getType() != null && elemMeta.getType().equals("CH3")) {
                        int vocabularyId = elemMeta.getVocabularyId();
                        vocName = vocabularyService.getVocabularyFolder(vocabularyId).getLabel();
                    }
                    elemVocabularyNames.add(vocName);
                }
            }
        }
    }

    private List<Integer> getExcludedIdsOfOriginalFolder() throws ServiceException {
        List<Integer> ids = new ArrayList<Integer>();
        //NB this method returns ORIGINAL vocabulary that we need here
        List<VocabularyConcept> concepts = vocabularyService.getVocabularyWithConcepts(vocabularyFolder.getIdentifier(),
                vocabularyFolder.getFolderName()).getConcepts();

        if (concepts != null) {
            for (VocabularyConcept c : concepts) {
                ids.add(c.getId());
            }
        }
        return ids;
    }


}
