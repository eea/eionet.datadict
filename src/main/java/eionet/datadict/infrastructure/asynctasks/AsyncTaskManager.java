package eionet.datadict.infrastructure.asynctasks;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Map;

public interface AsyncTaskManager {

    <T extends AsyncTask> String executeAsync(Class<T> taskType, Map<String, Object> parameters) 
            throws AsyncTaskManagementException;
    
    AsyncTaskExecutionStatus getExecutionStatus(String taskId) 
            throws AsyncTaskManagementException, ResourceNotFoundException;
    
}
