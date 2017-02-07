package eionet.datadict.dal;

import eionet.datadict.aop.*;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.meta.spring.SpringApplicationContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Aspect
@Component
public class KeepAsyncTaskHistoryAspect {


    
    private  AsyncTaskHistoryDao asyncTaskHistoryDao;
    
    
    
     @AfterReturning(
      pointcut = "execution(* eionet.datadict.dal.AsyncTaskDao.updateScheduledDate(..))",
      returning= "result")
	public void logBefore(JoinPoint joinPoint,Object result) {

            asyncTaskHistoryDao = SpringApplicationContext.getBean(AsyncTaskHistoryDao.class);
		System.out.println("logBefore() is running!");
		System.out.println("hijacked : " + joinPoint.getSignature().getName());
		System.out.println("******");
                System.out.println("AsyncTaskDao.updateScheduledDate() returned :"+((AsyncTaskExecutionEntry)result).getTaskClassName());
                System.out.println("AsyncTaskDao.updateScheduledDate() returned :"+((AsyncTaskExecutionEntry)result).getSerializedParameters());
                                System.out.println("AsyncTaskDao.updateScheduledDate() returned :"+((AsyncTaskExecutionEntry)result).getTaskId());
                 asyncTaskHistoryDao.store((AsyncTaskExecutionEntry)result);
                                
        }
}
