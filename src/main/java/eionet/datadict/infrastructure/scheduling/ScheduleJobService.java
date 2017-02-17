package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface ScheduleJobService {
    
        <T> String scheduleJob(Class<T> taskType, Map<String, Object> parameters, Integer intervalMinutes);
            List<AsyncTaskExecutionEntry> getAllScheduledTaskEntries();
            List<AsyncTaskExecutionEntryHistory> getTaskEntriesHistory();
            AsyncTaskExecutionEntry getTaskEntry(String taskId);
            AsyncTaskExecutionEntry getTaskEntryHistory(String taskId);
            void deleteJob(String taskId);
}
