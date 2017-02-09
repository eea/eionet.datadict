package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface ScheduleJobService {
    
        <T> String scheduleJob(Class<T> taskType, Map<String, Object> parameters, Integer intervalMinutes);
            String getJobStatus(String jobId);
            List<AsyncTaskExecutionEntry> getAllScheduledTaskEntries();
            List<AsyncTaskExecutionEntry> getTaskEntriesHistory();
            AsyncTaskExecutionEntry getTaskEntry(String jobId);
}
