package eionet.datadict.infrastructure.asynctasks;

import java.util.Map;
import java.util.concurrent.Callable;

public interface AsyncTask extends Callable<Object> {
    
    void setUp(Map<String, Object> parameters);
    
    Class getResultType();
    
    String getDisplayName();
    
    String composeResultUrl(String taskId, Object result);
    
}
