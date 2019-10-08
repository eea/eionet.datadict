package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskBuilder;
import eionet.meta.spring.SpringApplicationContext;
import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJob implements Job {
    
    private final AsyncTaskBuilder asyncTaskBuilder;
    
    /**
     *      */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJob.class);

    public AsyncJob() {
        this(SpringApplicationContext.getBean(AsyncTaskBuilder.class));
    }
    
    public AsyncJob(AsyncTaskBuilder asyncTaskBuilder) {
        this.asyncTaskBuilder = asyncTaskBuilder;
    }
    
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            this.executeCore(jec);
        }
        catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
    
    protected void executeCore(JobExecutionContext jec) throws Exception {
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(jec.getMergedJobDataMap());
        Class<? extends AsyncTask> taskType = dataMapAdapter.getTaskType();
        
        if (taskType == null) {
            throw new IllegalArgumentException("Asynchronous task class was not provided.");
        }
        
        Map<String, Object> taskParameters = dataMapAdapter.getParameters();
        AsyncTask task = this.asyncTaskBuilder.create(taskType, taskParameters);
        if (jec.getJobDetail() != null){
            LOGGER.info(String.format("Async job %s responds to %s.", jec.getJobDetail().getKey(), task.getClass().toString()));
        }
        Object result = task.call();
        jec.setResult(result);
    }
}
