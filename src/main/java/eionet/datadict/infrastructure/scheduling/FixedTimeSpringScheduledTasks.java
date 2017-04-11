package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.dal.AsyncTaskHistoryDao;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class FixedTimeSpringScheduledTasks {

    private static final Logger LOGGER = Logger.getLogger(FixedTimeSpringScheduledTasks.class);
    private final AsyncTaskHistoryDao asyncTaskHistoryDao;

    @Autowired
    public FixedTimeSpringScheduledTasks(AsyncTaskHistoryDao asyncTaskHistoryDao) {
        this.asyncTaskHistoryDao = asyncTaskHistoryDao;
    }

    @Scheduled(cron = "0 0 12 1 * *")
    public void schedulePeriodicCleanOfOldDataOnAsyncTaskHistoryTableTask() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date olderDate = cal.getTime();
        asyncTaskHistoryDao.deleteRecordsWithScheduledDateOlderThan(olderDate);
        LOGGER.info("Monthly Scheduled Task To Delete Data in ASYNC_TASK_ENTRY_HISTORY table older than : " + olderDate);
    }

}
