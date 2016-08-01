package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.meta.spring.SpringApplicationContext;
import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AsyncJob implements Job {
    
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
        AsyncTask task = SpringApplicationContext.getBean(taskType);
        task.setParameters(taskParameters);
        Object result = task.call();
        jec.setResult(result);
    }
}
