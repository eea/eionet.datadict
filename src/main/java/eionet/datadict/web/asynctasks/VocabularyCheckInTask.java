package eionet.datadict.web.asynctasks;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.meta.service.IVocabularyService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("vocabularyCheckInTask")
@Scope("prototype")
public class VocabularyCheckInTask implements AsyncTask {

    public static final String PARAM_VOCABULARY_ID = "vocabularyId";
    public static final String PARAM_USER_NAME = "userName";
    public static final String PARAM_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String PARAM_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
    
    public static Map<String, Object> createParamsBundle(int vocabularyId, String userName, String vocabularySetIdentifier, String vocabularyIdentifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_VOCABULARY_ID, vocabularyId);
        params.put(PARAM_USER_NAME, userName);
        params.put(PARAM_VOCABULARY_SET_IDENTIFIER, vocabularySetIdentifier);
        params.put(PARAM_VOCABULARY_IDENTIFIER, vocabularyIdentifier);
        
        return params;
    }
    
    private final IVocabularyService vocabularyService;
    
    private Map<String, Object> parameters;
    
    @Autowired
    public VocabularyCheckInTask(IVocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }
    
    @Override
    public void setUp(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Class getResultType() {
        return Void.TYPE;
    }

    @Override
    public String getDisplayName() {
        return String.format("Checking in vocabulary %s/%s", this.getVocabularySetIdentifier(), this.getVocabularyIdentifier());
    }

    @Override
    public String composeResultUrl(String taskId, Object result) {
        return String.format("/vocabulary/%s/%s", this.getVocabularySetIdentifier(), this.getVocabularyIdentifier());
    }

    @Override
    public Object call() throws Exception {
        vocabularyService.checkInVocabularyFolder(this.getVocabularyId(), this.getUserName());
        
        return null;
    }
    
    protected int getVocabularyId() {
        return (Integer) this.parameters.get(PARAM_VOCABULARY_ID);
    }
    
    protected String getUserName() {
        return (String) this.parameters.get(PARAM_USER_NAME);
    }
    
    protected String getVocabularySetIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_SET_IDENTIFIER);
    }
    
    protected String getVocabularyIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_IDENTIFIER);
    }
    
}
