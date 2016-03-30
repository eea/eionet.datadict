package eionet.web.action;

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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public static final String VALID_CONTENT_TYPE_FOR_RDF_UPLOAD = "application/rdf+xml";

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
    http://localhost:8080/datadict/restapi/vocabulary/createFolderAndVocabulary?folderIdentifier=folderIdentifier
    http://localhost:8080/datadict/restapi/vocabulary/createVocabulary?folderIdentifier=NewFolderIdentifier&folderLabel=NewFolderLabel&vocabularyFolderIdentifier=newVocFoldIdent&vocabularyFolderLabel=newVOcFoldLab
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
    public Resolution createFolderAndVocabulary() throws ServiceException {

        vocabularyService.createVocabularyFolder(vocabularyFolder, folder, getUserName());

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

        Reader rdfFileReader = new InputStreamReader(request.getInputStream(), CharEncoding.UTF_8);

        // So the trick here is to pass into the method below, 2 things:
        // a vocabularyFolder object which will have some values to it through Stripes Bean mechanism by populating some
        // of its fields from request parameters in the POST call
        // also it includes the rdfPayload which will be parsed for additional data. 
        final List<String> systemMessages = this.vocabularyRdfImportService.importRdfIntoVocabulary(rdfFileReader, vocabularyFolder);

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
        this.vocabularyFolder.setFolderLabel(vocabularyFolderLabel);
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
