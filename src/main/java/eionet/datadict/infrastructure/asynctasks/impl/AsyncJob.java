package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskBuilder;
import eionet.meta.spring.SpringApplicationContext;
import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AsyncJob implements Job {
    
    private final AsyncTaskBuilder asyncTaskBuilder;
    
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
        Object result = task.call();
        jec.setResult(result);
    }
}
