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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.errors.BadFormatException;
import eionet.datadict.errors.ConceptWithoutNotationException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskExecutionError;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.SchedulingIntervalUnit;
import eionet.datadict.model.enums.Enumerations.VocabularyRdfPurgeOption;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.stripes.FileBeanDecompressor;
import eionet.datadict.util.ScheduledTaskResolver;
import eionet.datadict.web.asynctasks.*;
import eionet.datadict.web.viewmodel.ScheduledTaskView;
import eionet.meta.dao.domain.*;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.exports.rdf.InspireCodelistXmlWriter;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.*;
import eionet.meta.service.data.*;
import eionet.util.*;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import org.springframework.web.util.UriUtils;

/**
 * Edit vocabulary folder action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/vocabulary/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{$event}")
public class VocabularyFolderActionBean extends AbstractActionBean {

    /**
     * JSP pages for vocabulary adding.
     */
    private static final String ADD_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/addVocabularyFolder.jsp";

    /**
     * JSP pages for vocabulary editing.
     */
    private static final String EDIT_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/editVocabularyFolder.jsp";

    /**
     * JSP pages for vocabulary viewing.
     */
    private static final String VIEW_VOCABULARY_FOLDER_JSP = "/pages/vocabularies/viewVocabularyFolder.jsp";

    /**
     * JSP page for dynamic filter response through ajax.
     */
    private static final String BOUND_ELEMENT_FILTER_JSP = "/pages/vocabularies/boundElementFilter.jsp";

    /**
     *Page responsible for scheduling the RDF import for a Vocabulary
     */
    private static final String SCHEDULE_VOCABULARY_SYNC = "/pages/vocabularies/scheduleVocabularySync.jsp";

     /**
     *Page showing the scheduled jobs queue and past job execution attempts
     */
    private static final String SCHEDULED_JOBS_VIEW = "/pages/vocabularies/viewScheduledJobs.jsp";

     /**
     *Page showing the scheduled Task past execution attempts 
     */
    private static final String SCHEDULED_TASK_HISTORY_VIEW = "/pages/vocabularies/viewScheduledTaskHistory.jsp";

    
    /**
     * Page to view the details of a scheduled task
     */
    private static final String SCHEDULED_TASK_DETAILS = "/pages/vocabularies/scheduledTaskDetails.jsp";
   
    /**
     * Page to view the details of a scheduled task History
     */
    private static final String  SCHEDULED_TASK_HISTORY_DETAILS = "/pages/vocabularies/scheduledTaskHistoryDetails.jsp"; 
   
    private static final String EDIT_SCHEDULED_TASK_DETAILS = "/pages/vocabularies/editScheduledTaskDetails.jsp";
    
    /**
     * Popup div's id prefix on jsp page.
     */
    private static final String EDIT_DIV_ID_PREFIX = "editConceptDiv";

    /**
     * Pop div's id for new concept form.
     */
    private static final String NEW_CONCEPT_DIV_ID = "addNewConceptDiv";

    /**
     * Reserved event names, that cannot be vocabulary concept identifiers.
     */
    public static final List<String> RESERVED_VOCABULARY_EVENTS;

    /**
     * Folder choice value [existing].
     */
    private static final String FOLDER_CHOICE_EXISTING = "existing";

    /**
     * Folder choice value [new].
     */
    private static final String FOLDER_CHOICE_NEW = "new";

    private static final String VOCABULARY_RDF_IMPORT_FROM_URL_TASK_CLASS_NAME = "eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask";

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
        RESERVED_VOCABULARY_EVENTS.add("uploadRdf");
        RESERVED_VOCABULARY_EVENTS.add("json");
        RESERVED_VOCABULARY_EVENTS.add("createSyncSchedule");
    }

    /**
     * Extension for CSV files.
     */
    private static final String CSV_FILE_EXTENSION = ".csv";

    /**
     * Extension for RDF files.
     */
    private static final String RDF_FILE_EXTENSION = ".rdf";
    
    /**
     * JSON contept type.
     */
    public static final String JSON_DEFAULT_OUTPUT_FORMAT = "application/json";

    /**
     * JSON file extension.
     */
    public static final String JSON_EXTENSION = ".json";

    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;
    
    @SpringBean
    private AttributeService attributeService;

    @SpringBean
    private AttributeDataService attributeDataService;

    /**
     * Site code service.
     */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /**
     * Data elements service.
     */
    @SpringBean
    private IDataService dataService;

    /**
     * Vocabulary folder.
     */
    private VocabularyFolder vocabularyFolder;

    /**
     * CSV Import Service.
     */
    @SpringBean
    private ICSVVocabularyImportService vocabularyCsvImportService;

    /**
     * RDF Import Service.
     */
    @SpringBean
    private IRDFVocabularyImportService vocabularyRdfImportService;

    @SpringBean
    private AsyncTaskManager asyncTaskManager;
    
    
    
    @SpringBean
    private AsyncTaskDao asyncTaskDao;

    @SpringBean
    private AsyncTaskHistoryDao asyncTaskHistoryDao;

    @SpringBean
    private FileBeanDecompressor fileBeanDecompressor;
    
    @SpringBean
    private ScheduledTaskResolver scheduledTaskResolver;
    
    @SpringBean
    private AsyncTaskDataSerializer asyncTaskDataSerializer;

    @SpringBean
    private DataElementsService dataElementsService;

    @SpringBean
    private VocabularyConceptService vocabularyConceptService;

    /**
     * Other versions of the same vocabulary folder.
     */
    private List<VocabularyFolder> vocabularyFolderVersions;

    /**
     * Vocabulary concepts.
     */
    private VocabularyConceptResult vocabularyConcepts;

    /**
     * Vocabulary concept to add/edit.
     */
    private VocabularyConcept vocabularyConcept;

    /**
     * Selected vocabulary concept ids.
     */
    private List<Integer> conceptIds;

    /**
     * Vocabulary folder id, from which the copy is made of.
     */
    private int copyId;

    /**
     * Popup div id to keep open, when validation error occur.
     */
    private String editDivId;

    /**
     * Vocabulary concept filter.
     */
    private VocabularyConceptFilter filter;

    /**
     * Concepts table page number.
     */
    private int page = 1;

    /**
     * Folders.
     */
    private List<Folder> folders;

    /**
     * New folder to be created.
     */
    private Folder folder;

    /**
     * Checkbox value for folder, when creating vocabulary folder.
     */
    private String folderChoice;

    /**
     * Data elements search filter.
     */
    private DataElementsFilter elementsFilter;

    /**
     * Data elements search result object.
     */
    private DataElementsResult elementsResult;

    /**
     * Bound data elements.
     */
    private List<DataElement> boundElements;

    /**
     * Data element id.
     */
    private int elementId;

    /**
     * Uploaded csv/rdf file to import into vocabulary.
     */
    private FileBean uploadedFileToImport;

    /**
     * Before import, if user requested purging data.
     */
    private boolean purgeVocabularyData = false;

    /**
     * Before import, if user requested purging bound elements.
     */
    private boolean purgeBoundElements = false;

    /**
     * Identifier before the user started editing. Needed to make the URLs working correctly still if user deletes identifier in the
     * UI
     */
    private String origIdentifier;

    /**
     * Rdf purge option.
     */
    private int rdfPurgeOption;

    private IVocabularyImportService.MissingConceptsAction missingConceptsAction;
    
    /**
     * Language for search.
     */
    private String lang;

    /**
     * Pref label for search.
     */
    private String label;

    /**
     * Identifier for search.
     */
    private String id;

    /**
     * Format for output type.
     */
    private String format;

    /**
     * JSON-LD supported output formats.
     */
    private static final List<String> SUPPORTED_JSON_FORMATS = new ArrayList<String>();
    static {
        SUPPORTED_JSON_FORMATS.add(JSON_DEFAULT_OUTPUT_FORMAT);
    }

    /**
     * Index for the new bound element filter in the list
     */
    private int boundElementFilterIndex;

    /**
     * Bound element id for the new bound element filter
     */
    private int boundElementFilterId;

    /**
     * Vocabulary folder id for the new bound element filter
     */
    private int vocabularyFolderId;

    /**
     * New bound element filter
     */
    private VocabularyConceptBoundElementFilter boundElementFilter;

    /**
     * List of bound element filters
     */
    private List<VocabularyConceptBoundElementFilter> boundElementFilters = new ArrayList<VocabularyConceptBoundElementFilter>();

    /**
     * List of columns for filter
     */
    private List<String> columns = new ArrayList<String>();

    /**
     *
     *Schedule Vocabulary Synchronization  Parameters 
     **/
    private String vocabularyRdfUrl ;
    
    private String emails;
    
    private Integer scheduleInterval;
    
    private List<AsyncTaskExecutionEntry> asyncTaskEntries;

    private List<AsyncTaskExecutionEntryHistory> asyncTaskEntriesHistory;
    
    private List<ScheduledTaskView> futureScheduledTaskViews = new ArrayList<ScheduledTaskView>();
    
    private List<ScheduledTaskView> scheduledTaskHistoryViews = new ArrayList<ScheduledTaskView>();
    
    private EmailValidator emailValidator = EmailValidator.getInstance();

    private String scheduledTaskId;
    
    private String scheduledTaskHistoryId;
    
    private ScheduledTaskView scheduledTaskView;

    private SchedulingIntervalUnit schedulingIntervalUnit;

    /***
     *This Field is mandatory if one wishes to redirect the user to the Generic Datadict Error page.
     **/
    private String errorTypeMsg;
    /**
     * Navigates to view vocabulary folder page.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
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

        boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        constructBoundElementsFilterer();
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        injectDataElementValuesInConcepts();

        vocabularyFolderVersions =
                vocabularyService.getVocabularyFolderVersions(vocabularyFolder.getContinuityId(), vocabularyFolder.getId(),
                        getUserName());

        return new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP);
    }

    /**scheduleIntervalMultiplier
     * Constructs new bound element filter
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution constructBoundElementFilter() throws ServiceException {
        List<Integer> cids = vocabularyService.getVocabularyConceptIds(vocabularyFolderId);
        boundElementFilter = vocabularyService.getVocabularyConceptBoundElementFilter(boundElementFilterId, cids);
        return new ForwardResolution(BOUND_ELEMENT_FILTER_JSP);
    }

    /**
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution search() throws ServiceException {
        return new ForwardResolution(VIEW_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Navigates to view vocabulary's working copy page.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
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
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
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
        boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        constructBoundElementsFilterer();
        vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
        injectDataElementValuesInConcepts();

        origIdentifier = vocabularyFolder.getIdentifier();
        folders = vocabularyService.getFolders(getUserName(), null);
        folderChoice = FOLDER_CHOICE_EXISTING;

        setRdfPurgeOption(1);

        return new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
    }

    private void constructBoundElementsFilterer() throws ServiceException {
        Collections.sort(boundElements, new Comparator<DataElement>() {
            @Override
            public int compare(DataElement o1, DataElement o2) {
                return (o1.getIdentifier().compareToIgnoreCase(o2.getIdentifier()));
            }
        });

        // populate columns filter
        columns.addAll(Arrays.asList(new String[] {
            "Notation", "Status", "Status Modified", "Accepted Date", "Not Accepted Date"
        }));
        for (DataElement boundElement : boundElements) {
            columns.add(boundElement.getIdentifier());
        }
        initFilter();

        for (DataElement boundElement : boundElements) {
            if (filter.getVisibleColumns().contains(boundElement.getIdentifier())) {
                filter.getBoundElementVisibleColumns().add(boundElement.getIdentifier());
            }
        }
        if (!filter.getBoundElementIds().isEmpty()) {
            List<Integer> cids = vocabularyService.getVocabularyConceptIds(vocabularyFolder.getId());
            for (Integer boundElementId : filter.getBoundElementIds()) {
                boundElementFilters.add(vocabularyService.getVocabularyConceptBoundElementFilter(boundElementId, cids));
            }
        }
    }

    private void injectDataElementValuesInConcepts() {
        if (!vocabularyConcepts.getList().isEmpty()) {
            List<Integer> vocabularyConceptIds = new ArrayList<Integer>();
            for (VocabularyConcept concept : vocabularyConcepts.getList()) {
                vocabularyConceptIds.add(concept.getId());
            }
            Map<Integer, List<List<DataElement>>> vocabularyConceptsDataElementValues =
                    vocabularyService.getVocabularyConceptsDataElementValues(vocabularyFolder.getId(),
                    ArrayUtils.toPrimitive(vocabularyConceptIds.toArray(new Integer[vocabularyConceptIds.size()])),
                            false);

            for (VocabularyConcept concept : vocabularyConcepts.getList()) {
                concept.setElementAttributes(vocabularyConceptsDataElementValues.get(concept.getId()));
            }
        }
    }

    /**
     * Searches data elements.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
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
        List<DataElement> dataElements = dataService.searchDataElements(elementsFilter);
        elementsResult = new DataElementsResult(dataElements, dataElements.size(), elementsFilter);
        editDivId = "addElementsDiv";

        boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());

        return new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
    }

    /**
     * Adds data element relation.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution addDataElement() throws ServiceException {
        Thread.currentThread().setName("ADD-DATAELEMENT");
        ActionMethodUtils.setLogParameters(getContext());
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
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution removeDataElement() throws ServiceException {
        Thread.currentThread().setName("REMOVE-DATAELEMENT");
        ActionMethodUtils.setLogParameters(getContext());
        vocabularyFolder =
                vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                        vocabularyFolder.isWorkingCopy());
        origIdentifier = vocabularyFolder.getIdentifier();
        validateView();
        if (!vocabularyFolder.isWorkingCopy()) {
            throw new ServiceException("Vocabulary should be in working copy status");
        }
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());

        vocabularyService.removeDataElement(vocabularyFolder.getId(), elementId);
        List<VocabularyConcept> concepts = vocabularyConceptService.getVocabularyConcepts(vocabularyFolder.getId());
        concepts.forEach(concept -> dataElementsService.deleteVocabularyConceptDataElementValues(concept.getId(), elementId));
        addSystemMessage("Data element removed");

        return resolution;
    }

    /**
     * Returns true if the current user is allowed to add new site codes.
     *
     * @return boolean
     */
    public boolean isCreateNewSiteCodeAllowed() {

        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUser(), "/sitecodes", "i");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * True, if user has update right.
     *
     * @return boolean
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
     * @return boolean
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
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution saveFolder() throws ServiceException {
        Thread.currentThread().setName("SAVE-VOCABULARY-FOLDER");
        ActionMethodUtils.setLogParameters(getContext());
        if (vocabularyFolder.getId() == 0) {
            if (copyId != 0) {
                String taskId = this.asyncTaskManager.executeAsync(VocabularyCreateCopyTask.class,
                        VocabularyCreateCopyTask.createParamsBundle(copyId, vocabularyFolder.getFolderId(), 
                                vocabularyFolder.getIdentifier(), vocabularyFolder.getLabel(),
                                vocabularyFolder.getBaseUri(), vocabularyFolder.getType(),
                                vocabularyFolder.isNumericConceptIdentifiers(), vocabularyFolder.isNotationsEqualIdentifiers(),
                                getUserName(), 
                                StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)? folder.getIdentifier() : null, 
                                StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)? folder.getLabel(): null));

                return AsyncTaskProgressActionBean.createAwaitResolution(taskId);
            } else {
                if (StringUtils.equals(FOLDER_CHOICE_EXISTING, folderChoice)) {
                    vocabularyService.createVocabularyFolder(vocabularyFolder, null, getUserName());
                }
                if (StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)) {
                    vocabularyService.createVocabularyFolder(vocabularyFolder, folder, getUserName());
                }
            }
        } else {
            if (StringUtils.equals(FOLDER_CHOICE_EXISTING, folderChoice)) {
                vocabularyService.updateVocabularyFolder(vocabularyFolder, null);
            }
            if (StringUtils.equals(FOLDER_CHOICE_NEW, folderChoice)) {
                vocabularyService.updateVocabularyFolder(vocabularyFolder, folder);
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
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution saveConcept() throws ServiceException {
        Thread.currentThread().setName("SAVE-VOCABULARY-CONCEPT");
        ActionMethodUtils.setLogParameters(getContext());
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());

        try {
            if (vocabularyConcept != null) {
                if(vocabularyService.checkIfConceptShouldBeAddedWhenBoundToElement(vocabularyFolder.getId(), vocabularyConcept.getNotation()) == false){
                    String errorMsg = "Concept without notation can not be created for vocabulary " + vocabularyFolder.getIdentifier() + " because it is referenced by data elements";
                    throw new ConceptWithoutNotationException(errorMsg);
                }

                // Save new concept
                vocabularyService.createVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept);
            } else {
                // Update existing concept
                VocabularyConcept fromForm = getEditableConcept();
                VocabularyConcept toUpdate = vocabularyService.getVocabularyConcept(fromForm.getId());
                toUpdate.setIdentifier(fromForm.getIdentifier());
                toUpdate.setLabel(fromForm.getLabel());
                toUpdate.setDefinition(fromForm.getDefinition());
                vocabularyService.quickUpdateVocabularyConcept(toUpdate);
                initFilter();
                resolution.addParameter("page", page);
                if (StringUtils.isNotEmpty(filter.getText())) {
                    resolution.addParameter("filter.text", filter.getText());
                }
            }

            addSystemMessage("Vocabulary concept saved successfully");
            return resolution;
        }
        catch(ConceptWithoutNotationException cwne){
            throw new ServiceException(cwne.getMessage(), cwne);
        }
    }

    /**
     * Action for checking in vocabulary folder.
     *
     * @return resolution
     */
    public Resolution checkIn() {
        Thread.currentThread().setName("VOCABULARY-CHECK-IN");
        ActionMethodUtils.setLogParameters(getContext());
        String taskId = this.asyncTaskManager.executeAsync(VocabularyCheckInTask.class,
                VocabularyCheckInTask.createParamsBundle(vocabularyFolder.getId(), getUserName(), 
                        vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier()));
        
        return AsyncTaskProgressActionBean.createAwaitResolution(taskId);
    }

    /**
     * Action for checking out vocabulary folder.
     *
     * @return resolution
     */
    public Resolution checkOut() {
        Thread.currentThread().setName("VOCABULARY-CHECK-OUT");
        ActionMethodUtils.setLogParameters(getContext());
        String taskId = this.asyncTaskManager.executeAsync(VocabularyCheckOutTask.class,
                VocabularyCheckOutTask.createParamsBundle(vocabularyFolder.getId(), getUserName(), 
                        vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier()));
        
        return AsyncTaskProgressActionBean.createAwaitResolution(taskId);
    }

    /**
     * Deletes the checked out version.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution undoCheckOut() throws ServiceException {
        Thread.currentThread().setName("VOCABULARY-UNDO-CHECKOUT");
        ActionMethodUtils.setLogParameters(getContext());
        String taskId = this.asyncTaskManager.executeAsync(VocabularyUndoCheckOutTask.class,
                VocabularyUndoCheckOutTask.createParamsBundle(vocabularyFolder.getId(), getUserName()));
        
        return AsyncTaskProgressActionBean.createAwaitResolution(taskId);
    }

    /**
     * Deletes vocabulary concepts.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution deleteConcepts() throws ServiceException {
        Thread.currentThread().setName("DELETE-VOCABULARY-CONCEPTS");
        ActionMethodUtils.setLogParameters(getContext());
        LOGGER.info("Deleting concepts with ids: " + conceptIds);
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
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution markConceptsInvalid() throws ServiceException {
        Thread.currentThread().setName("MARK-VOCABULARY-CONCEPTS-INVALID");
        ActionMethodUtils.setLogParameters(getContext());
        vocabularyService.markConceptsInvalid(conceptIds);
        addSystemMessage("Vocabulary concepts marked invalid");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * Removes the obsolete status from concepts.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
     */
    public Resolution markConceptsValid() throws ServiceException {
        Thread.currentThread().setName("MARK-VOCABULARY-CONCEPTS-VALID");
        ActionMethodUtils.setLogParameters(getContext());
        vocabularyService.markConceptsValid(conceptIds);
        addSystemMessage("Vocabulary concepts marked valid");
        RedirectResolution resolution = new RedirectResolution(VocabularyFolderActionBean.class, "edit");
        resolution.addParameter("vocabularyFolder.folderName", vocabularyFolder.getFolderName());
        resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
        resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
        return resolution;
    }

    /**
     * 
     * Display page to schedule a new synchronization of vocabulary or redirect to edit page if a scheduled job for this vocabulary already exists.
     *
     * @return resolution
     */
    public Resolution scheduleSynchronization() throws ServiceException, ResourceNotFoundException {
        vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), vocabularyFolder.isWorkingCopy());

        AsyncTaskExecutionEntry existingTaskEntry = asyncTaskDao.getVocabularyRdfImportTaskEntryByVocabularyName(vocabularyFolder.getIdentifier());
        if (existingTaskEntry != null) {
            this.scheduledTaskId = existingTaskEntry.getTaskId();
            return this.editScheduledJob();
        }

        setRdfPurgeOption(1);
        setSchedulingIntervalUnit(SchedulingIntervalUnit.DAY);
        return new ForwardResolution(SCHEDULE_VOCABULARY_SYNC);
    }
       
    /**
     * View A specific Scheduled Task's Details
     */
    public Resolution viewScheduledTaskDetails() throws ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = asyncTaskManager.getTaskEntry(scheduledTaskId);
        scheduledTaskView = new ScheduledTaskView();
        scheduledTaskView.setDetails(entry);
        scheduledTaskView.setType(scheduledTaskResolver.resolveTaskTypeFromTaskClassName(entry.getTaskClassName()));
        scheduledTaskView.setTaskParameters(asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters()));
        if (this.extractExceptionMessageFromErrorResult(entry.getSerializedResult()) != null) {
            scheduledTaskView.setTaskResult("Error:" +this.extractExceptionMessageFromErrorResult(entry.getSerializedResult()));
        } else {
            scheduledTaskView.setTaskResult(asyncTaskDataSerializer.deserializeResult(entry.getSerializedResult()));
        }
        return new ForwardResolution(SCHEDULED_TASK_DETAILS);
    }

    /**
     * View A specific Scheduled Task's History Details
     *
     */
    public Resolution viewScheduledTaskHistoryDetails() {
        AsyncTaskExecutionEntryHistory entryHistory = asyncTaskManager.getTaskEntryHistory(scheduledTaskHistoryId);
        scheduledTaskView = new ScheduledTaskView();
        scheduledTaskView.setDetails(entryHistory);
        scheduledTaskView.setType(scheduledTaskResolver.resolveTaskTypeFromTaskClassName(entryHistory.getTaskClassName()));
        scheduledTaskView.setTaskParameters(asyncTaskDataSerializer.deserializeParameters(entryHistory.getSerializedParameters()));
        if (this.extractExceptionMessageFromErrorResult(entryHistory.getSerializedResult()) != null) {
            scheduledTaskView.setTaskResult("Error: "+this.extractExceptionMessageFromErrorResult(entryHistory.getSerializedResult()));
        } else {
            scheduledTaskView.setTaskResult(asyncTaskDataSerializer.deserializeResult(entryHistory.getSerializedResult()));
        }
        return new ForwardResolution(SCHEDULED_TASK_HISTORY_DETAILS);
    }
    
    /**
     * View the page to edit a Scheduled Job 
     **/
    public Resolution editScheduledJob() throws ServiceException, ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = asyncTaskManager.getTaskEntry(scheduledTaskId);
        if (entry.getTaskClassName().equals(VOCABULARY_RDF_IMPORT_FROM_URL_TASK_CLASS_NAME)) {
            scheduledTaskView = new ScheduledTaskView();
            scheduledTaskView.setDetails(entry);
            scheduledTaskView.setType(scheduledTaskResolver.resolveTaskTypeFromTaskClassName(entry.getTaskClassName()));
            scheduledTaskView.setTaskParameters(asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters()));
            Map<String, Object> parameters = asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters());
            this.vocabularyRdfUrl = (String) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_RDF_FILE_URL);
            this.emails = (String) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_NOTIFIERS_EMAILS);
            this.rdfPurgeOption = Enumerations.VocabularyRdfPurgeOption.valueOf((String)parameters.get(VocabularyRdfImportFromUrlTask.PARAM_RDF_PURGE_OPTION)).getRdfPurgeOption();
            this.missingConceptsAction = (IVocabularyImportService.MissingConceptsAction) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_MISSING_CONCEPTS_ACTION);
            this.scheduleInterval = (Integer) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_SCHEDULE_INTERVAL);
            this.schedulingIntervalUnit = (SchedulingIntervalUnit) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_SCHEDULE_INTERVAL_UNIT);
            scheduledTaskView.setTaskResult(asyncTaskDataSerializer.deserializeResult(entry.getSerializedResult()));
            vocabularyFolder = vocabularyService.getVocabularyFolder((String) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_VOCABULARY_SET_IDENTIFIER),
                    (String) parameters.get(VocabularyRdfImportFromUrlTask.PARAM_VOCABULARY_IDENTIFIER), false);
        }
        return new ForwardResolution(EDIT_SCHEDULED_TASK_DETAILS);
    }

    public Resolution viewScheduledJobs() throws ServiceException {
        asyncTaskEntries = asyncTaskManager.getAllScheduledTaskEntries();
        for (AsyncTaskExecutionEntry entry : asyncTaskEntries) {
            ScheduledTaskView taskView = new ScheduledTaskView();
            taskView.setType(scheduledTaskResolver.resolveTaskTypeFromTaskClassName(entry.getTaskClassName()));
            taskView.setDetails(entry);
            taskView.setAdditionalDetails(scheduledTaskResolver.extractImportUrlFromVocabularyImportTask(entry));
            taskView.setTaskParameters(asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters()));
            futureScheduledTaskViews.add(taskView);
        }
        return new ForwardResolution(SCHEDULED_JOBS_VIEW);
    }
    
    /**
     *View all previous execution attempts of a scheduled job
     **/
    public Resolution viewScheduledJobHistory() throws ServiceException {
        asyncTaskEntriesHistory = asyncTaskManager.retrieveLimitedTaskHistoryByTaskId(scheduledTaskId, 10);
        for (AsyncTaskExecutionEntryHistory historyEntry : asyncTaskEntriesHistory) {
            ScheduledTaskView taskView = new ScheduledTaskView();
            taskView.setType(scheduledTaskResolver.resolveTaskTypeFromTaskClassName(historyEntry.getTaskClassName()));
            taskView.setDetails(historyEntry);
            taskView.setAdditionalDetails(scheduledTaskResolver.extractImportUrlFromVocabularyImportTask(historyEntry));
            taskView.setTaskParameters(asyncTaskDataSerializer.deserializeParameters(historyEntry.getSerializedParameters()));
            taskView.setAsyncTaskExecutionEntryHistoryId(historyEntry.getId());
            scheduledTaskHistoryViews.add(taskView);
        }
        return new ForwardResolution(SCHEDULED_TASK_HISTORY_VIEW);
    }

    
    /**
     * Schedule Synchronization of Vocabularies
     * @return resolution
     */
    public Resolution createScheduledJob() throws ServiceException {
           try {
            vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), vocabularyFolder.isWorkingCopy());
        } catch (ServiceException e) {
            e.setErrorParameter(ErrorActionBean.ERROR_TYPE_KEY, ErrorActionBean.ErrorType.INVALID_INPUT);
            throw e;
        } catch (Exception e) {
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }

        Map<String, Object> paramsBundle = VocabularyRdfImportFromUrlTask.createParamsBundle(
                vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), scheduleInterval, schedulingIntervalUnit, 
                vocabularyRdfUrl, emails, VocabularyRdfPurgeOption.translateRDFPurgeOptionNumberToEnum(rdfPurgeOption), missingConceptsAction);

        this.asyncTaskManager.scheduleTask(VocabularyRdfImportFromUrlTask.class, paramsBundle, schedulingIntervalUnit.getMinutes() * scheduleInterval);
        return new RedirectResolution(VocabularyFolderActionBean.class, "viewScheduledJobs");
    }

    /**
     * Update an existing scheduled Job
     * @return resolution
     */
    public Resolution updateScheduledJob() throws ServiceException {
           try {
            vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),vocabularyFolder.isWorkingCopy());
        } catch (ServiceException e) {
            e.setErrorParameter(ErrorActionBean.ERROR_TYPE_KEY, ErrorActionBean.ErrorType.INVALID_INPUT);
            throw e;
        } catch (Exception e) {
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }

        Map<String, Object> paramsBundle = VocabularyRdfImportFromUrlTask.createParamsBundle(
                vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), scheduleInterval, schedulingIntervalUnit, 
                vocabularyRdfUrl, emails, VocabularyRdfPurgeOption.translateRDFPurgeOptionNumberToEnum(rdfPurgeOption), missingConceptsAction);

        this.asyncTaskManager.updateScheduledTask(VocabularyRdfImportFromUrlTask.class, paramsBundle, schedulingIntervalUnit.getMinutes() * scheduleInterval, scheduledTaskId);
        return new RedirectResolution(VocabularyFolderActionBean.class, "viewScheduledJobs");
    }

    /**
     * Delete a Scheduled Job 
     **/
    public Resolution deleteScheduledJob(){
        asyncTaskManager.deleteTask(scheduledTaskId);
        return new RedirectResolution(VocabularyFolderActionBean.class, "viewScheduledJobs");
    }


    /**
     * Validates check out.
     *
     * @throws ServiceException
     *             if an error occurs
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
     *             if an error occurs
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
     * Validation on adding a bound data element.
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

            boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
            Resolution resolution = new ForwardResolution(EDIT_VOCABULARY_FOLDER_JSP);
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     * Validates save folder.
     *
     * @throws ServiceException
     *             if an error occurs
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
            if (!Util.isValidUri(vocabularyFolder.getBaseUri())) {
                addGlobalValidationError("Base URI is not a valid URI. \n The allowed schemes are: "
                        + "http, https, ftp, mailto, tel and urn. ");
            }
        }

        if (vocabularyFolder.isSiteCodeType() && !vocabularyFolder.isNumericConceptIdentifiers()) {
            addGlobalValidationError("Site code type vocabulary must have numeric concept identifiers");
        }

        // Validate unique identifier
        if (vocabularyFolder.getId() == 0) {
            int folderId = FOLDER_CHOICE_NEW.equalsIgnoreCase(folderChoice) ? folder.getId() : vocabularyFolder.getFolderId();

            if (!vocabularyService.isUniqueVocabularyFolderIdentifier(folderId, vocabularyFolder.getIdentifier())) {
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
            boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        }
    }

    /**
     * Because not all the properties of dynamic attributes get submitted by form (meta data), but only values, we don't have enough
     * data to do validation and re-displaying the attributes on the form when validation errors occour. This method loads the
     * attributes metadata from database and merges them with the submitted attributes.
     *
     * @throws ServiceException
     *             if an error occurs
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
     *            simple attribute 1
     * @param attributeValue
     *            simple attribute 2
     * @return simple attribute after merge
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
     *             if an error occurs
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

        //check for dates, they cannot be set to future
        Date today = new Date(System.currentTimeMillis());

        if (vc.getStatusModified() != null && today.before(vc.getStatusModified() )){
            addGlobalValidationError("Status modified date cannot be set to future");
        }

        if (vc.getAcceptedDate() != null && today.before(vc.getAcceptedDate())){
            addGlobalValidationError("Accepted date cannot be set to future");
        }

        if (vc.getNotAcceptedDate() != null && today.before(vc.getNotAcceptedDate())){
            addGlobalValidationError("Not accepted date cannot be set to future");
        }

        if (isValidationErrors()) {
            vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getId());
            initFilter();
            vocabularyConcepts = vocabularyService.searchVocabularyConcepts(filter);
            boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        }
    }

    
    /***
     * Validate Schedule Synchronization of a Vocabulary 
     **/
    @ValidationMethod(on = {"createScheduledJob", "updateScheduledJob"})
    public void ValidateCreateOrUpdateScheduledJob() throws ServiceException {
        if (vocabularyFolder.getId() == 0) {
            if (!isCreateRight()) {
                addGlobalValidationError("No permission to create new vocabulary");
            }
        } else if (!isUpdateRight()) {
            addGlobalValidationError("No permission to modify vocabulary");
        }
        if (StringUtils.isNotEmpty(vocabularyRdfUrl)) {
            if (!Util.isURL(vocabularyRdfUrl)) {
                addGlobalValidationError("Provided Vocabulary Rdf URL is not a valid URL. \n The allowed schemes are: "
                        + "http, https, ftp, mailto, tel and urn. ");
            }
        } else {
            addGlobalValidationError("RDF URL cannot be Empty.");
        }
        if (StringUtils.isEmpty(emails)) {
            addGlobalValidationError("You should provide at least one valid Email Address");
        }
        if (StringUtils.isNotEmpty(emails)) {
            String[] emailsArray = emails.split(",");
            for (String email : emailsArray) {
                if (!emailValidator.isValid(email)) {
                    addGlobalValidationError("Email Address:" + email + " is not valid");
                }
            }
        }
        if (scheduleInterval == null || scheduleInterval == 0) {
            addGlobalValidationError("Please Specify a valid number for Schedule Intervals");
        }
        if (isValidationErrors()) {
            folders = vocabularyService.getFolders(getUserName(), null);
            initFilter();
        }
    }

    /**
     * Navigates to vocabulary folders list.
     *
     * @return resolution
     */
    public Resolution cancelAdd() {
        return new RedirectResolution(VocabularyFoldersActionBean.class);
    }

    /**
     * Navigates to edit vocabulary folder page.
     *
     * @return resolution
     * @throws ServiceException
     *             if an error occurs
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
     * @return resolution
     */
    public Resolution rdf() {
        try {
            vocabularyFolder =
                    getVocabularyService().getVocabularyFolder(getVocabularyFolder().getFolderName(), getVocabularyFolder().getIdentifier(),
                            false);
            LOGGER.info(String.format("Exporting rdf for vocabulary #%d", vocabularyFolder.getId()));

            if (vocabularyFolder.isDraftStatus()) {
                throw new RuntimeException("Vocabulary is not in released or public draft status.");
            }

            List<VocabularyFolder> vocabularyFolders = new ArrayList<VocabularyFolder>();
            vocabularyFolders.add(vocabularyFolder);
            final List<RdfNamespace> nameSpaces = getVocabularyService().getVocabularyNamespaces(vocabularyFolders);
            LOGGER.info(String.format("Retrieved namespaces for vocabulary #%d", vocabularyFolder.getId()));

            final List<? extends VocabularyConcept> concepts = getVocabularyService().getAllConceptsWithAttributes(vocabularyFolder.getId());
            LOGGER.info(String.format("Retrieved concepts for vocabulary #%d", vocabularyFolder.getId()));
            final String contextRoot = VocabularyFolder.getBaseUri(vocabularyFolder);

            final String folderContextRoot =
                    Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + vocabularyFolder.getFolderName() + "/";

            final String commonElemsUri = Props.getRequiredProperty(PropsIF.DD_URL) + "/property/";

            StreamingResolution result = new StreamingResolution("application/rdf+xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyXmlWriter xmlWriter = new VocabularyXmlWriter(response.getOutputStream());
                    xmlWriter.writeRDFXml(commonElemsUri, folderContextRoot, contextRoot, vocabularyFolder, concepts, nameSpaces);
                }
            };
            result.setFilename(vocabularyFolder.getIdentifier() + ".rdf");
            LOGGER.info(String.format("Finishing exporting rdf for vocabulary #%d", vocabularyFolder.getId()));
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary RDF data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

    /**
     * Codelist output in INSPIRE format.
     *
     * @return xml output
     */
    public Resolution codelist() {
        try {

            vocabularyFolder =
                    vocabularyService
                            .getVocabularyWithConcepts(vocabularyFolder.getIdentifier(), vocabularyFolder.getFolderName());

            final String folderContextRoot = Props.getRequiredProperty(PropsIF.DD_URL);

            StreamingResolution result = new StreamingResolution("application/xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    InspireCodelistXmlWriter xmlWriter =
                            new InspireCodelistXmlWriter(response.getOutputStream(), vocabularyFolder, folderContextRoot);
                    xmlWriter.writeXml();
                }
            };
            result.setFilename(vocabularyFolder.getIdentifier() + ".xml");
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary XML data in ISPIRE format", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

    /**
     * Returns vocabulary concepts CSV.
     *
     * @return resolution
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

            final String folderContextRoot = VocabularyFolder.getBaseUri(vocabularyFolder);
            final List<VocabularyConcept> concepts = vocabularyService.getAllConceptsWithAttributes(vocabularyFolder.getId());
            final List<Triple<String, String, Integer>> boundElementsWithLanguage =
                    vocabularyService.getVocabularyBoundElementNamesByLanguage(concepts);

            StreamingResolution result = new StreamingResolution("text/csv") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyCSVOutputHelper.writeCSV(response.getOutputStream(), getUriPrefix(), folderContextRoot, concepts,
                            boundElementsWithLanguage);
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
    } // end of method csv

    /**
     * Imports CSV contents into vocabulary.
     *
     * @return resolution
     * @throws ServiceException
     *             when an error occurs
     */
    public Resolution uploadCsv() throws ServiceException {
        Thread.currentThread().setName("CSV-UPLOAD");
        ActionMethodUtils.setLogParameters(getContext());
        try {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());
            validateView();
            if (!vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Vocabulary should be in working copy status");
            }

            if (this.uploadedFileToImport == null) {
                throw new ServiceException("You should upload a file");
            }

            FileBean importFileBean = null;
            
            try {
                importFileBean = this.prepareImportSource();
                String fileName = importFileBean.getFileName();
                
                if (StringUtils.isEmpty(fileName) || !fileName.toLowerCase().endsWith(VocabularyFolderActionBean.CSV_FILE_EXTENSION)) {
                    throw new ServiceException("File should be a CSV file");
                }

                File tmpCsvFile = File.createTempFile(fileName, ".tmp");
                /* The temp file will be deleted, because due to the upgrade of apache commons-fileupload lib from 1.2.2 to 1.4,
                 the importFileBean.save(tmpCsvFile) command will recreate the tmp file and an exception will be thrown since the file is existing.*/
                tmpCsvFile.delete();
                importFileBean.save(tmpCsvFile);

                String taskId = this.asyncTaskManager.executeAsync(VocabularyCsvImportTask.class,
                        VocabularyCsvImportTask.createParamsBundle(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), 
                                vocabularyFolder.isWorkingCopy(), tmpCsvFile.getAbsolutePath(), purgeVocabularyData, purgeBoundElements));

                return AsyncTaskProgressActionBean.createAwaitResolution(taskId);
             }
             finally {
                /* 
                 * When FileBean.save() isn't called, FileBean.delete() must be called so that the
                 * uploaded file is removed from disk.
                 * See: https://stripesframework.atlassian.net/wiki/display/STRIPES/File+Uploads
                 */
                if (importFileBean != this.uploadedFileToImport) {
                    this.uploadedFileToImport.delete();
                }
            }
        } catch (ServiceException e) {
            LOGGER.error("Failed to import vocabulary CSV into db", e);
            e.setErrorParameter(ErrorActionBean.ERROR_TYPE_KEY, ErrorActionBean.ErrorType.INVALID_INPUT);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to import vocabulary CSV into db, unexpected exception: ", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    } // end of method uploadCsv

    /**
     * Imports RDF contents into vocabulary.
     *
     * @return resolution
     * @throws ServiceException
     *             when an error occurs
     */
    public Resolution uploadRdf() throws ServiceException {
        Thread.currentThread().setName("RDF-IMPORT");
        ActionMethodUtils.setLogParameters(getContext());
        try {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());
            validateView();
            if (!vocabularyFolder.isWorkingCopy()) {
                throw new ServiceException("Vocabulary should be in working copy status");
            }

            if (this.uploadedFileToImport == null) {
                throw new ServiceException("You should upload a file");
            }
         
            FileBean importFileBean = null;
            
            try {
                importFileBean = this.prepareImportSource();
                String fileName = importFileBean.getFileName();
                
                if (StringUtils.isEmpty(fileName) || !fileName.toLowerCase().endsWith(VocabularyFolderActionBean.RDF_FILE_EXTENSION)) {
                    throw new ServiceException("File should be a RDF file");
                }

                File tmpRdfFile = File.createTempFile(fileName, ".tmp");
                /* The temp file will be deleted, because due to the upgrade of apache commons-fileupload lib from 1.2.2 to 1.4,
                 the importFileBean.save(tmpRdfFile) command will recreate the tmp file and an exception will be thrown since the file is existing.*/
                tmpRdfFile.delete();
                importFileBean.save(tmpRdfFile);

                String taskId = this.asyncTaskManager.executeAsync(VocabularyRdfImportTask.class,
                        VocabularyRdfImportTask.createParamsBundle(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), 
                                vocabularyFolder.isWorkingCopy(), tmpRdfFile.getAbsolutePath(), rdfPurgeOption, missingConceptsAction));

                return AsyncTaskProgressActionBean.createAwaitResolution(taskId);
            }
            finally {
                /* 
                 * When FileBean.save() isn't called, FileBean.delete() must be called so that the
                 * uploaded file is removed from disk.
                 * See: https://stripesframework.atlassian.net/wiki/display/STRIPES/File+Uploads
                 */
                if (this.uploadedFileToImport != importFileBean) {
                    this.uploadedFileToImport.delete();
                }
            }
        } catch (ServiceException e) {
            LOGGER.error("Failed to import vocabulary RDF into db", e);
            e.setErrorParameter(ErrorActionBean.ERROR_TYPE_KEY, ErrorActionBean.ErrorType.INVALID_INPUT);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to import vocabulary RDF into db, unexpected exception: ", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    } // end of method uploadRDF

    /**
     * Forwards to vocabulary concept page, if the url pattern is: /vocabulary/folderName/folderIdentifier/conceptIdentifier.
     *
     * @return resolution
     */
    private Resolution getVocabularyConceptResolution() {
        HttpServletRequest httpRequest = getContext().getRequest();
        String url = httpRequest.getRequestURL().toString();

        String[] parameters = StringUtils.split(StringUtils.substringAfter(url, "/vocabulary/"), "/");

        if (parameters.length >= 3) {
            // check for possible server url rewrite
            if (parameters[2].contains(";jsessionid")) {
                parameters[2] = parameters[2].split(";jsessionid")[0];
            }
            if (!RESERVED_VOCABULARY_EVENTS.contains(parameters[2])) {
                RedirectResolution resolution = new RedirectResolution(VocabularyConceptActionBean.class, "view");
                resolution.addParameter("vocabularyFolder.folderName", UriUtils.decode(parameters[0], "utf-8"));
                resolution.addParameter("vocabularyFolder.identifier", UriUtils.decode(parameters[1], "utf-8"));
                resolution.addParameter("vocabularyConcept.identifier", UriUtils.decode(parameters[2], "utf-8"));
                if (vocabularyFolder.isWorkingCopy()) {
                    resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
                }
                return resolution;
            }
        }
        return null;
    }

    /**
     * Returns vocabulary concepts json.
     *
     * @return resolution
     */
    public Resolution json() {
        try {
            vocabularyFolder =
                    vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(),
                            vocabularyFolder.isWorkingCopy());

            if (vocabularyFolder.isDraftStatus()) {
                throw new RuntimeException("Vocabulary is not in released or public draft status.");
            }

            if (StringUtils.isBlank(format)) {
                format = JSON_DEFAULT_OUTPUT_FORMAT;
            }

            if (!SUPPORTED_JSON_FORMATS.contains(format)) {
                throw new RuntimeException("Unsupported JSON output format");
            }

            lang = StringUtils.trimToNull(lang);
            id = StringUtils.trimToNull(id);
            label = StringUtils.trimToNull(label);

            LOGGER.info("JSON CALL RECEIVED FOR: " + vocabularyFolder.getIdentifier() + ", with parameters: lang = " + lang);

            final List<VocabularyConcept> concepts = vocabularyService.getConceptsWithAttributes(vocabularyFolder.getId(), id, label, true);

            StreamingResolution result = new StreamingResolution(format) {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyJSONOutputHelper.writeJSON(response.getOutputStream(), vocabularyFolder, concepts, lang);
                }
            };

            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to output vocabulary as JSON-LD", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    } // end of method json

    protected boolean isZipFileName(String filename) {
        return StringUtils.endsWithIgnoreCase(filename, ".zip");
    }
    
    protected FileBean prepareImportSource() throws IOException, BadFormatException {
        if (this.isZipFileName(this.uploadedFileToImport.getFileName())) {
            return this.fileBeanDecompressor.unzip(this.uploadedFileToImport);
        }
        
        return this.uploadedFileToImport;
    }
    
    /**
     * Returns concept URI prefix.
     *
     * @return string
     */
    public String getUriPrefix() {
        String baseUri = VocabularyFolder.getBaseUri(vocabularyFolder);
        return StringEncoder.encodeToIRI(baseUri);
    }

    /**
     * Initiates filter correct with parameters.
     */
    private void initFilter() {
        if (filter == null) {
            filter = new VocabularyConceptFilter();
            filter.setConceptStatus(StandardGenericStatus.ACCEPTED);
            filter.setVisibleColumns(Arrays.asList(new String[] {
                "Notation", "Status", "Status Modified"
            }));
        }
        filter.setVocabularyFolderId(vocabularyFolder.getId());
        filter.setPageNumber(page);
        filter.setNumericIdentifierSorting(vocabularyFolder.isNumericConceptIdentifiers());
    }

    /**
     * True, if logged in user is the working user of the vocabulary.
     *
     * @return boolean
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
     * @return boolean
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
     * @return boolean
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
     * @return boolean
     */
    public String getNextIdentifier() {
        if (!vocabularyFolder.isNumericConceptIdentifiers()) {
            return "";
        } else {
            try {
                int identifier = vocabularyService.getNextIdentifierValue(vocabularyFolder.getId());
                return Integer.toString(identifier);
            } catch (ServiceException e) {
                LOGGER.error(e.getMessage(), e);
                return "";
            }
        }
    }

    /**
     * Returns the vocabulary concept that is submitted by form for update.
     *
     * @return vocabulary concept
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

    /**\
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
     * @return the boundElements
     */
    public List<DataElement> getBoundElements() {

        return boundElements;
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
     * @param uploadedFileToImport
     *            the uploadedFile to set
     */
    public void setUploadedFileToImport(FileBean uploadedFileToImport) {
        this.uploadedFileToImport = uploadedFileToImport;
    }

    /**
     * @param purgeVocabularyData
     *            purge before importing csv
     */
    public void setPurgeVocabularyData(boolean purgeVocabularyData) {
        this.purgeVocabularyData = purgeVocabularyData;
    }

    /**
     * @param purgeBoundElements
     *            purge before importing csv
     */
    public void setPurgeBoundElements(boolean purgeBoundElements) {
        this.purgeBoundElements = purgeBoundElements;
    }

    public void setRdfPurgeOption(int rdfPurgeOption) {
        this.rdfPurgeOption = rdfPurgeOption;
    }

    public int getRdfPurgeOption() {
        return rdfPurgeOption;
    }

    public IVocabularyImportService.MissingConceptsAction getMissingConceptsAction() {
        return missingConceptsAction;
    }

    public void setMissingConceptsAction(IVocabularyImportService.MissingConceptsAction missingConceptsAction) {
        this.missingConceptsAction = missingConceptsAction;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getBoundElementFilterIndex() {
        return boundElementFilterIndex;
    }

    public void setBoundElementFilterIndex(int boundElementFilterIndex) {
        this.boundElementFilterIndex = boundElementFilterIndex;
    }

    public int getBoundElementFilterId() {
        return boundElementFilterId;
    }

    public void setBoundElementFilterId(int boundElementFilterId) {
        this.boundElementFilterId = boundElementFilterId;
    }

    public int getVocabularyFolderId() {
        return vocabularyFolderId;
    }

    public void setVocabularyFolderId(int vocabularyFolderId) {
        this.vocabularyFolderId = vocabularyFolderId;
    }

    public VocabularyConceptBoundElementFilter getBoundElementFilter() {
        return boundElementFilter;
    }

    public void setBoundElementFilter(VocabularyConceptBoundElementFilter boundElementFilter) {
        this.boundElementFilter = boundElementFilter;
    }

    public List<VocabularyConceptBoundElementFilter> getBoundElementFilters() {
        return boundElementFilters;
    }

    public void setBoundElementFilters(List<VocabularyConceptBoundElementFilter> boundElementFilters) {
        this.boundElementFilters = boundElementFilters;
    }

    public List<Integer> getBoundElementFilterIds() {
        List<Integer> boundElementFilterIds = new ArrayList<Integer>();
        for (VocabularyConceptBoundElementFilter currentFilter : boundElementFilters) {
            boundElementFilterIds.add(currentFilter.getId());
        }
        return boundElementFilterIds;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void setSchedulingIntervalUnit(SchedulingIntervalUnit schedulingIntervalUnit) {
        this.schedulingIntervalUnit = schedulingIntervalUnit;
    }

    public SchedulingIntervalUnit getSchedulingIntervalUnit() {
        return schedulingIntervalUnit;
    }

    public String getVocabularyRdfUrl() {
        return vocabularyRdfUrl;
    }

    public void setVocabularyRdfUrl(String vocabularyRdfUrl) {
        this.vocabularyRdfUrl = vocabularyRdfUrl;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public Integer getScheduleInterval() {
        return scheduleInterval;
    }

    public void setScheduleInterval(Integer scheduleInterval) {
        this.scheduleInterval= scheduleInterval;
    }

    public List<AsyncTaskExecutionEntry> getAsyncTaskEntries() {
        return asyncTaskEntries;
    }

    public void setAsyncTaskEntries(List<AsyncTaskExecutionEntry> asyncTaskEntries) {
        this.asyncTaskEntries = asyncTaskEntries;
    }

    public List<AsyncTaskExecutionEntryHistory> getAsyncTaskEntriesHistory() {
        return asyncTaskEntriesHistory;
    }

    public void setAsyncTaskEntriesHistory(List<AsyncTaskExecutionEntryHistory> asyncTaskEntriesHistory) {
        this.asyncTaskEntriesHistory = asyncTaskEntriesHistory;
    }


    public List<ScheduledTaskView> getFutureScheduledTaskViews() {
        return futureScheduledTaskViews;
    }

    public void setFutureScheduledTaskViews(List<ScheduledTaskView> futureScheduledTaskViews) {
        this.futureScheduledTaskViews = futureScheduledTaskViews;
    }


    public List<ScheduledTaskView> getScheduledTaskHistoryViews() {
        return scheduledTaskHistoryViews;
    }

    public void setScheduledTaskHistoryViews(List<ScheduledTaskView> scheduledTaskHistoryViews) {
        this.scheduledTaskHistoryViews = scheduledTaskHistoryViews;
    }

    public String getScheduledTaskId() {
        return scheduledTaskId;
    }

    public void setScheduledTaskId(String scheduledTaskId) {
        this.scheduledTaskId = scheduledTaskId;
    }

    public ScheduledTaskView getScheduledTaskView() {
        return scheduledTaskView;
    }

    public void setScheduledTaskView(ScheduledTaskView scheduledTaskView) {
        this.scheduledTaskView = scheduledTaskView;
    }

    public String getErrorTypeMsg() {
        return errorTypeMsg;
    }

    public void setErrorTypeMsg(String errorTypeMsg) {
        this.errorTypeMsg = errorTypeMsg;
    }

    public String getScheduledTaskHistoryId() {
        return scheduledTaskHistoryId;
    }

    public void setScheduledTaskHistoryId(String scheduledTaskHistoryId) {
        this.scheduledTaskHistoryId = scheduledTaskHistoryId;
    }

    /**
     * @param taskResult the desirialised Async Task Result
     * @return the Exception error message, or null if no exception or error
     * message found in the Task Result.
     *
     */
    public String extractExceptionMessageFromErrorResult(Object taskResult) {
        Gson gson = new Gson();
        try {
            Map<String, String> results = gson.fromJson((String) taskResult, Map.class);

            if (results != null && results.get("@class").equals(AsyncTaskExecutionError.class.getCanonicalName())) {
                return results.get(AsyncTaskExecutionError.MESSAGE);
            } else {
                return null;
            }
        } catch (JsonSyntaxException e) {
            LOGGER.info("Async Task Result Not in Json Format");
        } catch (Exception e) {
            LOGGER.info("Async Task Result Not in Json Format");
        }
        return null;
    }

    public Integer getVocabularyBinding(int attributeId) {
       return attributeDataService.getVocabularyBinding(attributeId);
    }

    public List<VocabularyConcept> getVocabularyConcepts(int attributeId) throws ResourceNotFoundException, EmptyParameterException {
        DataDictEntity ddEntity = new DataDictEntity(vocabularyFolder.getId(), DataDictEntity.Entity.VCF);
        return attributeService.getAttributeVocabularyConcepts(attributeId, ddEntity, Attribute.ValueInheritanceMode.NONE);
    }

    public VocabularyFolder getVocabularyBindingFolder(int attributeId) throws ServiceException {
        Integer boundVocabularyId = getVocabularyBinding(attributeId);
        if (boundVocabularyId != null) {
            return vocabularyService.getVocabularyFolder(boundVocabularyId);
        }
        return null;
    }



}
