package eionet.datadict.dal;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface AsyncTaskHistoryDao {

    AsyncTaskExecutionEntryHistory retrieveTaskHistoryById(String id);

    List<AsyncTaskExecutionEntryHistory> retrieveTaskHistoryByTaskId(String taskId);

    void storeAsyncTaskEntry(AsyncTaskExecutionEntry entry);

    void updateExecutionStatusAndSerializedResult(AsyncTaskExecutionEntry entry);

    List<AsyncTaskExecutionEntryHistory> retrieveAllTasksHistory();
    
    void deleteRecordsWithScheduledDateOlderThan(Date date);
    
    void delete(Long id);
}
