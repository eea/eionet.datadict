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

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.StringEncoder;
import eionet.util.Util;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;

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

        resolution.addParameter("editDivId", editDivId);
        resolution.addParameter("elementId", elementId);

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
                                addGlobalValidationError("'" + metaInfo.getName() + "'"
                                        + " has the same value for the same language more than once: " + elem.getValueText() );
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
        }
    }

    /**
     * search concepts to be added as reference element.
     *
     * @return stripes resolution
     * @throws ServiceException if search fails
     */
    public Resolution searchConcepts() throws ServiceException {

        setConceptIdentifier(vocabularyConcept.getIdentifier());

        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());

        // current editable concept:
        vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), getConceptIdentifier(), true);
        validateView();
        initBeans();
        initSearchFilters();

        relatedConceptsFilter.setExcludedIds(Collections.singletonList(vocabularyConcept.getId()));

        // this is needed because of "limit " clause in the SQL. if this remains true, paging does not work in display:table
        relatedConceptsFilter.setUsePaging(false);

        // vocabulary is selected in step 1
        String vocabularyId = getContext().getRequestParameter("folderId");
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
        editDivId = "addConceptDiv";

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
        initBeans();

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
        String baseUri = vocabularyFolder.getBaseUri();
        if (StringUtils.isEmpty(baseUri)) {
            baseUri =
                    Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + vocabularyFolder.getFolderName() + "/"
                            + vocabularyFolder.getIdentifier();
        }
        if (!baseUri.endsWith("/")) {
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

    /**
     * If validation fails metadata and related elements data has to be added to concept bound elements to make the UI look nice.
     *
     * @throws ServiceException if database query fails
     */
    private void addElementMetadata() throws ServiceException {
        for (List<DataElement> elems : vocabularyConcept.getElementAttributes()) {
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

    /**
     * gets referenced concept meta from database.
     *
     * @param referenceElement reference elem of type localref or reference
     * @throws ServiceException if error in querying
     */
    private void setReferenceElementAttrs(DataElement referenceElement) throws ServiceException {
        int conceptId = referenceElement.getRelatedConceptId();
        VocabularyConcept relatedConcept = vocabularyService.getVocabularyConcept(conceptId);
        VocabularyFolder relatedFolder = vocabularyService.getVocabularyFolder(relatedConcept.getVocabularyId());
        Folder relatedVocSet = vocabularyService.getFolder(relatedFolder.getFolderId());

        referenceElement.setRelatedConceptId(conceptId);
        referenceElement.setRelatedConceptIdentifier(relatedConcept.getIdentifier());
        referenceElement.setRelatedConceptVocabulary(relatedFolder.getIdentifier());
        referenceElement.setRelatedConceptVocSet(relatedVocSet.getIdentifier());
        referenceElement.setRelatedConceptLabel(relatedConcept.getLabel());
    }

    public VocabularyFolder getRelatedVocabulary() {
        return relatedVocabulary;
    }

    public String getSearchEventName() {
        return searchEventName;
    }

    public List<Integer> getExcludedVocSetIds() {
        //return excludedVocSetIds == null ? "" : excludedVocSetIds;
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
}
