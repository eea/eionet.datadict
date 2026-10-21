package eionet.datadict.infrastructure.asynctasks;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.List;
import java.util.Map;

public interface AsyncTaskManager {

    <T extends AsyncTask> String executeAsync(Class<T> taskType, Map<String, Object> parameters)
            throws AsyncTaskManagementException;

    AsyncTaskExecutionStatus getExecutionStatus(String taskId)
            throws AsyncTaskManagementException, ResourceNotFoundException;

    AsyncTaskExecutionEntry getTaskEntry(String taskId)
            throws AsyncTaskManagementException, ResourceNotFoundException;

    /**
     * Returns all Task Entries in the Task Entry History table
     *
     */
    List<AsyncTaskExecutionEntryHistory> getTaskEntriesHistory();

    /**
     * Returns the History for a task Entry
     *
     */
    List<AsyncTaskExecutionEntryHistory> getTaskEntryHistoryByTaskId(String taskId);

    List<AsyncTaskExecutionEntryHistory> retrieveLimitedTaskHistoryByTaskId(String taskId, int limit);

    AsyncTaskExecutionEntryHistory getTaskEntryHistory(String id);

    void deleteTask(String taskId);

}
