package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.QuartzSchedulerDao;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskExecutionError;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("asyncJobListener")
public class AsyncJobListener implements JobListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobListener.class);
    
    private final AsyncTaskDao asyncTaskDao;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;
    private final QuartzSchedulerDao quartzSchedulerDao;
    
    @Autowired
    public AsyncJobListener(AsyncTaskDao asyncTaskDao, AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDataSerializer asyncTaskDataSerializer, QuartzSchedulerDao quartzSchedulerDao) {
        this.asyncTaskDao = asyncTaskDao;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
        this.quartzSchedulerDao = quartzSchedulerDao;
    }
    
    @Override
    public String getName() {
        return "AsyncJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jec) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jec.getJobDetail().getKey()));
        entry.setExecutionStatus(AsyncTaskExecutionStatus.ONGOING);
        entry.setStartDate(new Date());
        this.asyncTaskDao.updateStartStatus(entry);
        LOGGER.info(String.format("Async task %s is ongoing.", entry.getTaskId()));
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jec) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jec.getJobDetail().getKey()));
        entry.setEndDate(new Date());
        entry.setExecutionStatus(AsyncTaskExecutionStatus.ABORTED);
        this.asyncTaskDao.updateEndStatus(entry);
        LOGGER.info(String.format("Async task %s was aborted.", entry.getTaskId()));
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jec.getJobDetail().getKey()));
        entry.setEndDate(new Date());
        AsyncTaskExecutionStatus status;
        Object result;
        
        if (jee == null) {
            status = AsyncTaskExecutionStatus.COMPLETED;
            result = jec.getResult();
        }
        else {
            status = AsyncTaskExecutionStatus.FAILED;
            result = new AsyncTaskExecutionError(jee.getCause().getMessage(), ExceptionUtils.getFullStackTrace(jee.getCause()));
        }
        
        entry.setExecutionStatus(status);
            
        if (result != null) {
            entry.setSerializedResult(this.asyncTaskDataSerializer.serializeResult(result));
        }
        entry.setScheduledDate(jec.getNextFireTime());
        this.asyncTaskDao.updateEndStatus(entry);
        this.asyncTaskDao.updateScheduledDate(entry);
        if (jee == null) {
            LOGGER.info(String.format("Async task %s execution complete.", entry.getTaskId()));
        } else {
            LOGGER.info(String.format("Async task %s execution failed.", entry.getTaskId()), jee);
        }
    }

}
