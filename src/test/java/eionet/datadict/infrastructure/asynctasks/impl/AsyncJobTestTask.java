package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import java.util.Map;

public class AsyncJobTestTask implements AsyncTask {
    
    protected static final String PARAM_RAISE_ERROR = "raiseError";
    protected static final int RESULT_VALUE = 5;
    protected static final String ERROR_MESSAGE = "Some error.";
    
    private Map<String, Object> parameters;
    
    @Override
    public String getDisplayName() {
        return "Async Job Test Task";
    }

    @Override
    public Class getResultType() {
        return Integer.class;
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getResultUrl(String taskId, Map<String, Object> parameters, Object result) {
        return "/";
    }

    @Override
    public Object call() throws Exception {
        if (this.parameters == null) {
            throw new IllegalStateException();
        }
        
        boolean raiseError = (Boolean) this.parameters.get(PARAM_RAISE_ERROR);
        
        if (raiseError) {
            throw new Exception(ERROR_MESSAGE);
        }
        
        return RESULT_VALUE;
    }
    
}
