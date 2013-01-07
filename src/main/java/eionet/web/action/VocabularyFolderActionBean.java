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

package eionet.web.action;

import java.net.HttpURLConnection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/**
 * Edit vocabulary folder action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabulary/{vocabularyFolder.identifier}/{$event}")
public class VocabularyFolderActionBean extends AbstractActionBean {

    /** JSP pages. */
    private static final String ADD_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/addVocabularyFolder.jsp";
    private static final String EDIT_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/editVocabularyFolder.jsp";
    private static final String VIEW_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/viewVocabularyFolder.jsp";

    /** Popup div's id prefix on jsp page. */
    private static final String EDIT_DIV_ID_PREFIX = "editConceptDiv";
    /** Pop div's id for new concept form. */
    private static final String NEW_CONCEPT_DIV_ID = "addNewConceptDiv";

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Vocabulary folder. */
    private VocabularyFolder vocabularyFolder;

    /** Other versions of the same vocabulary folder. */
    private List<VocabularyFolder> vocabularyFolderVersions;

    /** Vocabulary concepts. */
    private VocabularyConceptResult vocabularyConcepts;

    /** Vocabulary concept to add/edit. */
    private VocabularyConcept vocabularyConcept;

    /** Selected vocabulary concept ids. */
    private List<Integer> conceptIds;

    /** Vocabulary folder id, from which the copy is made of. */
    private int copyId;

    /** Popup div id to keep open, when validation error occur. */
    private String editDivId;

    /** Vocabulary concept filter. */
    private VocabularyConceptFilter filter;

    /** Concepts table page number. */
    private int page = 1;

    /** Amount of concepts to reserve. */
    private int amount;

    /** The starting identifier for reserving new site codes. */
    private int startIdentifier;

    /**
     * Navigates to view vocabulary folder page.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), vocabularyFolder.isWorkingCopy());
        initFilter();
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        vocabularyFolderVersions =
                vocabularyService.getVocabularyFolderVersions(vocabularyFolder.getContinuityId(), vocabularyFolder.getId(),
                        getUserName());
        return new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP);
    }

    public Resolution search() throws ServiceException {
        LOGGER.debug("Serching");
        return new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Navigates to view vocabulary's working copy page.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution viewWorkingCopy() throws ServiceException {
        vocabularyFolder = vocabularyService.getVocabularyWorkingCopy(vocabularyFolder.getId());
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Navigates to add vocabulary folder form.
     *
     * @return
     */
    public Resolution add() {
        return new ForwardResolution(ADD_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Navigates to edit vocabulary folder form.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution edit() throws ServiceException {
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), vocabularyFolder.isWorkingCopy());
        initFilter();
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        startIdentifier = vocabularyService.getNextIdentifierValue(vocabularyFolder.getId());
        return new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Action that reserves free site codes (empty vocabulary concepts).
     *
     * @return
     * @throws ServiceException
     */
    public Resolution reserveFreeSiteCodes() throws ServiceException {

        vocabularyService.reserveFreeSiteCodes(vocabularyFolder.getId(), amount, startIdentifier);

        addSystemMessage("Free site codes successfully created");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Save vocabulary folder action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveFolder() throws ServiceException {
        if (vocabularyFolder.getId() == 0) {
            if (copyId != 0) {
                vocabularyService.createVocabularyFolderCopy(vocabularyFolder, copyId, getUserName());
            } else {
                vocabularyService.createVocabularyFolder(vocabularyFolder, getUserName());
            }
        } else {
            vocabularyService.updateVocabularyFolder(vocabularyFolder);
        }
        addSystemMessage("Vocabulary saved successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Save vocabulary concept action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveConcept() throws ServiceException {

        if (vocabularyConcept != null) {
            // Save new concept
            vocabularyService.createVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept);
        } else {
            // Update existing concept
            vocabularyService.updateVocabularyConcept(getEditableConcept());
        }

        addSystemMessage("Vocabulary concept saved successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Action for checking in vocabulary folder.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution checkIn() throws ServiceException {
        vocabularyService.checkInVocabularyFolder(vocabularyFolder.getId(), getUserName());
        addSystemMessage("Successfully checked in");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", false);
        return resolution;
    }

    /**
     * Action for checking out vocabulary folder.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution checkOut() throws ServiceException {
        vocabularyService.checkOutVocabularyFolder(vocabularyFolder.getId(), getUserName());
        addSystemMessage("Successfully checked out");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", true);
        return resolution;
    }

    /**
     * Deletes the checked out version.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution undoCheckOut() throws ServiceException {
        vocabularyService.undoCheckOut(vocabularyFolder.getId(), getUserName());
        addSystemMessage("Checked out version successfully deleted");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", false);
        return resolution;
    }

    /**
     * Deletes vocabulary concepts.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution deleteConcepts() throws ServiceException {
        vocabularyService.deleteVocabularyConcepts(conceptIds);
        addSystemMessage("Vocabulary concepts deleted successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Validates save concept.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"reserveFreeSiteCodes"})
    public void validateReserveFreeSiteCodes() throws ServiceException {
        if (amount < 1) {
            addGlobalValidationError("Amount must be a positive number");
        }

        if (amount > 1000) {
            addGlobalValidationError("Amount cannot be bigger than 1000");
        }

        List<Integer> unavailableIdentifiers = vocabularyService.checkAvailableIdentifiers(vocabularyFolder.getId(), amount, startIdentifier);
        if (unavailableIdentifiers.size() > 0) {
            addGlobalValidationError("Identifers are unavailaible: " + StringUtils.join(unavailableIdentifiers, ", "));
        }

        if (isValidationErrors()) {
            vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getId());
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        }
    }

    /**
     * Validates save folder.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"saveFolder"})
    public void validateSaveFolder() throws ServiceException {

        if (getUser() == null) {
            addGlobalValidationError("User must be logged in");
        }

        if (StringUtils.isEmpty(vocabularyFolder.getIdentifier())) {
            addGlobalValidationError("Vocabulary identifier is missing");
        } else {
            if (!Util.isValidIdentifier(vocabularyFolder.getIdentifier())) {
                addGlobalValidationError("Vocabulary identifier must be alpha-numeric value");
            }
        }
        if (StringUtils.isEmpty(vocabularyFolder.getLabel())) {
            addGlobalValidationError("Vocabulary label is missing");
        }

        if (StringUtils.isNotEmpty(vocabularyFolder.getBaseUri())) {
            if (!Util.isURI(vocabularyFolder.getBaseUri())) {
                addGlobalValidationError("Base URI contains illegal characters");
            }
        }

        if (vocabularyFolder.isSiteCodeType() && !vocabularyFolder.isNumericConceptIdentifiers()) {
            addGlobalValidationError("Site code type vocabulary must have numeric concept identifiers");
        }

        // Validate unique identifier
        if (vocabularyFolder.getId() == 0) {
            if (!vocabularyService.isUniqueFolderIdentifier(vocabularyFolder.getIdentifier())) {
                addGlobalValidationError("Vocabulary identifier is not unique");
            }
        } else {
            if (!vocabularyService.isUniqueFolderIdentifier(vocabularyFolder.getIdentifier(), vocabularyFolder.getId(),
                    vocabularyFolder.getCheckedOutCopyId())) {
                addGlobalValidationError("Vocabulary identifier is not unique");
            }
        }

        if (isValidationErrors()) {
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        }
    }

    /**
     * Validates save concept.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"saveConcept"})
    public void validateSaveConcept() throws ServiceException {
        VocabularyConcept vc = null;
        if (vocabularyConcept != null) {
            // Validating new concept
            vc = vocabularyConcept;
            editDivId = NEW_CONCEPT_DIV_ID;
        } else {
            // Validating edit concept
            vc = getEditableConcept();
            editDivId = EDIT_DIV_ID_PREFIX + vc.getId();
        }

        if (StringUtils.isEmpty(vc.getIdentifier())) {
            addGlobalValidationError("Vocabulary concept identifier is missing");
        } else {
            if (vocabularyFolder.isNumericConceptIdentifiers()) {
                if (!Util.isNumericID(vc.getIdentifier())) {
                    addGlobalValidationError("Vocabulary concept identifier must be numeric value");
                }
            } else {
                if (!Util.isValidIdentifier(vc.getIdentifier())) {
                    addGlobalValidationError("Vocabulary concept identifier must be alpha-numeric value");
                }
            }
        }
        if (StringUtils.isEmpty(vc.getLabel())) {
            addGlobalValidationError("Vocabulary concept label is missing");
        }

        // Validate unique identifier
        if (!vocabularyService.isUniqueConceptIdentifier(vc.getIdentifier(), vocabularyFolder.getId(), vc.getId())) {
            addGlobalValidationError("Vocabulary concept identifier is not unique");
        }

        if (isValidationErrors()) {
            vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getId());
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        }
    }

    /**
     * Navigates to vocabulary folders list.
     *
     * @return
     */
    public Resolution cancelAdd() {
        return new RedirectResolution(VocabularyFoldersActionBean.class);
    }

    /**
     * Navigates to edit vocabulary folder page.
     *
     * @return
     */
    public Resolution cancelSave() {
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Action, that returns RDF output of the vocabulary.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution rdf() {
        try {
            vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), false);
            initFilter();
            filter.setUsePaging(false);
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);

            if (vocabularyFolder.isDraftStatus()) {
                throw new RuntimeException("Vocabulary is not in released or public draft status.");
            }

            final String contextRoot =
                    StringUtils.isNotEmpty(vocabularyFolder.getBaseUri()) ? vocabularyFolder.getBaseUri() : Props
                            .getRequiredProperty(PropsIF.DD_URL) + "/vocabularies/" + vocabularyFolder.getIdentifier() + "/";

            StreamingResolution result = new StreamingResolution("application/rdf+xml") {
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyXmlWriter xmlWriter =
                            new VocabularyXmlWriter(response.getOutputStream(), contextRoot, vocabularyFolder,
                                    vocabularyConcepts.getList());
                    xmlWriter.writeManifestXml();
                }
            };
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary RDF data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

    /**
     * Initiates filter correct with parameters.
     */
    private void initFilter() {
        if (filter == null) {
            filter = new VocabularyConceptFilter();
        }
        filter.setVocabularyFolderId(vocabularyFolder.getId());
        filter.setPageNumber(page);
    }

    /**
     * True, if logged in user is the working user of the vocabulary.
     *
     * @return
     */
    public boolean isUserWorkingCopy() {
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
     * True, if vocabulary is checked out by other user.
     *
     * @return
     */
    public boolean isCheckedOutByOther() {

        if (vocabularyFolder == null) {
            return false;
        } else {
            return StringUtils.isNotBlank(vocabularyFolder.getWorkingUser()) && !vocabularyFolder.isWorkingCopy()
                    && !StringUtils.equals(getUserName(), vocabularyFolder.getWorkingUser());
        }
    }

    /**
     * True, if vocabulary is checked out by user.
     *
     * @return
     */
    public boolean isCheckedOutByUser() {

        if (vocabularyFolder == null) {
            return false;
        } else {
            return StringUtils.isNotBlank(vocabularyFolder.getWorkingUser()) && !vocabularyFolder.isWorkingCopy()
                    && StringUtils.equals(getUserName(), vocabularyFolder.getWorkingUser());
        }
    }

    /**
     * Returns autogenerated identifier for new concept. Empty string if VocabularyFolder.numericConceptIdentifiers=false.
     *
     * @return
     */
    public String getNextIdentifier() {
        if (!vocabularyFolder.isNumericConceptIdentifiers()) {
            return "";
        } else {
            try {
                int identifier = vocabularyService.getNextIdentifierValue(vocabularyFolder.getId());
                return Integer.toString(identifier);
            } catch (ServiceException e) {
                LOGGER.error(e);
                return "";
            }
        }
    }

    /**
     * Returns the vocabulary concept that is submitted by form for update.
     *
     * @return
     */
    public VocabularyConcept getEditableConcept() {
        for (VocabularyConcept vc : vocabularyConcepts.getList()) {
            if (vc != null) {
                return vc;
            }
        }
        return null;
    }

    /**
     * @return the vocabularyFolder
     */
    public VocabularyFolder getVocabularyFolder() {
        return vocabularyFolder;
    }

    /**
     * @param vocabularyFolder
     *            the vocabularyFolder to set
     */
    public void setVocabularyFolder(VocabularyFolder vocabularyFolder) {
        this.vocabularyFolder = vocabularyFolder;
    }

    /**
     * @return the vocabularyConcepts
     */
    public VocabularyConceptResult getVocabularyConcepts() {
        return vocabularyConcepts;
    }

    /**
     * @param vocabularyConcepts
     *            the vocabularyConcepts to set
     */
    public void setVocabularyConcepts(VocabularyConceptResult vocabularyConcepts) {
        this.vocabularyConcepts = vocabularyConcepts;
    }

    /**
     * @param vocabularyService
     *            the vocabularyService to set
     */
    public void setVocabularyService(IVocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    /**
     * @return the vocabularyConcept
     */
    public VocabularyConcept getVocabularyConcept() {
        return vocabularyConcept;
    }

    /**
     * @param vocabularyConcept
     *            the vocabularyConcept to set
     */
    public void setVocabularyConcept(VocabularyConcept vocabularyConcept) {
        this.vocabularyConcept = vocabularyConcept;
    }

    /**
     * @return the conceptIds
     */
    public List<Integer> getConceptIds() {
        return conceptIds;
    }

    /**
     * @param conceptIds
     *            the conceptIds to set
     */
    public void setConceptIds(List<Integer> conceptIds) {
        this.conceptIds = conceptIds;
    }

    /**
     * @return the copyId
     */
    public int getCopyId() {
        return copyId;
    }

    /**
     * @param copyId
     *            the copyId to set
     */
    public void setCopyId(int copyId) {
        this.copyId = copyId;
    }

    /**
     * @return the vocabularyFolderVersions
     */
    public List<VocabularyFolder> getVocabularyFolderVersions() {
        return vocabularyFolderVersions;
    }

    /**
     * @return the editDivId
     */
    public String getEditDivId() {
        return editDivId;
    }

    /**
     * @return the filter
     */
    public VocabularyConceptFilter getFilter() {
        return filter;
    }

    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter(VocabularyConceptFilter filter) {
        this.filter = filter;
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     *            the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @param amount
     *            the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * @return the startIdentifier
     */
    public int getStartIdentifier() {
        return startIdentifier;
    }

    /**
     * @param startIdentifier
     *            the startIdentifier to set
     */
    public void setStartIdentifier(int startIdentifier) {
        this.startIdentifier = startIdentifier;
    }

}
