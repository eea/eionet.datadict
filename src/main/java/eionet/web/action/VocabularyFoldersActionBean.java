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
import java.util.ArrayList;
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.ISiteCodeService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Action bean for listing vocabulary folders.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabularies")
public class VocabularyFoldersActionBean extends AbstractActionBean {

    /** Page path. */
    private static final String BROWSE_VOCABULARY_FOLDERS_JSP = "/pages/vocabularies/browseVocabularyFolders.jsp";

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Site code service. */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /** Folders. */
    private List<Folder> folders;

    /** Selected vocabulary folder ids. */
    private List<Integer> folderIds;

    /** Folder ID, currently clicked. */
    private int folderId;

    /** True, if operation is to expand. To collapse, it is false. */
    private boolean expand;

    /** Comma separated folder IDs, that are expanded. */
    private String expanded;

    /** Popup div id to keep open, when validation error occur. */
    private String editDivId;

    /**
     * View vocabulary folders list action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        folders = vocabularyService.getFolders(getUserName(), parseExpandedIds());
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
     * @throws ServiceException
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
     */
    @ValidationMethod(on = {"deleteFolder"})
    public void validateDeleteFolder() throws ServiceException {
        if (!isUpdateRight()) {
            addGlobalValidationError("No permission to modify folder");
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
     * Action, that returns RDF output of the folder's vocabularies.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution rdf() {
        try {
            final Folder folder = vocabularyService.getFolder(folderId);
            final List<VocabularyFolder> vocabularyFolders = vocabularyService.getReleasedVocabularyFolders(folderId);

            StreamingResolution result = new StreamingResolution("application/rdf+xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyXmlWriter xmlWriter = new VocabularyXmlWriter(response.getOutputStream());

                    String folderContextRoot = Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + folder.getIdentifier();

                    xmlWriter.writeXmlStart(true, folderContextRoot);
                    xmlWriter.writeFolderXml(folderContextRoot, folder.getLabel());

                    for (VocabularyFolder vocabularyFolder : vocabularyFolders) {
                        VocabularyConceptFilter filter = new VocabularyConceptFilter();
                        filter.setUsePaging(false);
                        filter.setObsoleteStatus(ObsoleteStatus.VALID_ONLY);
                        List<? extends VocabularyConcept> concepts = null;
                        if (vocabularyFolder.isSiteCodeType()) {
                            String countryCode = getContext().getRequestParameter("countryCode");
                            String identifier = getContext().getRequestParameter("identifier");
                            SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
                            siteCodeFilter.setUsePaging(false);
                            siteCodeFilter.setCountryCode(countryCode);
                            siteCodeFilter.setIdentifier(identifier);
                            concepts = siteCodeService.searchSiteCodes(siteCodeFilter).getList();
                        } else {
                            concepts =
                                    vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                                            vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);
                        }

                        final List<? extends VocabularyConcept> finalConcepts = concepts;

                        String vocabularyContextRoot =
                                StringUtils.isNotEmpty(vocabularyFolder.getBaseUri()) ? vocabularyFolder.getBaseUri() : Props
                                        .getRequiredProperty(PropsIF.DD_URL)
                                        + "/vocabulary/"
                                        + vocabularyFolder.getFolderName()
                                        + "/" + vocabularyFolder.getIdentifier() + "/";

                        xmlWriter.writeVocabularyFolderXml(vocabularyContextRoot, folderContextRoot, vocabularyFolder, finalConcepts);
                    }

                    xmlWriter.writeXmlEnd();
                }
            };

            result.setFilename(folder.getIdentifier() + ".rdf");

            return result;

        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary RDF data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
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
     * @param vocabularyService
     *            the vocabularyService to set
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

}
