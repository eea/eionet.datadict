package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AsyncJobTestTask implements AsyncTask {
    
    protected static final String PARAM_RAISE_ERROR = "raiseError";
    protected static final String PARAM_DURATION = "duration";
    protected static final int RESULT_VALUE = 5;
    protected static final String ERROR_MESSAGE = "Some error.";
    
    private Map<String, Object> parameters;
    
    public Map<String, Object> getParameters() {
        return this.parameters;
    }
    
    @Override
    public String getDisplayName() {
        return "Async Job Test Task";
    }

    @Override
    public Class getResultType() {
        return Integer.class;
    }

    @Override
    public void setUp(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String composeResultUrl(String taskId, Object result) {
        return "/";
    }

    @Override
    public Object call() throws Exception {
        if (this.parameters == null) {
            throw new IllegalStateException();
        }
        
        Boolean raiseError = (Boolean) this.parameters.get(PARAM_RAISE_ERROR);
        
        if (raiseError != null && raiseError) {
            throw new Exception(ERROR_MESSAGE);
        }
        
        Long duration = (Long) this.parameters.get(PARAM_DURATION);
        
        if (duration != null) {
            Thread.sleep(duration);
        }
        
        return RESULT_VALUE;
    }
    
}
