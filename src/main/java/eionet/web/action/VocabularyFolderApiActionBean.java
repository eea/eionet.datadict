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

import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.services.auth.WebApiAuthInfoService;
import eionet.datadict.services.auth.WebApiAuthService;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromApiTask;
import eionet.meta.DDUser;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.service.IJWTService;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyImportService.MissingConceptsAction;
import eionet.meta.service.IVocabularyImportService.UploadAction;
import eionet.meta.service.IVocabularyImportService.UploadActionBefore;
import eionet.meta.service.IVocabularyService;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.io.ByteArrayOutputStream;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * Vocabulary folder API action bean.
 *
 * @author enver
 */
@UrlBinding("/api/vocabulary/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{$event}")
public class VocabularyFolderApiActionBean extends AbstractActionBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyFolderApiActionBean.class);

    //Constants
    /**
     * Request parameter name for action before.
     */
    public static final String ACTION_BEFORE_REQ_PARAM = "actionBefore";

    /**
     * Request parameter name for missing concepts.
     */
    public static final String MISSING_CONCEPTS_REQ_PARAM = "missingConcepts";

    /**
     * Request parameter name for action.
     */
    public static final String ACTION_REQ_PARAM = "action";

    /**
     * API key header for request.
     */
    public static final String JWT_API_KEY_HEADER = "X-DD-API-KEY";

    /**
     * Keyword for content type.
     */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * Valid content type for RDF upload.
     */
    public static final String VALID_CONTENT_TYPE_FOR_RDF_UPLOAD = "application/rdf+xml";

    /**
     * Created time identifier in json.
     */
    public static final String TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON = "iat";

    /**
     * DOMAIN identifier in json.
     */
    public static final String DOMAIN = "domain";

    /**
     * JWT Key.
     */
    private static final String JWT_KEY = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * JWT Audience.
     */
    private static final String JWT_AUDIENCE = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);

    /**
     * JWT Expiration in minutes for signing.
     */
    private static final int JWT_EXPIRATION_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_EXP_IN_MINUTES);

    /**
     * JWT Timeout in minutes for verification (used to validate if sent token is still active or deprecated).
     */
    private static final int JWT_TIMEOUT_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES);

    /**
     * JWT Algorithm for signing.
     */
    private static final String JWT_SIGNING_ALGORITHM = Props.getProperty(PropsIF.DD_VOCABULARY_ADI_JWT_ALGORITHM);

    //Static variables
    /**
     * Reserved API names, that cannot be vocabulary concept identifiers.
     */
    public static final List<String> RESERVED_VOCABULARY_API_EVENTS;


    /**
     * Static block for initializations.
     */
    static {
        //Create supported/reserved api names
        RESERVED_VOCABULARY_API_EVENTS = new ArrayList<String>();
        RESERVED_VOCABULARY_API_EVENTS.add("uploadRdf");
    } // end of static block

    /**
     * Json output format.
     */
    public static final String JSON_FORMAT = "application/json";

    //Instance members
    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;

    /**
     * Vocabulary folder DAO.
     */
    @SpringBean
    private IVocabularyFolderDAO vocabularyFolderDAO;

    /**
     * JWT service.
     */
    @SpringBean
    private IJWTService jwtService;
    
    @SpringBean
    private AsyncTaskManager asyncTaskManager;

    /**
     * Vocabulary folder.
     */
    private VocabularyFolder vocabularyFolder;

    /**
     * RDF Import Service.
     */
    @SpringBean
    private IRDFVocabularyImportService vocabularyRdfImportService;

    @SpringBean
    private WebApiAuthInfoService webApiAuthInfoService;
    @SpringBean
    private WebApiAuthService webApiAuthService;
    @SpringBean
    private VocabularyDataService vocabularyDataService;
    
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

    // start params for create
    private String label;
    private String baseUri;
    private boolean numericConceptidentifiers;
    private boolean notationsEqualIdentifiers;
    // end params for create
    
    //Method definitions

    @DefaultHandler
    public Resolution createVocabulary() {
        DDUser user;
        Thread.currentThread().setName("ADD-VOCABULARY");
        ActionMethodUtils.setLogParameters(getContext());

        try {
            user = this.webApiAuthService.authenticate(this.webApiAuthInfoService.getAuthenticationInfo(getContext().getRequest()));
        } catch (UserAuthenticationException ex) {
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        }
        
        VocabularyFolder vocabulary = new VocabularyFolder();
        vocabulary.setIdentifier(this.vocabularyFolder.getIdentifier());
        vocabulary.setLabel(this.label);
        vocabulary.setNumericConceptIdentifiers(this.numericConceptidentifiers);
        vocabulary.setNotationsEqualIdentifiers(this.notationsEqualIdentifiers);
        vocabulary.setBaseUri(this.baseUri);
        final String vocabularySetIdentifier = this.vocabularyFolder.getFolderName();
        final VocabularyFolder created;
        
        try {
            created = this.vocabularyDataService.createVocabulary(vocabularySetIdentifier, vocabulary, user);
        } catch (EmptyParameterException ex) {
            LOGGER.info(ex.getMessage(), ex);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        } catch (ResourceNotFoundException ex) {
            LOGGER.info(ex.getMessage(), ex);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_FOUND_404, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        } catch (DuplicateResourceException ex) {
            LOGGER.info(ex.getMessage(), ex);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.CONFLICT, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        }
        
        StreamingResolution result = new StreamingResolution(JSON_FORMAT) {

            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                List<String> messages = new ArrayList<String>();
                messages.add(String.format("Successfully created vocabulary %s/%s", vocabularySetIdentifier, created.getIdentifier()));
                VocabularyJSONOutputHelper.writeJSON(response.getOutputStream(), messages);
            }
            
        };
        
        return result;
    }
    
    /**
     * Imports RDF contents into vocabulary.
     *
     * @return resolution
     * @throws eionet.meta.service.ServiceException when an error occurs
     */
    public Resolution uploadRdf() throws Exception {
        Thread.currentThread().setName("RDF-IMPORT");
            // read RDF from request body and params from url
            HttpServletRequest request = getContext().getRequest();
            ActionMethodUtils.setLogParameters(getContext());

            LOGGER.info("uploadRdf API - called with remote address: " + request.getRemoteAddr() + ", and remote host: " + request.getRemoteHost());

            Map<String, Object> result = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();

            String contentType = request.getHeader(CONTENT_TYPE_HEADER);
            if (!StringUtils.startsWithIgnoreCase(contentType, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD)) {
                LOGGER.error("uploadRdf API - invalid content type: " + contentType);
                
                result.put("error", "Invalid content-type for RDF upload: " + contentType);
                return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                    @Override
                    protected void stream(HttpServletResponse response) throws Exception {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        super.stream(response);
                    }
                };
            }

            String keyHeader = request.getHeader(JWT_API_KEY_HEADER);
            if (StringUtils.isNotBlank(keyHeader)) {
                String jsonWebToken = keyHeader;

                try {
                    JSONObject jsonObject = jwtService.verify(JWT_KEY, JWT_AUDIENCE, jsonWebToken);

                    long createdTimeInSeconds = jsonObject.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);
                    long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;

                    if (nowInSeconds > (createdTimeInSeconds + (JWT_TIMEOUT_IN_MINUTES * 60))) {
                        LOGGER.error("uploadRdf API - Deprecated token");
                        
                        result.put("error", "Cannot authorize: Deprecated token.");
                        return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                            @Override
                            protected void stream(HttpServletResponse response) throws Exception {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                super.stream(response);
                            }
                        };
                    }

                    // check if the domain that the token was generated in, is the same as this one
                    String domain = null;
                    try {
                        domain = jsonObject.getString(DOMAIN);
                    } catch(Exception e) {
                        LOGGER.error("uploadRdf API - The token does not include domain information");
                        
                        result.put("error", "Cannot authorize: The token does not include domain information.");
                        result.put("errorDetails", e.getMessage());
                        return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                            @Override
                            protected void stream(HttpServletResponse response) throws Exception {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                super.stream(response);
                            }
                        };
                    }

                    if (!Props.getProperty(PropsIF.DD_URL).equals(domain)) {
                        LOGGER.error("uploadRdf API - The token was not generated from this domain");
                        
                        result.put("error", "Cannot authorize: The token was not generated from this domain.");
                        return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                            @Override
                            protected void stream(HttpServletResponse response) throws Exception {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                super.stream(response);
                            }
                        };
                    }
                } catch (Exception e) {
                    LOGGER.error("uploadRdf API - Cannot verify key", e);
                    
                    result.put("error", "Cannot authorize: Cannot verify key.");
                    result.put("errorDetails", e.getMessage());
                    return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                        @Override
                        protected void stream(HttpServletResponse response) throws Exception {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            super.stream(response);
                        }
                    };
                }
            } else {
                LOGGER.error("uploadRdf API - Key missing");
                
                result.put("error", "Cannot authorize: API Key missing.");
                return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                    @Override
                    protected void stream(HttpServletResponse response) throws Exception {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        super.stream(response);
                    }
                };
            }

            LOGGER.info("uploadRdf API - request authorized");

            // these lines are redundant but for any case, are kept in code. Stripes handle well, request parameters.
            if (this.actionBefore == null && request.getParameter(ACTION_BEFORE_REQ_PARAM) != null) {
                setActionBefore(request.getParameter(ACTION_BEFORE_REQ_PARAM));
            }

            if (this.missingConcepts == null && request.getParameter(MISSING_CONCEPTS_REQ_PARAM) != null) {
                setMissingConcepts(request.getParameter(MISSING_CONCEPTS_REQ_PARAM));
            }

            if (this.action == null && request.getParameter(ACTION_REQ_PARAM) != null) {
                setAction(request.getParameter(ACTION_REQ_PARAM));
            }

            // validate parameters
            UploadActionBefore uploadActionBefore = null;
            UploadAction uploadAction = null;
            MissingConceptsAction missingConceptsAction = null;
            try {
                uploadActionBefore = validateAndGetUploadActionBefore(vocabularyRdfImportService.getSupportedActionBefore(true),
                        vocabularyRdfImportService.getDefaultActionBefore(true));
                uploadAction = validateAndGetUploadAction(vocabularyRdfImportService.getSupportedAction(true),
                        vocabularyRdfImportService.getDefaultAction(true));
                missingConceptsAction = validateAndGetMissingConceptsAction(vocabularyRdfImportService.getSupportedMissingConceptsAction(true),
                        vocabularyRdfImportService.getDefaultMissingConceptsAction(true));
            } catch (IllegalArgumentException e) {
                LOGGER.error("uploadRdf API - Illegal argument: " + e.getMessage());
                
                result.put("error", "Illegal argument");
                result.put("errorDetails", e.getMessage());
                return new StreamingResolution(contentType, mapper.writeValueAsString(result)) {
                    @Override
                    protected void stream(HttpServletResponse response) throws Exception {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        super.stream(response);
                    }
                };
            }
        
        // compress and base64 encode to accomodate storage for large RDF body content in JOB_DATA column of QRTZ_JOB_DETAILS table
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = request.getInputStream().read(buffer)) != -1) {
                gzos.write(buffer, 0, length);
            }
            request.getInputStream().close();
        }
        String base64EncodedCompressedRdf = Base64.getEncoder().encodeToString(baos.toByteArray());
       
        String taskId = this.asyncTaskManager.executeAsync(VocabularyRdfImportFromApiTask.class,
                    VocabularyRdfImportFromApiTask.createParamsBundle(base64EncodedCompressedRdf, 
                            vocabularyFolder.getFolderName(), vocabularyFolder.getIdentifier(), uploadActionBefore, uploadAction, missingConceptsAction, 
                            request.getParameter(VocabularyRdfImportFromApiTask.PARAM_NOTIFIERS_EMAILS)));

        result.put("url", Props.getRequiredProperty(PropsIF.DD_URL) + "/asynctasks/" + taskId);
        return new StreamingResolution(JSON_FORMAT, mapper.writeValueAsString(result));
    }

    /**
     * Validator and enum value converter for upload action before parameter.
     *
     * @param supportedUploadActionBefore supported action before list for this upload operation.
     * @param defaultValue                a default value if this action before param missing.
     * @return Converted enum value
     * @throws IllegalArgumentException when parameter is invalid or not supported.
     */
    private UploadActionBefore validateAndGetUploadActionBefore(List<UploadActionBefore> supportedUploadActionBefore,
                                                                UploadActionBefore defaultValue) throws IllegalArgumentException {
        if (defaultValue != null && (this.actionBefore == null || this.actionBefore.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            UploadActionBefore uploadActionBefore = UploadActionBefore.valueOf(this.actionBefore);
            if (supportedUploadActionBefore != null && !supportedUploadActionBefore.contains(uploadActionBefore)) {
                throw new IllegalArgumentException("Not supported action before parameter: " + this.actionBefore);
            }
            return uploadActionBefore;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid action before parameter: " + this.actionBefore, e);
        }
    } // end of method validateUploadActionBefore

    /**
     * Validator and enum value converter for upload action parameter.
     *
     * @param supportedUploadAction supported action list for this upload operation.
     * @param defaultValue          a default value if this action param missing.
     * @return Converted enum value
     * @throws IllegalArgumentException when parameter is invalid or not supported.
     */
    private UploadAction validateAndGetUploadAction(List<UploadAction> supportedUploadAction,
                                                    UploadAction defaultValue)
            throws IllegalArgumentException {
        if (defaultValue != null && (this.action == null || this.action.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            UploadAction uploadAction = UploadAction.valueOf(this.action);
            if (supportedUploadAction != null && !supportedUploadAction.contains(uploadAction)) {
                throw new IllegalArgumentException("Not supported action parameter: " + this.action);
            }
            return uploadAction;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid action parameter: " + this.action, e);
        }
    } // end of method validateAndGetUploadAction

    /**
     * Validator and enum value converter for upload missing concepts action parameter.
     *
     * @param supportedMissingConceptsAction supported missing concepts action list for this upload operation.
     * @param defaultValue                   a default value if this missing concepts action param missing.
     * @return Converted enum value
     * @throws IllegalArgumentException when parameter is invalid or not supported.
     */
    private MissingConceptsAction validateAndGetMissingConceptsAction(List<MissingConceptsAction> supportedMissingConceptsAction,
                                                                      MissingConceptsAction defaultValue) throws IllegalArgumentException {
        if (defaultValue != null && (this.missingConcepts == null || this.missingConcepts.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            MissingConceptsAction missingConceptsAction = MissingConceptsAction.valueOf(this.missingConcepts);
            if (supportedMissingConceptsAction != null && !supportedMissingConceptsAction.contains(missingConceptsAction)) {
                throw new IllegalArgumentException("Not supported missing concepts action parameter: " + this.missingConcepts);
            }
            return missingConceptsAction;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid missing concepts action parameter: " + this.missingConcepts, e);
        }
    } // end of method validateAndGetMissingConceptsAction

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

    public IVocabularyService getVocabularyService() {
        return vocabularyService;
    }

    public void setVocabularyService(IVocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    public IJWTService getJwtService() {
        return jwtService;
    }

    public void setJwtService(IJWTService jwtService) {
        this.jwtService = jwtService;
    }

    public IRDFVocabularyImportService getVocabularyRdfImportService() {
        return vocabularyRdfImportService;
    }

    public void setVocabularyRdfImportService(IRDFVocabularyImportService vocabularyRdfImportService) {
        this.vocabularyRdfImportService = vocabularyRdfImportService;
    }

    public WebApiAuthInfoService getWebApiAuthInfoService() {
        return webApiAuthInfoService;
    }

    public void setWebApiAuthInfoService(WebApiAuthInfoService webApiAuthInfoService) {
        this.webApiAuthInfoService = webApiAuthInfoService;
    }

    public WebApiAuthService getWebApiAuthService() {
        return webApiAuthService;
    }

    public void setWebApiAuthService(WebApiAuthService webApiAuthService) {
        this.webApiAuthService = webApiAuthService;
    }

    public VocabularyDataService getVocabularyDataService() {
        return vocabularyDataService;
    }

    public void setVocabularyDataService(VocabularyDataService vocabularyDataService) {
        this.vocabularyDataService = vocabularyDataService;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public boolean isNumericConceptidentifiers() {
        return numericConceptidentifiers;
    }

    public void setNumericConceptidentifiers(boolean numericConceptidentifiers) {
        this.numericConceptidentifiers = numericConceptidentifiers;
    }

    public boolean isNotationsEqualIdentifiers() {
        return notationsEqualIdentifiers;
    }

    public void setNotationsEqualIdentifiers(boolean notationsEqualIdentifiers) {
        this.notationsEqualIdentifiers = notationsEqualIdentifiers;
    }
    
} // end of class VocabularyFolderApiActionBean
