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
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.web.action;

import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.time.StopWatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Vocabulary folder API action bean.
 *
 * @author enver
 */
@UrlBinding("/api/vocabulary/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{$event}")
public class VocabularyFolderApiActionBean extends AbstractActionBean {

    /**
     * Before actions for file upload operations. Can be extended in the future for other file operations as well.
     */
    public static enum UploadActionBefore {
        keep, remove
    }

    /**
     * Actions for file upload operations. Can be extended in the future for other file operations as well.
     */
    public static enum UploadAction {
        add, delete
    }

    /**
     * Missing concept actions for file upload operations. Can be extended in the future for other file operations.
     */
    public static enum MissingConceptsAction {
        keep, remove, invalid, deprecated, retired, superseded
    }

    /**
     * Reserved API names, that cannot be vocabulary concept identifiers.
     */
    public static final List<String> RESERVED_VOCABULARY_API_EVENTS;

    /**
     * Supported RDF upload action before.
     */
    public static final List<UploadActionBefore> RDF_UPLOAD_SUPPORTED_ACTION_BEFORE;

    /**
     * RDF upload default action before value.
     */
    public static final UploadActionBefore RDF_UPLOAD_DEFAULT_ACTION_BEFORE = UploadActionBefore.keep;

    /**
     * Supported RDF upload action.
     */
    public static final List<UploadAction> RDF_UPLOAD_SUPPORTED_ACTION;

    /**
     * RDF upload default action value.
     */
    public static final UploadAction RDF_UPLOAD_DEFAULT_ACTION = UploadAction.add;

    /**
     * Supported RDF upload missing concepts action.
     */
    public static final List<MissingConceptsAction> RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION;

    /**
     * RDF upload default action before value.
     */
    public static final MissingConceptsAction RDF_UPLOAD_DEFAULT_MISSING_CONCEPTS_ACTION = MissingConceptsAction.keep;

    /**
     * Static block for initializations.
     */
    static {
        //Create supported/reserved api names
        RESERVED_VOCABULARY_API_EVENTS = new ArrayList<String>();
        RESERVED_VOCABULARY_API_EVENTS.add("uploadRdf");

        //Add RDF upload params
        RDF_UPLOAD_SUPPORTED_ACTION_BEFORE = new ArrayList<UploadActionBefore>();
        RDF_UPLOAD_SUPPORTED_ACTION_BEFORE.add(UploadActionBefore.keep);
        RDF_UPLOAD_SUPPORTED_ACTION_BEFORE.add(UploadActionBefore.remove);

        RDF_UPLOAD_SUPPORTED_ACTION = new ArrayList<UploadAction>();
        RDF_UPLOAD_SUPPORTED_ACTION.add(UploadAction.add);
        RDF_UPLOAD_SUPPORTED_ACTION.add(UploadAction.delete);

        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION = new ArrayList<MissingConceptsAction>();
        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.keep);
        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.remove);
        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.invalid);
        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.deprecated);
        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.retired);
        RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION.add(MissingConceptsAction.superseded);
    } // end of static block

    /**
     * Extension for RDF files.
     */
    private static final String RDF_FILE_EXTENSION = ".rdf";

    /**
     * Json output format.
     */
    public static final String JSON_FORMAT = "application/json";

    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;

    /**
     * Vocabulary folder.
     */
    private VocabularyFolder vocabularyFolder;

    /**
     * RDF Import Service.
     */
    @SpringBean
    private IRDFVocabularyImportService vocabularyRdfImportService;

    /**
     * Uploaded csv/rdf file to import into vocabulary.
     */
    //TODO - obsolete
    //KL 151216
    private FileBean sourceFile;

    /**
     * Action before param.
     */
    private String actionBefore;

    /**
     * Action param.
     */
    private String action;

    /**
     * Missing concepts action param.
     */
    private String missingConcepts;

    /**
     * Imports RDF contents into vocabulary.
     *
     * @return resolution
     * @throws eionet.meta.service.ServiceException when an error occurs
     */
    public Resolution uploadRdf() throws ServiceException {
        try {
            StopWatch timer = new StopWatch();
            timer.start();

            //KL151216 Read RDF from request body and params from url
            HttpServletRequest request = getContext().getRequest();

            //KL151216
            //TODO redesing: no need for bean attrs, put request param names to constants etc
            if (this.actionBefore == null && request.getParameter("actionBefore") != null) {
                setActionBefore(request.getParameter("actionBefore"));
            }

            if (this.missingConcepts == null && request.getParameter("missingConcepts") != null) {
                setActionBefore(request.getParameter("missingConcepts"));
            }

            if (this.missingConcepts == null && request.getParameter("uploadAction") != null) {
                setActionBefore(request.getParameter("uploadAction"));
            }

            //Validate parameters
            UploadActionBefore uploadActionBefore = validateAndGetUploadActionBefore(RDF_UPLOAD_SUPPORTED_ACTION_BEFORE, RDF_UPLOAD_DEFAULT_ACTION_BEFORE);
            
            //KL151216 - why these 2 are not used?
            UploadAction uploadAction = validateAndGetUploadAction(RDF_UPLOAD_SUPPORTED_ACTION, RDF_UPLOAD_DEFAULT_ACTION);
            MissingConceptsAction missingConceptsAction = validateAndGetMissingConceptsAction(RDF_UPLOAD_SUPPORTED_MISSING_CONCEPTS_ACTION, RDF_UPLOAD_DEFAULT_MISSING_CONCEPTS_ACTION);


            //TODO - FileBean obsolete
//KL 151216
/*
            if (this.sourceFile == null) {
                return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, "Source file is mandatory for import operation", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            String fileName = this.sourceFile.getFileName();
            if (StringUtils.isEmpty(fileName) ||
                    !fileName.toLowerCase().endsWith(VocabularyFolderApiActionBean.RDF_FILE_EXTENSION)) {
                return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, "File should be an RDF file for import API", ErrorActionBean.RETURN_ERROR_EVENT);
            }
*/

            VocabularyFolder workingCopy = null;
            try {
                workingCopy = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(),
                        vocabularyFolder.getIdentifier(), true);
            } catch (ServiceException e) {
                HashMap<String, Object> errorParameters = e.getErrorParameters();
                if (errorParameters == null ||
                        !ErrorActionBean.ErrorType.NOT_FOUND_404.equals(errorParameters.get(ErrorActionBean.ERROR_TYPE_KEY))) {
                    return super.createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, "Vocabulary should NOT have a working copy: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
                }
            }

            if (workingCopy != null && workingCopy.isWorkingCopy()) {
                return super.createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, "Vocabulary should NOT have a working copy", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            try {
                vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(),
                        vocabularyFolder.getIdentifier(), false);
            } catch (ServiceException e) {
                HashMap<String, Object> errorParameters = e.getErrorParameters();
                if (errorParameters != null &&
                        ErrorActionBean.ErrorType.NOT_FOUND_404.equals(errorParameters.get(ErrorActionBean.ERROR_TYPE_KEY))) {
                    return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, "Vocabulary can NOT be found: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
                }
            }

            if (vocabularyFolder.isWorkingCopy()) {
                return super.createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, "Vocabulary should NOT have a working copy", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            LOGGER.debug("Starting API RDF import operation");
            //KL 151216
            //Reader rdfFileReader = new InputStreamReader(this.sourceFile.getInputStream(), CharEncoding.UTF_8);
            Reader rdfFileReader = new InputStreamReader(request.getInputStream(), CharEncoding.UTF_8);

            boolean purgeVocabularyData = UploadActionBefore.remove.equals(uploadActionBefore);
            boolean purgePredicateBasis = false;

            final List<String> systemMessages = this.vocabularyRdfImportService.importRdfIntoVocabulary(rdfFileReader,
                    vocabularyFolder, purgeVocabularyData, purgePredicateBasis);
            for (String systemMessage : systemMessages) {
                addSystemMessage(systemMessage);
                LOGGER.info(systemMessage);
            }

            StreamingResolution result = new StreamingResolution(JSON_FORMAT) {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyJSONOutputHelper.writeJSON(response.getOutputStream(), systemMessages);
                }
            };

            timer.stop();
            LOGGER.debug("API RDF import completed in : " + timer.toString());
            return result;
        } catch (ServiceException e) {
            LOGGER.error("Failed to import vocabulary RDF into db", e);
            return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        } catch (Exception e) {
            LOGGER.error("Failed to import vocabulary RDF into db, unexpected exception: ", e);
            return super.createErrorResolution(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR, "Failed to import vocabulary RDF into db, unexpected exception: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        }
    } // end of method uploadRDF

    /**
     * Validator and enum value converter for upload action before parameter.
     *
     * @param supportedUploadActionBefore supported action before list for this upload operation.
     * @param defaultValue                a default value if this action before param missing.
     * @return Converted enum value
     * @throws ServiceException when parameter is invalid or not supported.
     */
    private UploadActionBefore validateAndGetUploadActionBefore(List<UploadActionBefore> supportedUploadActionBefore,
                                                                UploadActionBefore defaultValue) throws ServiceException {
        if (defaultValue != null && (this.actionBefore == null || this.actionBefore.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            UploadActionBefore uploadActionBefore = UploadActionBefore.valueOf(this.actionBefore);
            if (supportedUploadActionBefore != null && !supportedUploadActionBefore.contains(uploadActionBefore)) {
                throw new ServiceException("Not supported action before parameter: " + this.actionBefore);
            }
            return uploadActionBefore;
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid action before parameter: " + this.actionBefore, e);
        }
    } // end of method validateUploadActionBefore

    /**
     * Validator and enum value converter for upload action parameter.
     *
     * @param supportedUploadAction supported action list for this upload operation.
     * @param defaultValue          a default value if this action param missing.
     * @return Converted enum value
     * @throws ServiceException when parameter is invalid or not supported.
     */
    private UploadAction validateAndGetUploadAction(List<UploadAction> supportedUploadAction, UploadAction defaultValue)
            throws ServiceException {
        if (defaultValue != null && (this.action == null || this.action.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            UploadAction uploadAction = UploadAction.valueOf(this.action);
            if (supportedUploadAction != null && !supportedUploadAction.contains(uploadAction)) {
                throw new ServiceException("Not supported action parameter: " + this.action);
            }
            return uploadAction;
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid action parameter: " + this.action, e);
        }
    } // end of method validateAndGetUploadAction

    /**
     * Validator and enum value converter for upload missing concepts action parameter.
     *
     * @param supportedMissingConceptsAction supported missing concepts action list for this upload operation.
     * @param defaultValue                   a default value if this missing concepts action param missing.
     * @return Converted enum value
     * @throws ServiceException when parameter is invalid or not supported.
     */
    private MissingConceptsAction validateAndGetMissingConceptsAction(List<MissingConceptsAction> supportedMissingConceptsAction,
                                                                      MissingConceptsAction defaultValue) throws ServiceException {
        if (defaultValue != null && (this.missingConcepts == null || this.missingConcepts.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            MissingConceptsAction missingConceptsAction = MissingConceptsAction.valueOf(this.missingConcepts);
            if (supportedMissingConceptsAction != null && !supportedMissingConceptsAction.contains(missingConceptsAction)) {
                throw new ServiceException("Not supported missing concepts action parameter: " + this.missingConcepts);
            }
            return missingConceptsAction;
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid missing concepts action parameter: " + this.missingConcepts, e);
        }
    } // end of method validateAndGetMissingConceptsAction

    public void setSourceFile(FileBean sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setActionBefore(String actionBefore) {
        this.actionBefore = actionBefore;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setMissingConcepts(String missingConcepts) {
        this.missingConcepts = missingConcepts;
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
} // end of class VocabularyFolderApiActionBean
