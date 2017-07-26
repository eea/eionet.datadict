package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class FixedTimeScheduledTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedTimeScheduledTasks.class);
    private final AsyncTaskHistoryDao asyncTaskHistoryDao;

    @Autowired
    public FixedTimeScheduledTasks(AsyncTaskHistoryDao asyncTaskHistoryDao) {
        this.asyncTaskHistoryDao = asyncTaskHistoryDao;
    }

    @Scheduled(cron = "0 0 12 1 * *")
    public void schedulePeriodicCleanOfOldDataOnAsyncTaskHistoryTableTask() {
        List<AsyncTaskExecutionEntryHistory> historyEntries = this.asyncTaskHistoryDao.retrieveAllTasksHistory();
        for (AsyncTaskExecutionEntryHistory historyEntry : historyEntries) {
            asyncTaskHistoryDao.deleteOlderTaskHistoryThanLastN(historyEntry.getTaskId(), 10);
        }
        LOGGER.info("Weekly Scheduled Task To Purge  Data in ASYNC_TASK_ENTRY_HISTORY table and Keep only last 10 records for each Task");
    }

}
