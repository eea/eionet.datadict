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

import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;

/**
 * Action bean for listing vocabulary folders.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabularies")
public class VocabularyFoldersActionBean extends AbstractActionBean {

    /** Page path. */
    private static final String BROWSE_VOCABULARY_FOLDERS_JSP = "/pages/vocabularies/browseVocabularyFolders.jsp";

    /** Search results page. */
    private static final String VOCABULARY_SEARCH_RESULT_JSP = "/pages/vocabularies/vocabularyResult.jsp";


    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

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
     * Vocabularies search filter.
     */
    private VocabularyFilter vocabularyFilter;


    /**
     * View vocabulary folders list action.
     *
     * @return Default Resolution.
     * @throws ServiceException if retrieving folder data fails.
     */
    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        folders = vocabularyService.getFolders(getUserName(), parseExpandedIds());

        if (getUserName() != null && folders != null) {
            for (Folder folder : folders) {
                if (folder.isExpanded() && folder.getItems() != null) {
                    for (Object vocabulary : folder.getItems()) {
                        if (vocabulary instanceof VocabularyFolder
                                && !((VocabularyFolder) vocabulary).isWorkingCopy()
                                    && StringUtils.isEmpty(((VocabularyFolder) vocabulary).getWorkingUser())) {
                            setVisibleEditableVocabularies(true);
                            break;
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
     * @return
     * @throws ServiceException
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
     * @return
     * @throws ServiceException
     */
    public Resolution deleteFolder() throws ServiceException {
        LOGGER.debug("Deleting folder: " + getSubmittedFolder().getIdentifier());
        vocabularyService.deleteFolder(getSubmittedFolder().getId());
        addSystemMessage("Folder successfully deleted");
        return new RedirectResolution(VocabularyFoldersActionBean.class);
    }

    /**
     * Validates save folder.
     *
     * @throws ServiceException if user does not have update rights.
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
     * @throws ServiceException if user does not have delete rights.
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
     * @throws ServiceException if user does not have delete rights.
     */
    @ValidationMethod(on = {"delete"})
    public void validateDeleteVocabulary() throws ServiceException {
        if (!isDeleteRight()) {
            addGlobalValidationError("No permission to delete vocabulary!");
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
            result.remove(new Integer(folderId));
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
     * @return
     * @throws ServiceException
     */
    public Resolution delete() throws ServiceException {
        vocabularyService.deleteVocabularyFolders(folderIds);
        addSystemMessage("Vocabularies deleted successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFoldersActionBean.class);
        return resolution;
    }

    /**
     * search vocabulary folders.
     *
     * @return Stripes resolution
     * @throws ServiceException if search fails
     */
    public Resolution search() throws ServiceException {
        if (vocabularyFilter == null) {
            vocabularyFilter = new VocabularyFilter();
        }
        //do not show working copies for anonymous users
        if (!isUserLoggedIn()) {
            vocabularyFilter.setWorkingCopy(false);
        }

        vocabularyResult = vocabularyService.searchVocabularies(vocabularyFilter);

        return new ForwardResolution(VOCABULARY_SEARCH_RESULT_JSP);

    }

    /**
     * @param vocabularyService the vocabularyService to set
     */
    public void setVocabularyService(IVocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    /**
     * @return the folderIds
     */
    public List<Integer> getFolderIds() {
        return folderIds;
    }

    /**
     * @param folderIds the folderIds to set
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
     * @param folderId the folderId to set
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
     * @param expand the expand to set
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
     * @param expanded the expanded to set
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
     * @param folders the folders to set
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
     * @param editDivId the editDivId to set
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
     * @param visibleEditableVocabularies the visibleEditableVocabularies to set
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
     * @param folderIdentifier the folderIdentifier to set
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

}
