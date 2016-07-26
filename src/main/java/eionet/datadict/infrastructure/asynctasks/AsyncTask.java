package eionet.datadict.infrastructure.asynctasks;

import java.util.Map;
import java.util.concurrent.Callable;

public interface AsyncTask extends Callable<Object> {
    
    String getDisplayName();
    
    Class getResultType();
    
    void setParameters(Map<String, Object> parameters);
    
    String getResultUrl(String taskId, Map<String, Object> parameters, Object result);
    
}
