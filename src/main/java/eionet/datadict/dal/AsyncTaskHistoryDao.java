package eionet.datadict.dal;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface AsyncTaskHistoryDao {

    AsyncTaskExecutionEntry retrieveTaskById(String id);
    
    List<AsyncTaskExecutionEntry> retrieveTasksByTaskId(String taskId);
    void store(AsyncTaskExecutionEntry entry);
    
    List<AsyncTaskExecutionEntry> retrieveAllTasksHistory();
}
