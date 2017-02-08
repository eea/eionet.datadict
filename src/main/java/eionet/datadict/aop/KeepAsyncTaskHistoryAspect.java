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
public class KeepAsyncTaskHistoryAspect {

    private  AsyncTaskHistoryDao asyncTaskHistoryDao;
    private static final Logger LOGGER = Logger.getLogger(KeepAsyncTaskHistoryAspect.class);
    
     @AfterReturning(
      pointcut = "execution(* eionet.datadict.dal.AsyncTaskDao.updateScheduledDate(..))",
      returning= "result")
	public void logBefore(JoinPoint joinPoint,Object result) {

            asyncTaskHistoryDao = SpringApplicationContext.getBean(AsyncTaskHistoryDao.class);
	LOGGER.info("Invocation of Aspect to Store AsyncTaskEntry History");
                 asyncTaskHistoryDao.store((AsyncTaskExecutionEntry)result);
                                
        }
}
