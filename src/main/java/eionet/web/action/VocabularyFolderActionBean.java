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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.ICSVVocabularyImportService;
import eionet.meta.service.IDataService;
import eionet.meta.service.ISiteCodeService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.Triple;
import eionet.util.Util;
import eionet.util.VocabularyCSVOutputHelper;

/**
 * Edit vocabulary folder action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabulary/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{$event}")
public class VocabularyFolderActionBean extends AbstractActionBean {

    /** JSP pages. */
    private static final String ADD_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/addVocabularyFolder.jsp";
    private static final String EDIT_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/editVocabularyFolder.jsp";
    private static final String VIEW_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/viewVocabularyFolder.jsp";

    /** Popup div's id prefix on jsp page. */
    private static final String EDIT_DIV_ID_PREFIX = "editConceptDiv";
    /** Pop div's id for new concept form. */
    private static final String NEW_CONCEPT_DIV_ID = "addNewConceptDiv";

    /** Reserved event names, that cannot be vocabulary concept identifiers. */
    public static List<String> RESERVED_VOCABULARY_EVENTS;

    /** Folder choice values. */
    private static final String FOLDER_CHOICE_EXISTING = "existing";
    private static final String FOLDER_CHOICE_NEW = "new";

    static {
        RESERVED_VOCABULARY_EVENTS = new ArrayList<String>();
        RESERVED_VOCABULARY_EVENTS.add("view");
        RESERVED_VOCABULARY_EVENTS.add("search");
        RESERVED_VOCABULARY_EVENTS.add("viewWorkingCopy");
        RESERVED_VOCABULARY_EVENTS.add("add");
        RESERVED_VOCABULARY_EVENTS.add("edit");
        RESERVED_VOCABULARY_EVENTS.add("saveFolder");
        RESERVED_VOCABULARY_EVENTS.add("saveConcept");
        RESERVED_VOCABULARY_EVENTS.add("checkIn");
        RESERVED_VOCABULARY_EVENTS.add("checkOut");
        RESERVED_VOCABULARY_EVENTS.add("undoCheckOut");
        RESERVED_VOCABULARY_EVENTS.add("deleteConcepts");
        RESERVED_VOCABULARY_EVENTS.add("cancelAdd");
        RESERVED_VOCABULARY_EVENTS.add("cancelSave");
        RESERVED_VOCABULARY_EVENTS.add("rdf");
        RESERVED_VOCABULARY_EVENTS.add("csv");
        RESERVED_VOCABULARY_EVENTS.add("uploadCsv");
    }

    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String CSV_FILE_CONTENT_TYPE_PLAIN = "text/plain";
    private static final String CSV_FILE_CONTENT_TYPE_CSV = "text/csv";

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Site code service. */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /** Data elements service. */
    @SpringBean
    private IDataService dataService;

    /** Vocabulary folder. */
    private VocabularyFolder vocabularyFolder;

    /** CSV Import Service */
    @SpringBean
    private ICSVVocabularyImportService vocabularyCsvImportService;

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

    /** Folders. */
    private List<Folder> folders;

    /** New folder to be created. */
    private Folder folder;

    /** Checkbox value for folder, when creating vocabulary folder. */
    private String folderChoice;

    /** Data elements search filter. */
    private DataElementsFilter elementsFilter;

    /** Data elements search result object. */
    private DataElementsResult elementsResult;

    /** Bound data elements. */
    private List<DataElement> bindedElements;

    /** Data element id. */
    private int elementId;

    /** uploaded csv file to import into vocabulary */
    private FileBean uploadedCsvFile;

    /** before import, if user requested purging data */
    private boolean purgeVocabularyData = false;

    /**
     * Identifier before the user started editing. Needed to make the URLs working correctly still if user deletes identifier in the
     * UI
     */
    private String origIdentifier;

    /**
     * Navigates to view vocabulary folder page.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());

        validateView();
        // Check if vocabulary concept url
        Resolution resolution = getVocabularyConceptResolution();
        if (resolution != null) {
            return resolution;
        }

        initFilter();

        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        vocabularyFolderVersions =
                vocabularyService.getVocabularyFolderVersions(vocabularyFolder.getContinuityId(), vocabularyFolder.getId(),
                        getUserName());

        bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        return new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP);
    }

    public Resolution search() throws ServiceException {
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
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Navigates to add vocabulary folder form.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution add() throws ServiceException {
        folders = vocabularyService.getFolders(getUserName(), null);
        return new ForwardResolution(ADD_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Navigates to edit vocabulary folder form.
     *
     * @return Resolution
     * @throws ServiceException
     *             if error in queries
     */
    public Resolution edit() throws ServiceException {
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        initFilter();
        origIdentifier = vocabularyFolder.getIdentifier();
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        folders = vocabularyService.getFolders(getUserName(), null);
        folderChoice = FOLDER_CHOICE_EXISTING;

        bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        return new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Searches data elements.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution searchDataElements() throws ServiceException {
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        initFilter();
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        folders = vocabularyService.getFolders(getUserName(), null);
        folderChoice = FOLDER_CHOICE_EXISTING;

        if (elementsFilter == null) {
            elementsFilter = new DataElementsFilter();
        }
        elementsFilter.setRegStatus("Released");
        elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
        elementsResult = dataService.searchDataElements(elementsFilter);
        editDivId = "addElementsDiv";

        bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        return new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Adds data element relation.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution addDataElement() throws ServiceException {
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());

        vocabularyService.addDataElement(vocabularyFolder.getId(), elementId);
        addSystemMessage("Data element added");

        return resolution;
    }

    /**
     * Removes data element relation.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution removeDataElement() throws ServiceException {
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());

        vocabularyService.removeDataElement(vocabularyFolder.getId(), elementId);
        addSystemMessage("Data element removed");

        return resolution;
    }

    /**
     * Returns true if the current user is allowed to add new site codes.
     *
     * @return
     */
    public boolean isCreateNewSiteCodeAllowed() {

        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUserName(), "/sitecodes", "i");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return false;
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
     * True, if user has create right.
     *
     * @return
     */
    public boolean isCreateRight() {
        if (getUser() != null) {
            return getUser().hasPermission("/vocabularies", "i");
        }
        return false;
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
                if (StringUtils.equals(FOLDER_CHOICE_EXISTING, folderChoice)) {
                    vocabularyService.createVocabularyFolderCopy(vocabularyFolder, copyId, getUserName(), null);
                    vocabularyFolder.setFolderName(vocabularyService.getFolder(vocabularyFolder.getFolderId()).getIdentifier());
                }
                if (StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)) {
                    vocabularyService.createVocabularyFolderCopy(vocabularyFolder, copyId, getUserName(), folder);
                    vocabularyFolder.setFolderName(folder.getIdentifier());
                }
            } else {
                if (StringUtils.equals(FOLDER_CHOICE_EXISTING, folderChoice)) {
                    vocabularyService.createVocabularyFolder(vocabularyFolder, null, getUserName());
                    vocabularyFolder.setFolderName(vocabularyService.getFolder(vocabularyFolder.getFolderId()).getIdentifier());
                }
                if (StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)) {
                    vocabularyService.createVocabularyFolder(vocabularyFolder, folder, getUserName());
                    vocabularyFolder.setFolderName(folder.getIdentifier());
                }
            }
        } else {
            if (StringUtils.equals(FOLDER_CHOICE_EXISTING, folderChoice)) {
                vocabularyService.updateVocabularyFolder(vocabularyFolder, null);
                vocabularyFolder.setFolderName(vocabularyService.getFolder(vocabularyFolder.getFolderId()).getIdentifier());
            }
            if (StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)) {
                vocabularyService.updateVocabularyFolder(vocabularyFolder, folder);
                vocabularyFolder.setFolderName(folder.getIdentifier());
            }
        }
        origIdentifier = vocabularyFolder.getIdentifier();
        addSystemMessage("Vocabulary saved successfully");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        if (vocabularyFolder.isWorkingCopy()) {
            resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        }
        return resolution;
    }

    /**
     * Save vocabulary concept action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveConcept() throws ServiceException {

        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());

        if (vocabularyConcept != null) {
            // Save new concept
            vocabularyService.createVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept);
        } else {
            // Update existing concept
            vocabularyService.quickUpdateVocabularyConcept(getEditableConcept());
            initFilter();
            resolution.addParameter("page", page);
            if (StringUtils.isNotEmpty(filter.getText())) {
                resolution.addParameter("filter.text", filter.getText());
            }
        }

        addSystemMessage("Vocabulary concept saved successfully");
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
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        // resolution.addParameter("vocabularyFolder.workingCopy", false);
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
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
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
        int id = vocabularyService.undoCheckOut(vocabularyFolder.getId(), getUserName());
        vocabularyFolder = vocabularyService.getVocabularyFolder(id);
        addSystemMessage("Checked out version successfully deleted");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        // resolution.addParameter("vocabularyFolder.workingCopy", false);
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
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Marks vocabulary concepts obsolete.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution markConceptsObsolete() throws ServiceException {
        vocabularyService.markConceptsObsolete(conceptIds);
        addSystemMessage("Vocabulary concepts marked obsolete");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Removes the obsolete status from concepts.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution unMarkConceptsObsolete() throws ServiceException {
        vocabularyService.unMarkConceptsObsolete(conceptIds);
        addSystemMessage("Obsolete status removed from vocabulary concepts");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Validates check out.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"checkOut"})
    public void validateCheckOut() throws ServiceException {
        if (!isUpdateRight()) {
            addGlobalValidationError("No permission to modify vocabulary");
            getContext().setSourcePageResolution(new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP));
        }
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
     * Validation on adding a binded data element.
     *
     * @throws ServiceException
     *             if checking fails
     */
    @ValidationMethod(on = {"addDataElement"})
    public void validateAddDataElement() throws ServiceException {
        if (vocabularyService.vocabularyHasDataElementBinding(vocabularyFolder.getId(), elementId)) {
            addGlobalValidationError("This vocabulary already has binding to this element.");
        }

        // if validation errors were set make sure the right resolution is returned
        if (isValidationErrors()) {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
            folders = vocabularyService.getFolders(getUserName(), null);
            folderChoice = FOLDER_CHOICE_EXISTING;

            bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
            Resolution resolution = new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     * validates removing data elements. Elements which have values in any concepts cannot be removed.
     *
     * @throws ServiceException
     *             if checking fails
     */
    @ValidationMethod(on = {"removeDataElement"})
    public void validaRemoveDataElement() throws ServiceException {

        // if this element binding has valued in any concept - do not remove it
        List<VocabularyConcept> conceptsWithValue =
                vocabularyService.getConceptsWithElementValue(elementId, vocabularyFolder.getId());

        if (!conceptsWithValue.isEmpty()) {
            String ids = StringUtils.join(conceptsWithValue, ",");
            addGlobalValidationError("This element has value in Concepts: " + ids + '\n'
                    + "Please delete the values before removing the element binding.");
        }

        if (isValidationErrors()) {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
            folders = vocabularyService.getFolders(getUserName(), null);
            folderChoice = FOLDER_CHOICE_EXISTING;

            bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
            Resolution resolution = new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
            getContext().setSourcePageResolution(resolution);
        }

    }

    /**
     * Validates save folder.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"saveFolder"})
    public void validateSaveFolder() throws ServiceException {

        if (vocabularyFolder.getId() == 0) {
            if (!isCreateRight()) {
                addGlobalValidationError("No permission to create new vocabulary");
            }
        } else {
            if (!isUpdateRight()) {
                addGlobalValidationError("No permission to modify vocabulary");
            }
        }

        if (StringUtils.isEmpty(folderChoice)) {
            addGlobalValidationError("Folder is not specified");
        }

        // Validate new folder
        if (StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)) {
            if (StringUtils.isEmpty(folder.getIdentifier())) {
                addGlobalValidationError("Folder identifier is missing");
            }

            if (StringUtils.isEmpty(folder.getLabel())) {
                addGlobalValidationError("Folder label is missing");
            }

            if (StringUtils.isNotEmpty(folder.getIdentifier())) {
                if (!Util.isValidIdentifier(vocabularyFolder.getIdentifier())) {
                    addGlobalValidationError("Folder contains illegal characters (/%?#:\\)");
                }
                if (!vocabularyService.isUniqueFolderIdentifier(folder.getIdentifier(), 0)) {
                    addGlobalValidationError("The new folder's identifier is not unique");
                }
            }
        }

        // Validate vocabulary
        if (StringUtils.isEmpty(vocabularyFolder.getIdentifier())) {
            addGlobalValidationError("Vocabulary identifier is missing");
        } else {
            if (!Util.isValidIdentifier(vocabularyFolder.getIdentifier())) {
                addGlobalValidationError("Vocabulary identifier contains illegal characters (/%?#:\\)");
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
            if (!vocabularyService.isUniqueVocabularyFolderIdentifier(vocabularyFolder.getFolderId(),
                    vocabularyFolder.getIdentifier())) {
                addGlobalValidationError("Vocabulary identifier is not unique");
            }
        } else {
            if (!vocabularyService.isUniqueVocabularyFolderIdentifier(vocabularyFolder.getFolderId(),
                    vocabularyFolder.getIdentifier(), vocabularyFolder.getId(), vocabularyFolder.getCheckedOutCopyId())) {
                addGlobalValidationError("Vocabulary identifier is not unique");
            }
        }

        // Validate attributes (only when updating existing vocabulary)
        if (vocabularyFolder.getId() != 0) {
            mergeAttributes();
            for (List<SimpleAttribute> attrs : vocabularyFolder.getAttributes()) {
                if (attrs != null) {
                    for (SimpleAttribute attr : attrs) {
                        if (attr != null) {
                            if (attr.isMandatory() && StringUtils.isEmpty(attr.getValue())) {
                                addGlobalValidationError(attr.getLabel() + " is missing");
                            }
                        }
                    }
                }
            }
        }

        if (isValidationErrors()) {
            folders = vocabularyService.getFolders(getUserName(), null);
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
            bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        }
    }

    /**
     * Because not all the properties of dynamic attributes get submitted by form (meta data), but only values, we don't have enough
     * data to do validation and re-displaying the attributes on the form when validation errors occour. This method loads the
     * attributes metadata from database and merges them with the submitted attributes.
     *
     * @throws ServiceException
     */
    private void mergeAttributes() throws ServiceException {
        List<SimpleAttribute> attrMeta = vocabularyService.getVocabularyFolderAttributesMetadata();
        List<List<SimpleAttribute>> attributes = new ArrayList<List<SimpleAttribute>>();

        if (vocabularyFolder.getAttributes() != null) {
            for (int i = 0; i < vocabularyFolder.getAttributes().size(); i++) {
                List<SimpleAttribute> attrValues = vocabularyFolder.getAttributes().get(i);
                SimpleAttribute attrMetadata = attrMeta.get(i);
                List<SimpleAttribute> attrs = new ArrayList<SimpleAttribute>();
                if (attrValues != null) {
                    for (SimpleAttribute attrValue : attrValues) {
                        if (attrValue != null) {
                            attrs.add(mergeTwoAttributes(attrMetadata, attrValue));
                        } else {
                            attrs.add(attrMetadata);
                        }
                    }
                } else {
                    attrs.add(attrMetadata);
                }
                attributes.add(attrs);
            }
        }

        vocabularyFolder.setAttributes(attributes);
    }

    /**
     * Returns new attribute object with merged data.
     *
     * @param metadata
     * @param attributeValue
     * @return
     */
    private SimpleAttribute mergeTwoAttributes(SimpleAttribute metadata, SimpleAttribute attributeValue) {
        if (metadata.getAttributeId() != attributeValue.getAttributeId()) {
            throw new IllegalStateException("Illegal set of attributes metadata, failed to synchronize attributes.");
        }
        try {
            SimpleAttribute result = (SimpleAttribute) BeanUtils.cloneBean(metadata);
            result.setValue(attributeValue.getValue());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone attributes object", e);
        }
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
                    addGlobalValidationError("Vocabulary concept identifier contains illegal characters (/%?#:\\)");
                }
                if (RESERVED_VOCABULARY_EVENTS.contains(vc.getIdentifier())) {
                    addGlobalValidationError("This vocabulary concept identifier is reserved value and cannot be used");
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
            bindedElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
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
     * @throws ServiceException
     */
    public Resolution cancelSave() throws ServiceException {
        vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getId());
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class);
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
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
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            false);

            if (vocabularyFolder.isDraftStatus()) {
                throw new RuntimeException("Vocabulary is not in released or public draft status.");
            }

            List<VocabularyFolder> vocabularyFolders = new ArrayList<VocabularyFolder>();
            vocabularyFolders.add(vocabularyFolder);
            final List<RdfNamespace> nameSpaces = vocabularyService.getVocabularyNamespaces(vocabularyFolders);

            initFilter();
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

            final String contextRoot =
                    StringUtils.isNotEmpty(vocabularyFolder.getBaseUri()) ? vocabularyFolder.getBaseUri() : Props
                            .getRequiredProperty(PropsIF.DD_URL)
                            + "/vocabulary/"
                            + vocabularyFolder.getFolderName()
                            + "/"
                            + vocabularyFolder.getIdentifier() + "/";

            final String folderContextRoot =
                    Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + vocabularyFolder.getFolderName() + "/";

            final String commonElemsUri = Props.getRequiredProperty(PropsIF.DD_URL) + "/property/";

            StreamingResolution result = new StreamingResolution("application/rdf+xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyXmlWriter xmlWriter = new VocabularyXmlWriter(response.getOutputStream());
                    xmlWriter.writeRDFXml(commonElemsUri, folderContextRoot, contextRoot, vocabularyFolder, finalConcepts,
                            nameSpaces);
                }
            };
            result.setFilename(vocabularyFolder.getIdentifier() + ".rdf");
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary RDF data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }// end of method rdf

    /**
     * Returns vocabulary concepts CSV.
     *
     * @return
     */
    public Resolution csv() {
        try {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());
            validateView();
            if (vocabularyFolder.isDraftStatus()) {
                throw new RuntimeException("Vocabulary is not in released or public draft status.");
            }

            final String folderContextRoot =
                    StringUtils.isNotEmpty(vocabularyFolder.getBaseUri()) ? vocabularyFolder.getBaseUri() : Props
                            .getRequiredProperty(PropsIF.DD_URL)
                            + "/vocabulary/"
                            + vocabularyFolder.getFolderName()
                            + "/"
                            + vocabularyFolder.getIdentifier() + "/";

            final List<VocabularyConcept> concepts =
                    vocabularyService.getVocabularyConceptsWithAttributes(vocabularyFolder.getId(),
                            vocabularyFolder.isNumericConceptIdentifiers(), ObsoleteStatus.ALL);
            final List<Triple<String, String, Integer>> fieldNamesWithLanguage =
                    vocabularyService.getVocabularyBoundElementNames(vocabularyFolder);

            StreamingResolution result = new StreamingResolution("text/csv") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyCSVOutputHelper.writeCSV(response.getOutputStream(), getUriPrefix(), folderContextRoot, concepts,
                            fieldNamesWithLanguage);
                }
            };
            result.setFilename(vocabularyFolder.getIdentifier() + ".csv");
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary CSV data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }// end of method csv

    /**
     * Returns vocabulary concepts CSV.
     *
     * @return
     */
    public Resolution uploadCsv() {
        try {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());
            validateView();
            if (!vocabularyFolder.isWorkingCopy()) {
                throw new RuntimeException("Vocabulary should be in working copy status");
            }

            if (this.uploadedCsvFile == null) {
                throw new RuntimeException("You should upload a file");
            }

            // TODO there should be more control file!!!
            String fileName = this.uploadedCsvFile.getFileName();
            if (StringUtils.isEmpty(fileName) || !fileName.toLowerCase().endsWith(VocabularyFolderActionBean.CSV_FILE_EXTENSION)) {
                throw new RuntimeException("File should be a CSV file");
            }

            String contentType = this.uploadedCsvFile.getContentType();
            if (!StringUtils.equals(contentType, VocabularyFolderActionBean.CSV_FILE_CONTENT_TYPE_PLAIN)
                    && !StringUtils.equals(contentType, VocabularyFolderActionBean.CSV_FILE_CONTENT_TYPE_PLAIN)) {
                throw new RuntimeException("You should upload a valid CSV file (plain/text or csv/text)");
            }




            // this.purge;
            // consume stupid bom first!! if it exists!
            InputStream is = this.uploadedCsvFile.getInputStream();
            byte[] firstThreeBytes = new byte[3];
            is.read(firstThreeBytes);

            if (!Arrays.equals(firstThreeBytes, VocabularyCSVOutputHelper.BOM_BYTE_ARRAY)) {
                is.close();
                is = this.uploadedCsvFile.getInputStream();
            }

            Reader csvFileReader = new InputStreamReader(is, CharEncoding.UTF_8);

            // concepts = new ArrayList<VocabularyConcept>();
            // final List<VocabularyConcept> foundConcepts =
            this.vocabularyCsvImportService.importCsvIntoVocabulary(csvFileReader, vocabularyFolder, purgeVocabularyData);

            // TODO dont just return edit, update this with some system messages!
            return edit();
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary CSV data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }// end of method csv

    /**
     * Forwards to vocabulary concept page, if the url patter is: /vocabylary/folderIdentifier/conceptIdentifier.
     *
     * @return
     */
    private Resolution getVocabularyConceptResolution() {
        HttpServletRequest httpRequest = getContext().getRequest();
        String url = httpRequest.getRequestURL().toString();
        // String query = httpRequest.getQueryString();

        String[] parameters = StringUtils.split(StringUtils.substringAfter(url, "/vocabulary/"), "/");

        if (parameters.length >= 3) {
            if (!RESERVED_VOCABULARY_EVENTS.contains(parameters[2])) {
                RedirectResolution resolution = new RedirectResolution(VocabularyConceptActionBean.class, "view");
                resolution.addParameter("vocabularyFolder.folderName", parameters[0]);
                resolution.addParameter("vocabularyFolder.identifier", parameters[1]);
                resolution.addParameter("vocabularyConcept.identifier", parameters[2]);
                if (vocabularyFolder.isWorkingCopy()) {
                    resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
                }
                return resolution;
            }
        }
        return null;
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
     * Initiates filter correct with parameters.
     */
    private void initFilter() {
        if (filter == null) {
            filter = new VocabularyConceptFilter();
        }
        filter.setVocabularyFolderId(vocabularyFolder.getId());
        filter.setPageNumber(page);
        filter.setNumericIdentifierSorting(vocabularyFolder.isNumericConceptIdentifiers());
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
     * @return the vocabularyService
     */
    public IVocabularyService getVocabularyService() {
        return vocabularyService;
    }

    /**
     * @return the folder
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * @param folder
     *            the folder to set
     */
    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    /**
     * @return the folderChoice
     */
    public String getFolderChoice() {
        return folderChoice;
    }

    /**
     * @param folderChoice
     *            the folderChoice to set
     */
    public void setFolderChoice(String folderChoice) {
        this.folderChoice = folderChoice;
    }

    /**
     * @return the folders
     */
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * @return the elementsFilter
     */
    public DataElementsFilter getElementsFilter() {
        return elementsFilter;
    }

    /**
     * @param elementsFilter
     *            the elementsFilter to set
     */
    public void setElementsFilter(DataElementsFilter elementsFilter) {
        this.elementsFilter = elementsFilter;
    }

    /**
     * @return the elementsResult
     */
    public DataElementsResult getElementsResult() {
        return elementsResult;
    }

    /**
     * @return the bindedElements
     */
    public List<DataElement> getBindedElements() {
        return bindedElements;
    }

    /**
     * @return the elementId
     */
    public int getElementId() {
        return elementId;
    }

    /**
     * @param elementId
     *            the elementId to set
     */
    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

    public String getOrigIdentifier() {
        return origIdentifier;
    }

    public void setOrigIdentifier(String origIdentifier) {
        this.origIdentifier = origIdentifier;
    }

    /**
     * @param uploadedFile
     *            the uploadedFile to set
     */
    public void setUploadedCsvFile(FileBean uploadedCsvFile) {
        this.uploadedCsvFile = uploadedCsvFile;
    }

    /**
     * @param purgeVocabularyData
     *            purge before importing csv
     */
    public void setPurgeVocabularyData(boolean purgeVocabularyData) {
        this.purgeVocabularyData = purgeVocabularyData;
    }
}
