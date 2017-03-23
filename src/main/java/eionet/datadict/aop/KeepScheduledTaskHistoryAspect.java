package eionet.datadict.aop;

import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.meta.spring.SpringApplicationContext;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Aspect
@Component
public class KeepScheduledTaskHistoryAspect {

    private AsyncTaskHistoryDao asyncTaskHistoryDao;
    private static final Logger LOGGER = Logger.getLogger(KeepScheduledTaskHistoryAspect.class);

    @AfterReturning(
            pointcut = "execution(* eionet.datadict.dal.AsyncTaskDao.updateScheduledDate(..))",
            returning = "result")
    public void persistAsyncTaskEntryHistory(JoinPoint joinPoint, Object result) {
        LOGGER.info("Invocation of Aspect to Store AsyncTaskEntry History upon updating the Scheduled Date of an Async Task Entry");
        getAsyncTaskHistoryDao().storeAsyncTaskEntry((AsyncTaskExecutionEntry) result);
    }
    
     @AfterReturning(
            pointcut = "execution(* eionet.datadict.dal.AsyncTaskDao.updateEndStatus(..))",
            returning = "result")
    public void updateAsyncTaskEntryHistoryResult(JoinPoint joinPoint, Object result) {
        LOGGER.info("Invocation of Aspect to Update AsyncTaskEntry History upon updating the End Status and Serialized Result of an Async Task Entry");
        getAsyncTaskHistoryDao().updateExecutionStatusAndSerializedResult((AsyncTaskExecutionEntry) result);
    }
    
    public AsyncTaskHistoryDao getAsyncTaskHistoryDao(){
    if(asyncTaskHistoryDao==null){
    this.asyncTaskHistoryDao = SpringApplicationContext.getBean(AsyncTaskHistoryDao.class); 
    }
    return this.asyncTaskHistoryDao;
    }
}
