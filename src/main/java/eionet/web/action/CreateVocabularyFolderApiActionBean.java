package eionet.web.action;

import eionet.meta.dao.domain.DDApiKey;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.service.IApiKeyService;
import eionet.meta.service.IJWTService;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import static eionet.web.action.AbstractActionBean.LOGGER;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
//@UrlBinding("/restapi/vocabulary/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{$event}")
//@UrlBinding("/restapi/vocabulary/{$event}")
@UrlBinding("/restapi/vocabulary/{$event}")
public class CreateVocabularyFolderApiActionBean extends AbstractActionBean {

    public static final String FOLDER_LABEL = "folderLabel";

    public static final String FOLDER_IDENTIFIER = "folderIdentifier";

    public static final String VOCABULARY_FOLDER_LABEL = "vocabularyFolderLabel";

    public static final String VOCABULARY_FOLDER_IDENTIFIER = "vocabularyFolderIdentifier";

    public static final String VOCABULARY_FOLDER_BASE_URI = "baseUri";

    public static final String VOCABULARY_FOLDER_TYPE = "type";

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
    public static final String VALID_CONTENT_TYPE_FOR_RDF_PAYLOAD = "application/rdf+xml";

    
    
    public static final String JWT_ISSUER ="iss";
    
    
    /**
     * API Key identifier in json.
     */
    public static final String API_KEY_IDENTIFIER_IN_JSON = "API_KEY";

    /**
     * Created time identifier in json.
     */
    public static final String TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON = "iat";

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
     * JWT Timeout in minutes for verification (used to validate if sent token
     * is still active or deprecated).
     */
    private static final int JWT_TIMEOUT_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES);

    /**
     * JWT Algorithm for signing.
     */
    private static final String JWT_SIGNING_ALGORITHM = Props.getProperty(PropsIF.DD_VOCABULARY_ADI_JWT_ALGORITHM);

    /**
     * Folder choice value [existing].
     */
    private static final String FOLDER_CHOICE_EXISTING = "existing";

    /**
     * Folder choice value [new].
     */
    private static final String FOLDER_CHOICE_NEW = "new";

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
     * JWT service.
     */
    @SpringBean
    private IJWTService jwtService;

    /**
     * API-Key service.
     */
    @SpringBean
    private IApiKeyService apiKeyService;

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
     * Vocabulary folder id, from which the copy is made of.
     */
    private int copyId;

    private String folderLabel;

    private String folderIdentifier;

    private String vocabularyFolderLabel;

    private String vocabularyFolderIdentifier;

    private String baseUri;

    private String type;

    /**
     * New folder to be created.
     */
    private Folder folder;

    /**
     * Checkbox value for folder, when creating vocabulary folder.
     */
    private String folderChoice;

    /**
     * Id of an existing folder in the DD database
     */
    private int folderId;

    private int vocabularyId;

    private boolean numericConceptidentifiers;

    private boolean notationsEqualIdentifiers;

    private boolean keepRelationsOnDelete;

    
    
    
    
    
    
    /** 
    Test URLS for Rest Calls:
    * http://localhost:8080/datadict/restapi/vocabulary/createFolderAndVocabulary?folderIdentifier=folderIDent1&folderLabel=foldLab1&vocabularyFolderIdentifier=vocabIdentifier8&vocabularyFolderLabel=vocabLabel8
     Long story short, we pass parameters the usual HTTP GET Way
     Then stripes will open the Bean, examine it through reflection at runtime, and bind
     the passed parameters to the bean's properties if they exist and if of course there are 
    
     *Create A new Folder and a Vocabulary inside the Folder
     * @param folderIdentifier
     * @param folderLabel
     * @param vocabularyFolderIdentifier
     * @param vocabularyFolderLabel
     * @param type
     * Keep in mind that we also need the label of the vocabulary which is not included in the current context,
     * so we have just disabled the NotNull constraint from our Local MySQL DataDict Database Vocabulary Table 
     */
    public Resolution createFolderAndVocabulary() throws ServiceException, UnsupportedEncodingException, IOException {

        
          HttpServletRequest request = getContext().getRequest();

      LOGGER.info("Create Folder and Vocabulary API call invocation from remote address: " + request.getRemoteAddr() + ", and remote host: " + request.getRemoteHost());
      
      
        // Ensure that the request has the right header:
          String contentType = request.getHeader(CONTENT_TYPE_HEADER);

            if (!StringUtils.startsWithIgnoreCase(contentType, VALID_CONTENT_TYPE_FOR_RDF_PAYLOAD)) {
                LOGGER.error("uploadRdf API - invalid content type: " + contentType);
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Invalid content-type for RDF upload", ErrorActionBean.RETURN_ERROR_EVENT);
            }
          
        
            //Security Handling with JWT:
            String keyHeader = request.getHeader(JWT_API_KEY_HEADER);
            String tokenUser;

             if (StringUtils.isNotBlank(keyHeader)) {
                 
                 String jsonWebToken = keyHeader;

                try {
                    JSONObject jsonObject = jwtService.verify(JWT_KEY, JWT_AUDIENCE, jsonWebToken);

                    long createdTimeInSeconds = jsonObject.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);

                    // Check if Token is expired because it was created before a specific time limit 
                    long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;
                    if (nowInSeconds > (createdTimeInSeconds + (JWT_TIMEOUT_IN_MINUTES * 60))) {
                        LOGGER.error("Create Vocabulary API- Deprecated token");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Deprecated token", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    String apiKey = jsonObject.getString(API_KEY_IDENTIFIER_IN_JSON);

                    DDApiKey ddApiKey = apiKeyService.getApiKey(apiKey);

                    if (ddApiKey == null) {
                        LOGGER.error("Create Vocabulary API- Invalid key");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid key", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    //Note: Scope can also be used

                    if (ddApiKey.getExpires() != null) {
                        Date now = Calendar.getInstance().getTime();
                        if (now.after(ddApiKey.getExpires())) {
                            LOGGER.error("Create Vocabulary API - Expired key");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Expired key", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }

                    String remoteAddr = ddApiKey.getRemoteAddr();
                    if (StringUtils.isNotBlank(remoteAddr)) {
                        if (!StringUtils.equals(remoteAddr, request.getRemoteAddr()) && !StringUtils.equals(remoteAddr, request.getRemoteHost())) {
                            LOGGER.error("Create Vocabulary API - Invalid remote end point");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid remote end point", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }
                    tokenUser = jsonObject.getString(JWT_ISSUER);
                    if (StringUtils.isEmpty(tokenUser)) {
                        LOGGER.error("Create Vocabulary API - jwt issuer missing");
                      return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: empty jwt issuer", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                } catch (Exception e) {
                    LOGGER.error("Create Vocabulary API- Cannot verify key", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
                }
                 
                 
             }else {
                LOGGER.error("Create Vocabulary API - Key missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "API Key cannot be missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
         // If the JWT Verification is right, we can now validate the passed parameters and then start the Vocabulary Creation
         
        LOGGER.info("create Folder and  Vocabulary API - Request authorized");
        
        // Validate Parameters Passed
        
        if ( StringUtils.isEmpty(folderIdentifier)) {
                LOGGER.error("create Folder and  Vocabulary API- folderIdentifier Parameter missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary API- folderIdentifier Parameter missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
        
        if (StringUtils.isEmpty(folderLabel) ) {
                LOGGER.error("Create Folder and Vocabulary Api - folderLabel parameter missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary Api - folderLabel parameter missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
        if (StringUtils.isEmpty(vocabularyFolderIdentifier)) {
                LOGGER.error("Create Folder and Vocabulary API - vocabularyFolderIdentifier missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary API - vocabularyFolderIdentifier missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
        if ( StringUtils.isEmpty(vocabularyFolderLabel)) {
                LOGGER.error("Create Folder and Vocabulary API - vocabularyFolderLabel missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary API - vocabularyFolderLabel missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
  
        
        
        
       // vocabularyService.createVocabularyFolder(vocabularyFolder, folder, getUserName());

        Reader rdfFileReader = new InputStreamReader(request.getInputStream(), CharEncoding.UTF_8);
        
        // So the trick here is to pass into the method below, 2 things:
        // a vocabularyFolder object which will have some values to it through Stripes Bean mechanism by populating some
        // of its fields from request parameters in the POST call
        // also it includes the rdfPayload which will be parsed for additional data. 
        
        final List<String> systemMessages = this.vocabularyRdfImportService.createFolderAndVocabularyFromRDF(rdfFileReader, folder, vocabularyFolder,tokenUser);

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
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        StreamingResolution streamingResolution = new StreamingResolution(CONTENT_TYPE_HEADER, "Folder and Vocabulary Created Successfully");

        return streamingResolution;
    }
    
    
    
    /**
    *Create a Vocabulary inside an Existing Folder
    *Example URL:  http://localhost:8080/datadict/restapi/vocabulary/createVocabulary?folderId=39&vocabularyFolderIdentifier=newVocFoldIdent&vocabularyFolderLabel=newVOcFoldLab
    *
    *@param folderId
    * @param vocabularyFolderIdentifier
    * @param vocabularyFolderLabel
    * The method also needs an RDF payload in the following format:
    <?xml version="1.0" encoding="UTF-8"?>
    <rdf:RDF xmlns:adms="http://www.w3.org/ns/adms#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:dctype="http://purl.org/dc/dcmitype/" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dd="http://dd.eionet.europa.eu/schema.rdf#" xmlns="http://localhost:8080/datadict/property/" xml:base="http://localhost:8080/datadict/vocabulary/cdda/">
       <rdf:Description rdf:about="http://dd.uat.eionet.europa.eu/vocabulary/">
        <dd:type>Common</dd:type>
        <dd:numericConceptidentifiers>1</dd:numericConceptidentifiers>
        <dd:notationsEqualIdentifiers>1</dd:notationsEqualIdentifiers>
       </rdf:Description>
   </rdf:RDF>
    */
    public Resolution createVocabulary() throws UnsupportedEncodingException, IOException, ServiceException, RDFParseException, RDFHandlerException {

        //Read RDF from request body and params from url
        HttpServletRequest request = getContext().getRequest();

         // Ensure that the request has the right header:
          String contentType = request.getHeader(CONTENT_TYPE_HEADER);

            if (!StringUtils.startsWithIgnoreCase(contentType, VALID_CONTENT_TYPE_FOR_RDF_PAYLOAD)) {
                LOGGER.error("create Vocabulary  API - invalid content type: " + contentType);
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Invalid content-type for RDF upload", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
            //Security Handling with JWT:
            String keyHeader = request.getHeader(JWT_API_KEY_HEADER);
            String tokenUser;

             if (StringUtils.isNotBlank(keyHeader)) {
                 
                 String jsonWebToken = keyHeader;

                try {
                    JSONObject jsonObject = jwtService.verify(JWT_KEY, JWT_AUDIENCE, jsonWebToken);

                    long createdTimeInSeconds = jsonObject.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);

                    // Check if Token is expired because it was created before a specific time limit 
                    long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;
                    if (nowInSeconds > (createdTimeInSeconds + (JWT_TIMEOUT_IN_MINUTES * 60))) {
                        LOGGER.error("Create Vocabulary API- Deprecated token");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Deprecated token", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    String apiKey = jsonObject.getString(API_KEY_IDENTIFIER_IN_JSON);

                    DDApiKey ddApiKey = apiKeyService.getApiKey(apiKey);

                    if (ddApiKey == null) {
                        LOGGER.error("Create Vocabulary API- Invalid key");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid key", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    //Note: Scope can also be used

                    if (ddApiKey.getExpires() != null) {
                        Date now = Calendar.getInstance().getTime();
                        if (now.after(ddApiKey.getExpires())) {
                            LOGGER.error("Create Vocabulary API - Expired key");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Expired key", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }

                    String remoteAddr = ddApiKey.getRemoteAddr();
                    if (StringUtils.isNotBlank(remoteAddr)) {
                        if (!StringUtils.equals(remoteAddr, request.getRemoteAddr()) && !StringUtils.equals(remoteAddr, request.getRemoteHost())) {
                            LOGGER.error("Create Vocabulary API - Invalid remote end point");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid remote end point", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }
                    tokenUser = jsonObject.getString(JWT_ISSUER);
                    if (StringUtils.isEmpty(tokenUser)) {
                        LOGGER.error("Create Vocabulary API - jwt issuer missing");
                      return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: empty jwt issuer", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                } catch (Exception e) {
                    LOGGER.error("Create Vocabulary API- Cannot verify key", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
                }
                 
                 
             }else {
                LOGGER.error("Create Vocabulary API - Key missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "API Key cannot be missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
         // If the JWT Verification is right, we can now validate the passed parameters and then start the Vocabulary Creation
         
        LOGGER.info("create Vocabulary API - Request authorized");
        
        
             // Validate Parameters Passed
        
        
        if (folderId==0 ) {
                LOGGER.error("Create Vocabulary Api - folderId parameter missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary Api - folderId parameter missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
        if (StringUtils.isEmpty(vocabularyFolderIdentifier)) {
                LOGGER.error("Create Vocabulary API - vocabularyFolderIdentifier missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary API - vocabularyFolderIdentifier missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
        if ( StringUtils.isEmpty(vocabularyFolderLabel)) {
                LOGGER.error("Create Vocabulary API - vocabularyFolderLabel missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary API - vocabularyFolderLabel missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
        //FInally we need to check if there is one Folder with the passed folderId
        
        try{
        Folder folder = vocabularyService.getFolder(folderId);
        }catch (Exception e) {
                    LOGGER.error("Create Vocabulary API- Folder Cannot Be Found ", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_FOUND_404, " Folder with ID " +folderId +" Not Found ", ErrorActionBean.RETURN_ERROR_EVENT);
                }
        
        Reader rdfFileReader = new InputStreamReader(request.getInputStream(), CharEncoding.UTF_8);
        
        // So the trick here is to pass into the method below, 2 things:
        // a vocabularyFolder object which will have some values to it through Stripes Bean mechanism by populating some
        // of its fields from request parameters in the POST call
        // also it includes the rdfPayload which will be parsed for additional data. 
        
        final List<String> systemMessages = this.vocabularyRdfImportService.createVocabularyFromRDF(rdfFileReader, vocabularyFolder,tokenUser);

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

        return result;

    }
    
 
    
    /**
       *vocabularyId is the required parameter in order to know which vocabulary to delete.
       * keepRelationsOnDelete refers to the baseUris and whether they should replace the relations
       * or we should delete the relations completely
       * @param vocabularyId
       */
      public Resolution deleteVocabulary() throws ServiceException{
      
          //Read RDF from request body and params from url
        HttpServletRequest request = getContext().getRequest();

         // Ensure that the request has the right header:
          String contentType = request.getHeader(CONTENT_TYPE_HEADER);

            if (!StringUtils.startsWithIgnoreCase(contentType, VALID_CONTENT_TYPE_FOR_RDF_PAYLOAD)) {
                LOGGER.error("create Vocabulary  API - invalid content type: " + contentType);
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Invalid content-type for RDF upload", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
            //Security Handling with JWT:
            String keyHeader = request.getHeader(JWT_API_KEY_HEADER);
            String tokenUser;

             if (StringUtils.isNotBlank(keyHeader)) {
                 
                 String jsonWebToken = keyHeader;

                try {
                    JSONObject jsonObject = jwtService.verify(JWT_KEY, JWT_AUDIENCE, jsonWebToken);

                    long createdTimeInSeconds = jsonObject.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);

                    // Check if Token is expired because it was created before a specific time limit 
                    long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;
                    if (nowInSeconds > (createdTimeInSeconds + (JWT_TIMEOUT_IN_MINUTES * 60))) {
                        LOGGER.error("Mark Vocabulary To Be Deleted API- Deprecated token");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Deprecated token", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    String apiKey = jsonObject.getString(API_KEY_IDENTIFIER_IN_JSON);

                    DDApiKey ddApiKey = apiKeyService.getApiKey(apiKey);

                    if (ddApiKey == null) {
                        LOGGER.error("Mark Vocabulary To Be Deleted API- Invalid key");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid key", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    //Note: Scope can also be used

                    if (ddApiKey.getExpires() != null) {
                        Date now = Calendar.getInstance().getTime();
                        if (now.after(ddApiKey.getExpires())) {
                            LOGGER.error("Mark Vocabulary To Be Deleted API - Expired key");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Expired key", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }

                    String remoteAddr = ddApiKey.getRemoteAddr();
                    if (StringUtils.isNotBlank(remoteAddr)) {
                        if (!StringUtils.equals(remoteAddr, request.getRemoteAddr()) && !StringUtils.equals(remoteAddr, request.getRemoteHost())) {
                            LOGGER.error("Mark Vocabulary To Be Deleted API - Invalid remote end point");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid remote end point", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }
                    tokenUser = jsonObject.getString(JWT_ISSUER);
                    if (StringUtils.isEmpty(tokenUser)) {
                        LOGGER.error("Mark Vocabulary To Be Deleted API - jwt issuer missing");
                      return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: empty jwt issuer", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                } catch (Exception e) {
                    LOGGER.error("Mark Vocabulary To Be Deleted API- Cannot verify key", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
                }
                 
                 
             }else {
                LOGGER.error("Mark Vocabulary To Be Deleted API - Key missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "API Key cannot be missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
         // If the JWT Verification is right, we can now validate the passed parameters and then start the Vocabulary Creation
         
        LOGGER.info("Mark Vocabulary TO Be Deleted API - Request authorized");
          
        // Parameter check 
          
        if (vocabularyId==0 ) {
                LOGGER.error("Mark Vocabulary TO Be Deleted  API - vocabularyId parameter missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Vocabulary Api - vocabulary parameter missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }
        
         try{
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyId);
        }catch (Exception e) {
                    LOGGER.error("Create Vocabulary API- Vocabulary Cannot Be Found ", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_FOUND_404, " Vocabulary with ID " + vocabularyId +" Not Found ", ErrorActionBean.RETURN_ERROR_EVENT);
                }
        
         List<Integer> folderIds = new ArrayList<Integer>();
          folderIds.add(vocabularyId);
          
          //vocabularyService.deleteVocabularyFolders(folderIds, keepRelationsOnDelete);
          vocabularyService.markVocabularyFolderToBeDeleted(vocabularyId);

           StreamingResolution stResol = new StreamingResolution(CONTENT_TYPE_HEADER, "Vocabulary Marked to be deleted successfully !");
      
           return stResol;
      }
    
    
    
    
    
    /**
     * Through the rest calls to this Bean, any request params in the HTTP Get
     * are bound to the beans properties by stripes But we also need to have 2
     * instances : a VocabularyFolder and a folder. So we have implemented the
     * constructor for the Bean, to instantiate the above classes so that then
     * they can be used in the getters and setters of the passed
     * properties-fields.
     */
    public CreateVocabularyFolderApiActionBean() {

        super();
        VocabularyFolder vocabularyFolder = new VocabularyFolder();
        // vocabularyFolder.setType(VocabularyType.COMMON);
        setVocabularyFolder(vocabularyFolder);
        setFolder((new Folder()));
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

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getFolderLabel() {
        return folderLabel;
    }

    /**
     * Set the URL RequestParam folderLabel to the folder object instantiated in
     * the Bean's Constructor
     */
    public void setFolderLabel(String folderLabel) {
        this.folder.setLabel(folderLabel);
        this.folderLabel = folderLabel;
    }

    public String getFolderIdentifier() {
        return folderIdentifier;
    }

    /**
     * Set the URL RequestParam folderIdentifier to the folder object
     * instantiated in the Bean's Constructor
     */
    public void setFolderIdentifier(String folderIdentifier) {
        this.folder.setIdentifier(folderIdentifier);
        this.folderIdentifier = folderIdentifier;
    }

    public String getVocabularyFolderLabel() {
        return vocabularyFolderLabel;
    }

    /*
     *Set the URL RequestParam vocabularyFolderLabel to the vocabularyFolder object instantiated in the Bean's Constructor
     */
    public void setVocabularyFolderLabel(String vocabularyFolderLabel) {
        this.vocabularyFolder.setLabel(vocabularyFolderLabel);
        this.vocabularyFolderLabel = vocabularyFolderLabel;
    }

    public String getVocabularyFolderIdentifier() {
        return vocabularyFolderIdentifier;
    }

    /*
     *Set the URL RequestParam vocabularyFolderIdentifier to the vocabularyFolder object instantiated in the Bean's Constructor
     */
    public void setVocabularyFolderIdentifier(String vocabularyFolderIdentifier) {
        this.vocabularyFolder.setIdentifier(vocabularyFolderIdentifier);
        this.vocabularyFolderIdentifier = vocabularyFolderIdentifier;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getType() {
        return type;
    }

    /*
     *Set the URL RequestParam type to the vocabularyFolder object instantiated in the Bean's Constructor
     */
    public void setType(String type) {

        if (StringUtils.equals(VocabularyType.COMMON.toString(), type.toUpperCase())) {
            this.vocabularyFolder.setType(VocabularyType.COMMON);
        }
        if (StringUtils.equals(VocabularyType.SITE_CODE.toString(), type.toUpperCase())) {
            this.vocabularyFolder.setType(VocabularyType.SITE_CODE);
        }

        this.type = type;
    }

    public int getFolderId() {
        return folderId;
    }

    /*
     *Set the URL RequestParam folderId to the vocabularyFolder object instantiated in the Bean's Constructor
     */
    public void setFolderId(int folderId) {
        this.vocabularyFolder.setFolderId(folderId);
        this.folderId = folderId;
    }

    public boolean isNumericConceptidentifiers() {
        return numericConceptidentifiers;
    }

    /*
     *Set the URL RequestParam numericConceptidentifiers to the vocabularyFolder object instantiated in the Bean's Constructor
     */
    public void setNumericConceptidentifiers(boolean numericConceptidentifiers) {
        this.vocabularyFolder.setNumericConceptIdentifiers(numericConceptidentifiers);
        this.numericConceptidentifiers = numericConceptidentifiers;
    }

    public boolean isNotationsEqualIdentifiers() {
        return notationsEqualIdentifiers;
    }

    /*
     *Set the URL RequestParam notationsEqualIdentifiers to the vocabularyFolder object instantiated in the Bean's Constructor
     */
    public void setNotationsEqualIdentifiers(boolean notationsEqualIdentifiers) {

        this.vocabularyFolder.setNotationsEqualIdentifiers(numericConceptidentifiers);
        this.notationsEqualIdentifiers = notationsEqualIdentifiers;
    }

    public boolean isKeepRelationsOnDelete() {
        return keepRelationsOnDelete;
    }

    public void setKeepRelationsOnDelete(boolean keepRelationsOnDelete) {
        this.keepRelationsOnDelete = keepRelationsOnDelete;
    }

    public int getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(int vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

}
