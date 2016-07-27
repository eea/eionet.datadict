package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("asyncJobListener")
public class AsyncJobListener implements JobListener {

    private final AsyncTaskDao asyncTaskDao;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    @Autowired
    public AsyncJobListener(AsyncTaskDao asyncTaskDao, AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDataSerializer asyncTaskDataSerializer) {
        this.asyncTaskDao = asyncTaskDao;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
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
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jec) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jec.getJobDetail().getKey()));
        entry.setEndDate(new Date());
        entry.setExecutionStatus(AsyncTaskExecutionStatus.ABORTED);
        this.asyncTaskDao.updateEndStatus(entry);
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
            result = jee.getCause();
        }
        
        entry.setExecutionStatus(status);
            
        if (result != null) {
            entry.setSerializedResult(this.asyncTaskDataSerializer.serializeResult(result));
        }
        
        this.asyncTaskDao.updateEndStatus(entry);
    }
    
}
