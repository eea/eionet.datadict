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

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;

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

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Vocabulary folder. */
    private VocabularyFolder vocabularyFolder;

    /** Vocabulary concepts. */
    private List<VocabularyConcept> vocabularyConcepts;

    /** Vocabulary concept to add/edit. */
    private VocabularyConcept vocabularyConcept;

    /** Selected vocabulary concept ids. */
    private List<Integer> conceptIds;

    /**
     * Navigates to view vocabulary folder page.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), false);
        vocabularyConcepts = vocabularyService.getVocabularyConcepts(vocabularyFolder.getId());
        return new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP);
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
        vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), false);
        vocabularyConcepts = vocabularyService.getVocabularyConcepts(vocabularyFolder.getId());
        return new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Save vocabulary folder action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveFolder() throws ServiceException {
        if (vocabularyFolder.getId() == 0) {
            vocabularyService.createVocabularyFolder(vocabularyFolder);
        } else {
            vocabularyService.updateVocabularyFolder(vocabularyFolder);
            LOGGER.debug("Updating vocabulary folder: " + vocabularyFolder.getIdentifier());
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
        if (vocabularyConcept.getId() == 0) {
            vocabularyService.createVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept);
            LOGGER.debug("Creating vocabulary concept: " + vocabularyConcept.getIdentifier());
        } else {
            vocabularyService.updateVocabularyConcept(vocabularyConcept);
            LOGGER.debug("Updating vocabulary concept: " + vocabularyConcept.getIdentifier());
        }
        addSystemMessage("Vocabulary concept saved successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Deletes vocabulary concepts.
     *
     * @return
     */
    public Resolution deleteConcepts() {
        LOGGER.debug("Deleting concepts: " + StringUtils.join(conceptIds, ", "));
        addSystemMessage("Vocabulary concepts deleted successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
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
     * Returns autogenerated identifier for new concept.
     *
     * @return
     */
    public int getNextIdentifier() {
        if (vocabularyConcepts != null) {
            return vocabularyConcepts.size() + 1;
        }
        return 1;
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
    public List<VocabularyConcept> getVocabularyConcepts() {
        return vocabularyConcepts;
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

}
