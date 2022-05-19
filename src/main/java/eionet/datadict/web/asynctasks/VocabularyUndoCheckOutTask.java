package eionet.datadict.web.asynctasks;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("vocabularyUndoCheckOutTask")
@Scope("prototype")
public class VocabularyUndoCheckOutTask implements AsyncTask {

    public static final String PARAM_VOCABULARY_ID = "vocabularyId";
    public static final String PARAM_USER_NAME = "userName";
    
    public static final String RESULT_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String RESULT_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
    
    public static Map<String, Object> createParamsBundle(int vocabularyId, String userName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_VOCABULARY_ID, vocabularyId);
        params.put(PARAM_USER_NAME, userName);
        
        return params;
    }
    
    private final IVocabularyService vocabularyService;
    
    private Map<String, Object> parameters;
    
    @Autowired
    public VocabularyUndoCheckOutTask(IVocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
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
        return String.format("Undoing vocabulary checkout");
    }

    @Override
    public String composeResultUrl(String taskId, Object result) {
        Map<String, Object> resultMap = (Map) result;
        return String.format("/vocabulary/%s/%s", 
                resultMap.get(RESULT_VOCABULARY_SET_IDENTIFIER), resultMap.get(RESULT_VOCABULARY_IDENTIFIER));
    }

    @Override
    public Object call() throws Exception {
        Thread.currentThread().setName("VOCABULARY-UNDO-CHECKOUT");
        int id = vocabularyService.undoCheckOut(this.getVocabularyId(), this.getUserName());
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(id);
        Map<String, Object> result = new HashMap();
        result.put(RESULT_VOCABULARY_SET_IDENTIFIER, vocabularyFolder.getFolderName());
        result.put(RESULT_VOCABULARY_IDENTIFIER, vocabularyFolder.getIdentifier());
        
        return result;
    }
    
    protected int getVocabularyId() {
        return (Integer) this.parameters.get(PARAM_VOCABULARY_ID);
    }
    
    protected String getUserName() {
        return (String) this.parameters.get(PARAM_USER_NAME);
    }
    
}
