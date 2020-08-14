package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.dal.CleanMysqlSystemDbLoggingTableDao;
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
    private final CleanMysqlSystemDbLoggingTableDao cleanMySqlLoggingTableDao;

    @Autowired
    public FixedTimeScheduledTasks(AsyncTaskHistoryDao asyncTaskHistoryDao, CleanMysqlSystemDbLoggingTableDao cleanMySqlLoggingTableDao) {
        this.asyncTaskHistoryDao = asyncTaskHistoryDao;
        this.cleanMySqlLoggingTableDao = cleanMySqlLoggingTableDao;
    }

    @Scheduled(cron = "0 0 12 1 * *")
    public void schedulePeriodicCleanOfOldDataOnAsyncTaskHistoryTableTask() {
        List<AsyncTaskExecutionEntryHistory> historyEntries = this.asyncTaskHistoryDao.retrieveAllTasksHistory();
        for (AsyncTaskExecutionEntryHistory historyEntry : historyEntries) {
            asyncTaskHistoryDao.deleteOlderTaskHistoryThanLastN(historyEntry.getTaskId(), 10);
        }
        LOGGER.info("Weekly Scheduled Task To Purge  Data in ASYNC_TASK_ENTRY_HISTORY table and Keep only last 10 records for each Task");
    }

    @Scheduled(cron = "0 0 8 1 * ?")  //runs first day of every month at 8am
    public void schedulePeriodicCleanOfOldDataOnMySqlGeneralLogTableTask() {
        cleanMySqlLoggingTableDao.delete();
        LOGGER.info("Monthly Scheduled Task To Delete Data in mysql.general_log table that are older that one month");
    }

}
