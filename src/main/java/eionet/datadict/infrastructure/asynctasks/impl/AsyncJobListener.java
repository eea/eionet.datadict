package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import net.sf.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("asyncJobListener")
public class AsyncJobListener implements JobListener {

    private final AsyncTaskDao asyncTaskDao;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final AsyncTaskResultSerializer asyncTaskResultSerializer;
    
    @Autowired
    public AsyncJobListener(AsyncTaskDao asyncTaskDao, AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskResultSerializer asyncTaskResultSerializer) {
        this.asyncTaskDao = asyncTaskDao;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskResultSerializer = asyncTaskResultSerializer;
    }
    
    @Override
    public String getName() {
        return "AsyncJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jec) {
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(jec.getMergedJobDataMap());
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jec.getJobDetail().getKey()));
        entry.setTaskClassName(dataMapAdapter.getTaskTypeName());
        entry.setExecutionStatus(AsyncTaskExecutionStatus.ONGOING);
        entry.setStartDate(new Date());
        entry.setSerializedParameters(JSONObject.fromObject(dataMapAdapter.getParameters()).toString());
        this.asyncTaskDao.create(entry);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jec) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jec.getJobDetail().getKey()));
        entry.setEndDate(new Date());
        entry.setExecutionStatus(AsyncTaskExecutionStatus.ABORTED);
        this.asyncTaskDao.updateStatus(entry);
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
            result = jee;
        }
        
        entry.setExecutionStatus(status);
            
        if (jec.getResult() != null) {
            entry.setSerializedResult(this.asyncTaskResultSerializer.serializeResult(result));
        }
        
        this.asyncTaskDao.updateStatus(entry);
    }
    
}
