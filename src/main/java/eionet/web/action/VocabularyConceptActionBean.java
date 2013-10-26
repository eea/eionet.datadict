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

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UriUtils;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/**
 * Vocabulary concept action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabularyconcept/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{vocabularyConcept.identifier}/{$event}")
public class VocabularyConceptActionBean extends AbstractActionBean {

    /** JSP pages. */
    private static final String VIEW_VOCABULARY_CONCEPT_JSP = "/pages/vocabularies/viewVocabularyConcept.jsp";
    /** JSP page for edit screen. */
    private static final String EDIT_VOCABULARY_CONCEPT_JSP = "/pages/vocabularies/editVocabularyConcept.jsp";

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Vocabulary folder. */
    private VocabularyFolder vocabularyFolder;

    /** Vocabulary concept to add/edit. */
    private VocabularyConcept vocabularyConcept;

    /** Other vocabulary concepts in the vocabulary folder. */
    private List<VocabularyConcept> vocabularyConcepts;

    /** private helper property for vocabulary concept identifier . */
    private String conceptIdentifier;

    /**
     * View action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        // to be removed if Stripes is upgraded to 1.5.8
        handleConceptIdentifier();

        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), false);
        validateView();

        // LOGGER.debug("Element attributes: " + vocabularyConcept.getElementAttributes().size());

        return new ForwardResolution(VIEW_VOCABULARY_CONCEPT_JSP);
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
        try {
            identifier = UriUtils.decode(identifier, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported Encoding Exception " + e);
        }

        setConceptIdentifier(identifier);
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
        initBeans();

        // LOGGER.debug("Element attributes: " + vocabularyConcept.getElementAttributes().size());

        return new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
    }

    /**
     * Action for saving concept.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveConcept() throws ServiceException {
        vocabularyConcept.setIdentifier(getConceptIdentifier());
        vocabularyService.updateVocabularyConcept(vocabularyConcept);

        addSystemMessage("Vocabulary concept saved successfully");

        RedirectResolution resolution = new RedirectResolution(getClass(), "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        resolution.addParameter("vocabularyConcept.identifier", Util.encodeURLPath(vocabularyConcept.getIdentifier()));
        return resolution;
    }

    /**
     * Marks vocabulary concept obsolete.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution markConceptObsolete() throws ServiceException {
        vocabularyConcept.setIdentifier(getConceptIdentifier());
        vocabularyService.markConceptsObsolete(Collections.singletonList(vocabularyConcept.getId()));

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
        vocabularyConcept.setIdentifier(getConceptIdentifier());
        vocabularyService.unMarkConceptsObsolete(Collections.singletonList(vocabularyConcept.getId()));

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

        // Validate unique identifier
        if (!vocabularyService.isUniqueConceptIdentifier(getConceptIdentifier(), vocabularyFolder.getId(),
                vocabularyConcept.getId())) {
            addGlobalValidationError("Vocabulary concept identifier is not unique");
        }

        if (vocabularyConcept.getElementAttributes() != null) {
            for (List<DataElement> elems : vocabularyConcept.getElementAttributes()) {
                if (elems != null) {
                    for (DataElement elem : elems) {
                        if (elem != null) {
                            if (vocabularyService.isReferenceElement(elem.getId())) {
                                if (!Util.isURL(elem.getAttributeValue())) {
                                    addGlobalValidationError("Element value '" + elem.getAttributeValue()
                                            + "' must be in URL format");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isValidationErrors()) {
            initBeans();
            vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), true);
        }
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
     * @return
     */
    public String getConceptUri() {
        return VocabularyXmlWriter.escapeIRI(getUriPrefix() + getConceptIdentifier());
    }

    /**
     * Returns the prefix of the URL for a link to a <em>HTML view</em> of the concept.
     * This must match the @UrlBinding of this class.
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
        String baseUri = vocabularyFolder.getBaseUri();
        if (StringUtils.isEmpty(baseUri)) {
            baseUri =
                    Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + vocabularyFolder.getFolderName() + "/"
                            + vocabularyFolder.getIdentifier();
        }
        if (!baseUri.endsWith("/")) {
            baseUri += "/";
        }

        return VocabularyXmlWriter.escapeIRI(baseUri);
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
     * @param identifier
     *            vocabulary concept identifier
     */
    public void setConceptIdentifier(String identifier) {
        conceptIdentifier = identifier;
    }

}
