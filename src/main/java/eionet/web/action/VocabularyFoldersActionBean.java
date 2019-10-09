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
 * The Original Code is Data Dictionary.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.action;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptData;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Util;

/**
 * Action bean for listing vocabulary folders.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabularies")
public class VocabularyFoldersActionBean extends AbstractActionBean {

    /** Page path. */
    private static final String BROWSE_VOCABULARY_FOLDERS_JSP = "/pages/vocabularies/browseVocabularyFolders.jsp";

    /** Vocabulary search results page. */
    private static final String VOCABULARY_SEARCH_RESULT_JSP = "/pages/vocabularies/vocabularyResult.jsp";

    /** Vocabulary concept search results page. */
    private static final String CONCEPT_SEARCH_RESULT_JSP = "/pages/vocabularies/vocabularyConceptResult.jsp";

    /** Vocabularies maintenance page. */
    private static final String VOCABULARIES_MAINTENANCE_JSP = "/pages/vocabularies/vocabulariesMaintenance.jsp";
    
    private static final String VIEW_VOCABULARY_FOR_SELECTION_JSP = "/pages/attributes/selectVocabulary.jsp";

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** data service. */
    @SpringBean
    private IDataService dataService;

    /** Folders. */
    private List<Folder> folders;

    /** Selected vocabulary folder ids. */
    private List<Integer> folderIds;

    /** Folder numeric ID, currently clicked. */
    private int folderId;

    /** Folder Identifier, currently clicked. */
    private String identifier;

    /** True, if operation is to expand. To collapse, it is false. */
    private boolean expand;

    /** Comma separated folder IDs, that are expanded. */
    private String expanded;

    /** The page contains visible editable vocabularies. */
    private boolean visibleEditableVocabularies;

    /** Popup div id to keep open, when validation error occur. */
    private String editDivId;

    /**
     * Vocabularies search result.
     */
    private VocabularyResult vocabularyResult;

    /**
     * Vocabulary concepts search result.
     */
    private List<VocabularyConceptData> vocabularyConceptResult;

    /**
     * Vocabularies search filter.
     */
    private VocabularyFilter vocabularyFilter;

    /**
     * Search concepts filter.
     */
    private VocabularyConceptFilter vocabularyConceptFilter;

    /**
     * vocabulary IDs that have base uri specified.
     */
    private List<Integer> vocabulariesWithBaseUri;

    /**
     * if true and vocabularies are deleted then relation in another vocabulary is deleted but element value is replace by base url
     * + identifier.
     */
    private boolean keepRelationsOnDelete;

    /**
     * List of vocabulary status texts to be displayed in the list of vocabularies after vocabulary name. Released status is the
     * normal, and show the status only when it is different from the normal.
     */
    private RegStatus[] statusTextsToDisplay = {RegStatus.DRAFT, RegStatus.PUBLIC_DRAFT};
    /**
     * Old site prefix.
     */
    private String oldSitePrefix = null;
    /**
     * New site prefix.
     */
    private String newSitePrefix = null;

    /**
     * View vocabulary folders list action.
     *
     * @return Default Resolution.
     * @throws ServiceException
     *             if retrieving folder data fails.
     */
    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        folders = vocabularyService.getFolders(getUserName(), parseExpandedIds());
        vocabulariesWithBaseUri = new ArrayList<Integer>();

        if (getUserName() != null && folders != null) {
            for (Folder folder : folders) {
                if (folder.isExpanded() && folder.getItems() != null) {
                    for (Object vocabulary : folder.getItems()) {
                        if (vocabulary instanceof VocabularyFolder && ((VocabularyFolder) vocabulary).getBaseUri() != null) {
                            vocabulariesWithBaseUri.add(((VocabularyFolder) vocabulary).getId());
                        }
                        if (vocabulary instanceof VocabularyFolder && !((VocabularyFolder) vocabulary).isWorkingCopy()
                                && StringUtils.isEmpty(((VocabularyFolder) vocabulary).getWorkingUser())) {
                            setVisibleEditableVocabularies(true);
                        }
                    }
                }
            }

            // base URI:
            for (Folder folder : folders) {
                if (folder.isExpanded() && folder.getItems() != null) {
                    for (Object vocabulary : folder.getItems()) {
                        if (vocabulary instanceof VocabularyFolder && ((VocabularyFolder) vocabulary).getBaseUri() != null) {
                            vocabulariesWithBaseUri.add(((VocabularyFolder) vocabulary).getId());
                        }
                    }
                }
            }

        }
        return new ForwardResolution(BROWSE_VOCABULARY_FOLDERS_JSP);
    }

    /**
     * Action for updating folder.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution saveFolder() throws ServiceException {
        LOGGER.debug("Saving folder: " + getSubmittedFolder().getIdentifier());
        vocabularyService.updateFolder(getSubmittedFolder());
        addSystemMessage("Folder successfully updated");
        return new RedirectResolution(VocabularyFoldersActionBean.class);
    }

    /**
     * Action for deleting folder.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution deleteFolder() throws ServiceException {
        LOGGER.debug("Deleting folder: " + getSubmittedFolder().getIdentifier());
        vocabularyService.deleteFolder(getSubmittedFolder().getId());
        addSystemMessage("Folder successfully deleted");
        return new RedirectResolution(VocabularyFoldersActionBean.class);
    }

    /**
     * Action for maintaining folders.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution maintain() throws ServiceException {
        // TODO future enchancement: folderIds can be used for selection based updates.
        // i.e. page can have, update all, update selected, update exclusion of selected
        return new ForwardResolution(VOCABULARIES_MAINTENANCE_JSP);
    }

    /**
     * Validation on search concepts. Checks if text is entered
     *
     * @throws ServiceException
     *             if databaes call fails
     */
    @ValidationMethod(on = {"searchConcepts"})
    public void validateSearchConcepts() throws ServiceException {
        if (vocabularyConceptFilter == null || StringUtils.isEmpty(vocabularyConceptFilter.getText())) {
            addGlobalValidationError("Search text cannot be empty.");
        } else if (vocabularyConceptFilter != null && !StringUtils.isEmpty(vocabularyConceptFilter.getText())
                && vocabularyConceptFilter.getText().length() < 2) {
            addGlobalValidationError("Search text must be at least two characters.");
        }

        if (isValidationErrors()) {
            folders = vocabularyService.getFolders(getUserName(), null);
        }
    }

    /**
     * Validation on maintenance.
     *
     * @throws ServiceException
     *             if operation fails
     */
    @ValidationMethod(on = {"maintain", "populate", "changeSitePrefix"})
    public void validateMaintain() throws ServiceException {
        if (!isUpdateRight()) {
            addGlobalValidationError("No permission to modify folders");
        }
    } // end of method validateMaintain

    /**
     * Validates save folder.
     *
     * @throws ServiceException
     *             if user does not have update rights.
     */
    @ValidationMethod(on = {"saveFolder"})
    public void validateSaveFolder() throws ServiceException {
        if (!isUpdateRight()) {
            addGlobalValidationError("No permission to modify folder");
        }

        Folder folder = getSubmittedFolder();

        if (StringUtils.isEmpty(folder.getIdentifier())) {
            addGlobalValidationError("Folder identifier is missing");
        }

        if (StringUtils.isEmpty(folder.getLabel())) {
            addGlobalValidationError("Folder label is missing");
        }

        if (!vocabularyService.isUniqueFolderIdentifier(folder.getIdentifier(), folder.getId())) {
            addGlobalValidationError("Folder identifier is not unique");
        }

        if (isValidationErrors()) {
            editDivId = "editFolderDiv" + folder.getId();
            folders = vocabularyService.getFolders(getUserName(), null);
        }
    }

    /**
     * Validates delete folder.
     *
     * @throws ServiceException
     *             if user does not have delete rights.
     */
    @ValidationMethod(on = {"deleteFolder"})
    public void validateDeleteFolder() throws ServiceException {
        if (!isDeleteRight()) {
            addGlobalValidationError("No permission to delete folder!");
        }

        Folder folder = getSubmittedFolder();

        if (!vocabularyService.isFolderEmpty(folder.getId())) {
            addGlobalValidationError("Cannot delete, folder is not empty");
        }

        if (isValidationErrors()) {
            editDivId = "editFolderDiv" + folder.getId();
            folders = vocabularyService.getFolders(getUserName(), null);
        }
    }

    /**
     * Validates delete vocabulary.
     *
     * @throws ServiceException
     *             if user does not have delete rights.
     */
    @ValidationMethod(on = {"delete"})
    public void validateDeleteVocabulary() throws ServiceException {
        if (!isDeleteRight()) {
            addGlobalValidationError("No permission to delete vocabulary!");
        }

        // if vocabulary is used in CH3 element - cannot delete
        List<DataElement> elementsAsSoruce = dataService.getVocabularySourceElements(folderIds);
        if (elementsAsSoruce.size() > 0) {
            addGlobalValidationError("Deleted vocabularies are used as values source for data elements: "
                    + StringUtils.join(elementsAsSoruce, ","));
        }

        if (isValidationErrors()) {
            folders = vocabularyService.getFolders(getUserName(), null);
        }
    }

    /**
     * True, if user has update right.
     *
     * @return
     */
    public boolean isUpdateRight() {
        if (getUser() != null) {
            return getUser().hasPermission("/vocabularies", "u") || getUser().hasPermission("/vocabularies", "i");
        }
        return false;
    }

    /**
     * True, if user has delete right.
     *
     * @return
     */
    public boolean isDeleteRight() {
        if (getUser() != null) {
            return getUser().hasPermission("/vocabularies", "d");
        }
        return false;
    }

    /**
     * Returns the expanded folder IDs and sets the correct expanded[] value.
     *
     * @return
     */
    private int[] parseExpandedIds() {
        List<Integer> result = new ArrayList<Integer>();

        if (StringUtils.isNotEmpty(expanded)) {
            String[] expandedStrArr = StringUtils.split(expanded, ",");
            for (String s : expandedStrArr) {
                result.add(Integer.parseInt(s));
            }
        }
        if (expand) {
            result.add(folderId);
        } else {
            result.remove(Integer.valueOf(folderId));
        }

        expanded = StringUtils.join(result, ",");

        return ArrayUtils.toPrimitive(result.toArray(new Integer[result.size()]));
    }

    /**
     * Returns the folder that is submitted by form for update.
     *
     * @return
     */
    public Folder getSubmittedFolder() {
        for (Folder f : folders) {
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    /**
     * Deletes vocabulary folders.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution delete() throws ServiceException {
        vocabularyService.deleteVocabularyFolders(folderIds, keepRelationsOnDelete);
        addSystemMessage("Vocabularies deleted successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFoldersActionBean.class);
        return resolution;
    }

    /**
     * Populates empty base uris.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution populate() throws ServiceException {
        String sitePrefix = getSitePrefix();
        if (!sitePrefix.endsWith("/")) {
            sitePrefix += "/";
        }
        int numberOfRows = vocabularyService.populateEmptyBaseUris(sitePrefix);
        addSystemMessage("Empty base URIs are populated. " + numberOfRows + " vocabularies updated.");
        RedirectResolution resolution = new RedirectResolution(VocabularyFoldersActionBean.class, "maintain");
        return resolution;
    } // end of method populate

    /**
     * Changes site prefix for base uris.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution changeSitePrefix() throws ServiceException {
        int numberOfRows = vocabularyService.changeSitePrefix(oldSitePrefix, newSitePrefix);
        addSystemMessage("Site prefix changed. " + numberOfRows + " vocabularies were updated.");
        addSystemMessage("\"" + oldSitePrefix + "\" replaced by \"" + newSitePrefix + "\"");
        RedirectResolution resolution = new RedirectResolution(VocabularyFoldersActionBean.class, "maintain");
        return resolution;
    } // end of method changeSitePrefix

    /**
     * search vocabulary folders.
     *
     * @return Stripes resolution
     * @throws ServiceException
     *             if search fails
     */
    public Resolution search() throws ServiceException {
        if (vocabularyFilter == null) {
            vocabularyFilter = new VocabularyFilter();
        }
        // do not show working copies for anonymous users
        if (!isUserLoggedIn()) {
            vocabularyFilter.setWorkingCopy(false);
        }

        vocabularyResult = vocabularyService.searchVocabularies(vocabularyFilter);

        return new ForwardResolution(VOCABULARY_SEARCH_RESULT_JSP);
    }

    /**
     * search concepts folders.
     *
     * @return Stripes resolution
     * @throws ServiceException
     *             if search fails
     */
    public Resolution searchConcepts() throws ServiceException {
        if (vocabularyConceptFilter == null) {
            vocabularyConceptFilter = new VocabularyConceptFilter();
        }
        // this is needed because of "limit " clause in the SQL. if this remains true, paging does not work in display:table
        vocabularyConceptFilter.setUsePaging(false);

        // do not show working copies for anonymous users
        vocabularyConceptResult = vocabularyService.searchAllVocabularyConcept(vocabularyConceptFilter);

        return new ForwardResolution(CONCEPT_SEARCH_RESULT_JSP);
    }

    /**
     * Validates changing site prefix.
     *
     * @throws ServiceException
     *             if an error occurs
     */
    @ValidationMethod(on = {"changeSitePrefix"})
    public void validateChangeSitePrefix() throws ServiceException {
        if (StringUtils.isBlank(newSitePrefix)) {
            addGlobalValidationError("New Site Prefix is missing");
        } else if (!Util.isValidUri(newSitePrefix)) {
            addGlobalValidationError("New Site prefix is not a valid URI. \n The allowed schemes are: "
                    + "http, https, ftp, mailto, tel and urn.");
        } else if (!StringUtils.endsWith(newSitePrefix, "/")) {
            newSitePrefix += "/";
        }

        if (StringUtils.isBlank(oldSitePrefix)) {
            addGlobalValidationError("Old Site Prefix is missing");
        } else if (!Util.isValidUri(oldSitePrefix)) {
            addGlobalValidationError("Old Site prefix is not a valid URI. \n The allowed schemes are: "
                    + "http, https, ftp, mailto, tel and urn.");
        } else if (!StringUtils.endsWith(oldSitePrefix, "/")) {
            oldSitePrefix += "/";
        }

        if (StringUtils.equals(oldSitePrefix, newSitePrefix)) {
            addGlobalValidationError("Old and New Site Prefixes are the same.");
        }
    } // end of method validateChangeSitePrefix

    /**
     * @return the folderIds
     */
    public List<Integer> getFolderIds() {
        return folderIds;
    }

    /**
     * @param folderIds
     *            the folderIds to set
     */
    public void setFolderIds(List<Integer> folderIds) {
        this.folderIds = folderIds;
    }

    /**
     * @return the folderId
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * @param folderId
     *            the folderId to set
     */
    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    /**
     * @return the expand
     */
    public boolean isExpand() {
        return expand;
    }

    /**
     * @param expand
     *            the expand to set
     */
    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    /**
     * @return the expanded
     */
    public String getExpanded() {
        return expanded;
    }

    /**
     * @param expanded
     *            the expanded to set
     */
    public void setExpanded(String expanded) {
        this.expanded = expanded;
    }

    /**
     * @return the folders
     */
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * @param folders
     *            the folders to set
     */
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * @return the editDivId
     */
    public String getEditDivId() {
        return editDivId;
    }

    /**
     * @param editDivId
     *            the editDivId to set
     */
    public void setEditDivId(String editDivId) {
        this.editDivId = editDivId;
    }

    /**
     * @return the visibleEditableVocabularies
     */
    public boolean isVisibleEditableVocabularies() {
        return visibleEditableVocabularies;
    }

    /**
     * @param visibleEditableVocabularies
     *            the visibleEditableVocabularies to set
     */
    public void setVisibleEditableVocabularies(boolean visibleEditableVocabularies) {
        this.visibleEditableVocabularies = visibleEditableVocabularies;
    }

    /**
     * @return the folderIdentifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param folderIdentifier
     *            the folderIdentifier to set
     */
    public void setIdentifier(String folderIdentifier) {
        this.identifier = folderIdentifier;
    }

    public VocabularyResult getVocabularyResult() {
        return vocabularyResult;
    }

    public VocabularyFilter getVocabularyFilter() {
        return vocabularyFilter;
    }

    public void setVocabularyFilter(VocabularyFilter vocabularyFilter) {
        this.vocabularyFilter = vocabularyFilter;
    }

    public VocabularyConceptFilter getVocabularyConceptFilter() {
        return vocabularyConceptFilter;
    }

    public void setVocabularyConceptFilter(VocabularyConceptFilter vocabularyConceptFilter) {
        this.vocabularyConceptFilter = vocabularyConceptFilter;
    }

    public List<VocabularyConceptData> getVocabularyConceptResult() {
        return vocabularyConceptResult;
    }

    public List<Integer> getVocabulariesWithBaseUri() {
        return vocabulariesWithBaseUri;
    }

    public boolean isKeepRelationsOnDelete() {
        return keepRelationsOnDelete;
    }

    public void setKeepRelationsOnDelete(boolean keepRelationsOnDelete) {
        this.keepRelationsOnDelete = keepRelationsOnDelete;
    }

    /**
     * @return the statusTextsToDisplay
     */
    public RegStatus[] getStatusTextsToDisplay() {
        return statusTextsToDisplay;
    }

    public String getOldSitePrefix() {
        return oldSitePrefix;
    }

    public void setOldSitePrefix(String oldSitePrefix) {
        this.oldSitePrefix = StringUtils.trimToNull(oldSitePrefix);
    }

    /**
     * Returns site prefix.
     *
     * @return new site prefix or default
     */
    public String getNewSitePrefix() {
        if (StringUtils.isEmpty(newSitePrefix)) {
            return getSitePrefix();
        }
        return newSitePrefix;
    }

    public void setNewSitePrefix(String newSitePrefix) {
        this.newSitePrefix = StringUtils.trimToNull(newSitePrefix);
    }
    
    private String attrId;
    
    public String getAttrId() {
        return this.attrId;
    }
    
    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }
    
    public Resolution selectVocabulary() throws ServiceException {
        folders = vocabularyService.getFolders(getUserName(), parseExpandedIds());
        vocabulariesWithBaseUri = new ArrayList<Integer>();

        for (Folder folder : folders) {
            if (folder.isExpanded() && folder.getItems() != null) {
                for (Object vocabulary : folder.getItems()) {
                    if (vocabulary instanceof VocabularyFolder && ((VocabularyFolder) vocabulary).getBaseUri() != null) {
                        vocabulariesWithBaseUri.add(((VocabularyFolder) vocabulary).getId());
                    }
                    if (vocabulary instanceof VocabularyFolder && !((VocabularyFolder) vocabulary).isWorkingCopy()
                            && StringUtils.isEmpty(((VocabularyFolder) vocabulary).getWorkingUser())) {
                        setVisibleEditableVocabularies(true);
                    }
                }
            }
        }
        return new ForwardResolution(VIEW_VOCABULARY_FOR_SELECTION_JSP);
    }

}
