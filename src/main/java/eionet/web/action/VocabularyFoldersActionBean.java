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

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;

/**
 * Action bean for listing vocabulary folders.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabularies")
public class VocabularyFoldersActionBean extends AbstractActionBean {

    /** */
    private static final String BROWSE_VOCABULARY_FOLDERS_JSP = "/pages/vocabularies/browseVocabularyFolders.jsp";
    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Vocabulary folders. */
    private List<VocabularyFolder> vocabularyFolders;

    /** Selected vocabulary folder ids. */
    private List<Integer> folderIds;

    /**
     * View vocabulary folders list action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        vocabularyFolders = vocabularyService.getVocabularyFolders(getUserName());
        return new ForwardResolution(BROWSE_VOCABULARY_FOLDERS_JSP);
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
     * @return the vocabularyFolders
     */
    public List<VocabularyFolder> getVocabularyFolders() {
        return vocabularyFolders;
    }

    /**
     * @param vocabularyFolders
     *            the vocabularyFolders to set
     */
    public void setVocabularyFolders(List<VocabularyFolder> vocabularyFolders) {
        this.vocabularyFolders = vocabularyFolders;
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

}
