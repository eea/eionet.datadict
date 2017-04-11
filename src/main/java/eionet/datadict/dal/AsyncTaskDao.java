package eionet.datadict.dal;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import java.util.Date;
import java.util.List;

public interface AsyncTaskDao {
    
    AsyncTaskExecutionEntry getStatusEntry(String taskId);
    
    List<AsyncTaskExecutionEntry> getAllEntries();
    
    AsyncTaskExecutionEntry getFullEntry(String taskId);
    
    void create(AsyncTaskExecutionEntry entry);
    
    void updateStartStatus(AsyncTaskExecutionEntry entry);
    
    AsyncTaskExecutionEntry updateEndStatus(AsyncTaskExecutionEntry entry);
    
    void updateTaskParameters(AsyncTaskExecutionEntry entry);
    
    AsyncTaskExecutionEntry updateScheduledDate(AsyncTaskExecutionEntry entry);
    
    void delete(AsyncTaskExecutionEntry entry);
    
    AsyncTaskExecutionEntry getVocabularyRdfImportTaskEntryByVocabularyName(String vocabularyIdentifier);

}
