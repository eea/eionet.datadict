package eionet.datadict.infrastructure.asynctasks;

import java.util.Map;
import java.util.concurrent.Callable;

public interface AsyncTask extends Callable<Object> {
    
    Class getResultType();
    
    void setParameters(Map<String, Object> parameters);
    
}
