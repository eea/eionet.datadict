package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManagementException;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsyncTaskManagerImpl implements AsyncTaskManager {

    private final Scheduler scheduler;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final AsyncTaskDao asyncTaskDao;
    private final JobListener asyncJobListener;
    
    @Autowired
    public AsyncTaskManagerImpl(@Qualifier("jobScheduler") Scheduler scheduler, 
            @Qualifier("asyncJobListener") JobListener asyncJobListener,
            AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDao asyncTaskDao) {
        this.scheduler = scheduler;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDao = asyncTaskDao;
        this.asyncJobListener = asyncJobListener;
    }
    
    @PostConstruct
    public void init() {
        try {
            this.scheduler.getListenerManager().addJobListener(asyncJobListener, GroupMatcher.jobGroupEquals(asyncJobKeyBuilder.getGroup()));
        }
        catch(SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
    }
    
    @Override
    public <T extends AsyncTask> String executeAsync(Class<T> taskType, Map<String, Object> parameters) 
            throws AsyncTaskManagementException {
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(new JobDataMap());
        dataMapAdapter.setTaskType(taskType);
        dataMapAdapter.putParameters(parameters);
        JobKey jobKey = this.asyncJobKeyBuilder.createNew();
        
        JobDetail jobDetail = JobBuilder.newJob(AsyncJob.class)
                .withIdentity(jobKey)
                .setJobData(dataMapAdapter.getDataMap())
                .build();
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .startNow();
        
        try {
            this.scheduler.scheduleJob(jobDetail, triggerBuilder.build());
        }
        catch (SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
        
        return this.asyncJobKeyBuilder.getTaskId(jobKey);
    }

    @Override
    @Transactional
    public AsyncTaskExecutionStatus getExecutionStatus(String taskId) 
            throws AsyncTaskManagementException, ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = this.asyncTaskDao.getStatusEntry(taskId);
        
        if (entry == null) {
            throw new ResourceNotFoundException();
        }
        
        if (entry.getExecutionStatus() != AsyncTaskExecutionStatus.ONGOING) {
            return entry.getExecutionStatus();
        }
        
        if (this.hasTriggers(taskId)) {
            return AsyncTaskExecutionStatus.ONGOING;
        }
        
        entry.setExecutionStatus(AsyncTaskExecutionStatus.KILLED);
        this.asyncTaskDao.updateStatus(entry);
        
        return AsyncTaskExecutionStatus.KILLED;
    }
    
    protected boolean hasTriggers(String taskId) throws AsyncTaskManagementException {
        JobKey jobKey = this.asyncJobKeyBuilder.create(taskId);
        
        try {
            return !this.scheduler.getTriggersOfJob(jobKey).isEmpty();
        }
        catch (SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
    }
    
}
