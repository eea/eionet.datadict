package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.dal.CleanMysqlSystemDbLoggingTableDao;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    private final CleanMysqlSystemDbLoggingTableDao cleanMySqlLoggingTableDao;

    @Autowired
    public FixedTimeScheduledTasks(CleanMysqlSystemDbLoggingTableDao cleanMySqlLoggingTableDao) {
        this.cleanMySqlLoggingTableDao = cleanMySqlLoggingTableDao;
    }

    @Scheduled(cron = "0 0 8 1 * ?") //runs first day of every month at 8am
    @SchedulerLock(name = "mySqlGeneralLogTableCleanupLock")
    public void schedulePeriodicCleanOfOldDataOnMySqlGeneralLogTableTask() {
        LockAssert.assertLocked();
        cleanMySqlLoggingTableDao.delete();
        LOGGER.info("Monthly Scheduled Task To Delete Data in mysql.general_log table that are older that one month");
    }

}
