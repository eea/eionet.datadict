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

    public List<AsyncTaskExecutionEntryHistory> retrieveLimitedTaskHistoryByTaskId(String taskId, int limit);

    void storeAsyncTaskEntry(AsyncTaskExecutionEntry entry);

    void updateExecutionStatusAndSerializedResult(AsyncTaskExecutionEntry entry);

    List<AsyncTaskExecutionEntryHistory> retrieveAllTasksHistory();

    void deleteRecordsWithScheduledDateOlderThan(Date date);

    /**
     *@param taskId the taskId of the Async Task whose history Records will be purged
     * @param LastRecordsCount the Number of the last Records to keep and delete the older ones.
     * The last Records are Determined by ordering the Data with ID descending
     **/
    void deleteOlderTaskHistoryThanLastN(String taskId,int lastRecordsCount);
    void delete(Long id);
}
