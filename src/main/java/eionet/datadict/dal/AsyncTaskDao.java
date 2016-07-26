package eionet.datadict.dal;

import eionet.datadict.model.AsyncTaskExecutionEntry;

public interface AsyncTaskDao {
    
    AsyncTaskExecutionEntry getStatusEntry(String taskId);
    
    AsyncTaskExecutionEntry getResultEntry(String taskId);
    
    void create(AsyncTaskExecutionEntry entry);
    
    void updateStartStatus(AsyncTaskExecutionEntry entry);
    
    void updateEndStatus(AsyncTaskExecutionEntry entry);
    
}
