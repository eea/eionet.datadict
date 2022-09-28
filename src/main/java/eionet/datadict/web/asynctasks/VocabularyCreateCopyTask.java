package eionet.datadict.web.asynctasks;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import static eionet.datadict.web.asynctasks.VocabularyUndoCheckOutTask.RESULT_VOCABULARY_IDENTIFIER;
import static eionet.datadict.web.asynctasks.VocabularyUndoCheckOutTask.RESULT_VOCABULARY_SET_IDENTIFIER;
import eionet.meta.dao.IFolderDAO;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.IVocabularyService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class VocabularyCreateCopyTask implements AsyncTask {

    public static final String PARAM_FROM_VOCABULARY_ID = "fromVocabularyId";
    public static final String PARAM_TO_VOCABULARY_FOLDER_ID = "toVocabularyFolderId";
    public static final String PARAM_TO_VOCABULARY_IDENTIFIER = "toVocabularyIdentifier";
    public static final String PARAM_TO_VOCABULARY_LABEL = "toVocabularyLabel";
    public static final String PARAM_TO_VOCABULARY_BASE_URI = "toVocabularyBaseUri";
    public static final String PARAM_TO_VOCABULARY_TYPE = "toVocabularyType";
    public static final String PARAM_TO_VOCABULARY_NUMERIC_CONCEPT_IDENTIFIERS = "toVocabularyNumericConceptIdentifiers";
    public static final String PARAM_TO_VOCABULARY_NOTATIONS_EQUALS_IDENTIFIERS = "toVocabularyNotationsEqualIdentifiers";
    public static final String PARAM_USER_NAME = "userName";
    public static final String PARAM_NEW_FOLDER_IDENTIFIER = "newFolderIdentifier";
    public static final String PARAM_NEW_FOLDER_LABEL = "newFolderLabel";
    
    public static final String RESULT_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String RESULT_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
       
    public static Map<String, Object> createParamsBundle(int fromVocabularyId, int toVocabularyFolderId, 
            String toVocabularyIdentifier, String toVocabularyLabel, 
            String toVocabularyBaseUri, VocabularyType toVocabularyType, 
            boolean toVocabularyNumericConceptIdentifiers, boolean toVocabularyNotationsEqualIdentifiers,
            String userName, String folderIdentifier, String folderLabel) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_FROM_VOCABULARY_ID, fromVocabularyId);
        params.put(PARAM_TO_VOCABULARY_FOLDER_ID, toVocabularyFolderId);
        params.put(PARAM_TO_VOCABULARY_IDENTIFIER, toVocabularyIdentifier);
        params.put(PARAM_TO_VOCABULARY_LABEL, toVocabularyLabel);
        params.put(PARAM_TO_VOCABULARY_BASE_URI, toVocabularyBaseUri);
        params.put(PARAM_TO_VOCABULARY_TYPE, toVocabularyType);
        params.put(PARAM_TO_VOCABULARY_NUMERIC_CONCEPT_IDENTIFIERS, toVocabularyNumericConceptIdentifiers);
        params.put(PARAM_TO_VOCABULARY_NOTATIONS_EQUALS_IDENTIFIERS, toVocabularyNotationsEqualIdentifiers);
        params.put(PARAM_USER_NAME, userName);
        params.put(PARAM_NEW_FOLDER_IDENTIFIER, folderIdentifier);
        params.put(PARAM_NEW_FOLDER_LABEL, folderLabel);
        
        return params;
    }
    
    private final IVocabularyService vocabularyService;
    private final IFolderDAO folderDAO;
    
    private Map<String, Object> parameters;
    
    @Autowired
    public VocabularyCreateCopyTask(IVocabularyService vocabularyService, IFolderDAO folderDAO) {
        this.vocabularyService = vocabularyService;
        this.folderDAO = folderDAO;
    }
    
    @Override
    public void setUp(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Class getResultType() {
        return Map.class;
    }

    @Override
    public String getDisplayName() {
        return String.format("Copying vocabulary...");
    }

    @Override
    public String composeResultUrl(String taskId, Object result) {
        Map<String, Object> resultMap = (Map) result;
        return String.format("/vocabulary/%s/%s", 
                resultMap.get(RESULT_VOCABULARY_SET_IDENTIFIER), resultMap.get(RESULT_VOCABULARY_IDENTIFIER));
    }

    @Override
    public Object call() throws Exception {
        Thread.currentThread().setName("VOCABULARY-CREATE-COPY");
        VocabularyFolder vocabularyFolder = new VocabularyFolder();
        vocabularyFolder.setFolderId(getToVocabularyFolderId());
        vocabularyFolder.setIdentifier(getToVocabularyIdentifier());
        vocabularyFolder.setLabel(getToVocabularyLabel());
        vocabularyFolder.setBaseUri(getToVocabularyBaseUri());
        vocabularyFolder.setType(getToVocabularyType());
        vocabularyFolder.setNumericConceptIdentifiers(getToVocabularyNumericConceptIdentifiers());
        vocabularyFolder.setNotationsEqualIdentifiers(getToVocabularyNotationsEqualIdentifiers());
        
        Map<String, Object> result = new HashMap();
        
        result.put(RESULT_VOCABULARY_IDENTIFIER, vocabularyFolder.getIdentifier());
        Folder newFolder = null;
        if (StringUtils.isNotBlank(getNewFolderIdentifier()) && StringUtils.isNotBlank(getNewFolderLabel())) {
            newFolder = new Folder();
            newFolder.setIdentifier(getNewFolderIdentifier());
            newFolder.setLabel(getNewFolderLabel());
            result.put(RESULT_VOCABULARY_SET_IDENTIFIER,  newFolder.getIdentifier());
        } else {
            result.put(RESULT_VOCABULARY_SET_IDENTIFIER,  folderDAO.getFolder(getToVocabularyFolderId()).getIdentifier());
        }
        
        vocabularyService.createVocabularyFolderCopy(vocabularyFolder, getFromVocabularyId(), getUserName(), newFolder);
        
        result.put(RESULT_VOCABULARY_IDENTIFIER, vocabularyFolder.getIdentifier());
        
        return result;
    }

    protected int getFromVocabularyId() {
        return (Integer) this.parameters.get(PARAM_FROM_VOCABULARY_ID);
    }
    
    protected int getToVocabularyFolderId() {
        return (Integer) this.parameters.get(PARAM_TO_VOCABULARY_FOLDER_ID);
    }

    protected String getToVocabularyIdentifier() {
        return (String) this.parameters.get(PARAM_TO_VOCABULARY_IDENTIFIER);
    }
    
    protected String getToVocabularyLabel() {
        return (String) this.parameters.get(PARAM_TO_VOCABULARY_LABEL);
    }

    protected String getToVocabularyBaseUri() {
        return (String) this.parameters.get(PARAM_TO_VOCABULARY_BASE_URI);
    }

    protected VocabularyType getToVocabularyType() {
        return (VocabularyType) this.parameters.get(PARAM_TO_VOCABULARY_TYPE);
    }

    protected boolean getToVocabularyNumericConceptIdentifiers() {
        return (Boolean) this.parameters.get(PARAM_TO_VOCABULARY_NUMERIC_CONCEPT_IDENTIFIERS);
    }

    protected boolean getToVocabularyNotationsEqualIdentifiers() {
        return (Boolean) this.parameters.get(PARAM_TO_VOCABULARY_NOTATIONS_EQUALS_IDENTIFIERS);
    }

    protected String getUserName() {
        return (String) this.parameters.get(PARAM_USER_NAME);
    }

    protected String getNewFolderIdentifier() {
        return (String) this.parameters.get(PARAM_NEW_FOLDER_IDENTIFIER);
    }

    protected String getNewFolderLabel() {
        return (String) this.parameters.get(PARAM_NEW_FOLDER_LABEL);
    }

}
