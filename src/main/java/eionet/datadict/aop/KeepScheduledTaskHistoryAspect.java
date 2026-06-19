package eionet.datadict.aop;

import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Aspect
@Component
public class KeepScheduledTaskHistoryAspect {

    private final AsyncTaskHistoryDao asyncTaskHistoryDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(KeepScheduledTaskHistoryAspect.class);

    public KeepScheduledTaskHistoryAspect(AsyncTaskHistoryDao asyncTaskHistoryDao) {
        this.asyncTaskHistoryDao = asyncTaskHistoryDao;
    }

    @AfterReturning(
            pointcut = "execution(* eionet.datadict.dal.AsyncTaskDao.updateScheduledDate(..))",
            returning = "result")
    public void persistAsyncTaskEntryHistory(AsyncTaskExecutionEntry result) {
        if (result == null) {
            return;
        }
        LOGGER.info("Invocation of Aspect to Store AsyncTaskEntry History upon updating the Scheduled Date of an Async Task Entry.");
        asyncTaskHistoryDao.storeAsyncTaskEntry(result);
    }

    @AfterReturning(
            pointcut = "execution(* eionet.datadict.dal.AsyncTaskDao.updateEndStatus(..))",
            returning = "result")
    public void updateAsyncTaskEntryHistoryResult(AsyncTaskExecutionEntry result) {
        if (result == null) {
            return;
        }
        LOGGER.info("Invocation of Aspect to Update AsyncTaskEntry History upon updating the End Status and Serialized Result of an Async Task Entry.");
        asyncTaskHistoryDao.updateExecutionStatusAndSerializedResult(result);
    }

}
