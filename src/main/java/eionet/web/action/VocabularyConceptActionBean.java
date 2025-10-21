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
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    /**
     * Action for saving concept.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveConcept() throws ServiceException {
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

        if (vocabularyConcept.getStatusModified() != null && today.before(vocabularyConcept.getStatusModified() )){
            addGlobalValidationError("Status modified date cannot be set to future");
        }

        if (vocabularyConcept.getAcceptedDate() != null && today.before(vocabularyConcept.getAcceptedDate())){
            addGlobalValidationError("Accepted date cannot be set to future");
        }

        if (vocabularyConcept.getNotAcceptedDate() != null && today.before(vocabularyConcept.getNotAcceptedDate())){
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
        return getUriPrefix() + Util.encodeURLPath(getConceptIdentifier());
    }

    public String getConceptUriWithNonEncodedIdentifier() {
        return getUriPrefix() + getConceptIdentifier();
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

    /**
     * If validation fails metadata and related elements data has to be added to concept bound elements to make the UI look nice.
     *
     * @throws ServiceException if database query fails
     */
    private void addElementMetadata() throws ServiceException {
        List<List<DataElement>> elementAttributes = vocabularyConcept.getElementAttributes();
        if (elementAttributes != null) {
            for (List<DataElement> elems :elementAttributes) {
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
