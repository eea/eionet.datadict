package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManagementException;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(AsyncTaskManagerImpl.class);
    
    private final Scheduler scheduler;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final AsyncTaskDao asyncTaskDao;
    private final JobListener asyncJobListener;
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    @Autowired
    public AsyncTaskManagerImpl(@Qualifier("jobScheduler") Scheduler scheduler, 
            @Qualifier("asyncJobListener") JobListener asyncJobListener,
            AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDao asyncTaskDao,
            AsyncTaskDataSerializer asyncTaskDataSerializer) {
        this.scheduler = scheduler;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDao = asyncTaskDao;
        this.asyncJobListener = asyncJobListener;
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
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
    @Transactional
    public <T extends AsyncTask> String executeAsync(Class<T> taskType, Map<String, Object> parameters) 
            throws AsyncTaskManagementException {
        if (taskType == null) {
            throw new IllegalArgumentException("Task type cannot be null.");
        }
        
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(new JobDataMap());
        dataMapAdapter.setTaskType(taskType);
        
        if (parameters != null) {
            dataMapAdapter.putParameters(parameters);
        }
        
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
        
        this.createTaskEntry(jobKey, dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());
        
        return this.asyncJobKeyBuilder.getTaskId(jobKey);
    }

    @Override
    @Transactional
    public AsyncTaskExecutionStatus getExecutionStatus(String taskId) 
            throws AsyncTaskManagementException, ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = this.asyncTaskDao.getStatusEntry(taskId);
        
        if (entry == null) {
            throw this.createTaskNotFoundException(taskId);
        }
        
        return this.getExecutionStatusForEntry(entry);
    }
    
    @Override
    @Transactional
    public AsyncTaskExecutionEntry getTaskEntry(String taskId) throws ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = this.asyncTaskDao.getFullEntry(taskId);
        
        if (entry == null) {
            throw this.createTaskNotFoundException(taskId);
        }
        
        entry.setExecutionStatus(this.getExecutionStatusForEntry(entry));
        
        return entry;
    }
    
    protected void createTaskEntry(JobKey jobKey, String taskTypeName, Map<String, Object> parameters) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jobKey));
        entry.setTaskClassName(taskTypeName);
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setScheduledDate(new Date());
        entry.setSerializedParameters(this.asyncTaskDataSerializer.serializeParameters(parameters));
        this.asyncTaskDao.create(entry);
        LOGGER.info(String.format("Created async task with id: %s", entry.getTaskId()));
    }
    
    protected AsyncTaskExecutionStatus getExecutionStatusForEntry(AsyncTaskExecutionEntry entry) {
        if (entry.getExecutionStatus() != AsyncTaskExecutionStatus.SCHEDULED && entry.getExecutionStatus() != AsyncTaskExecutionStatus.ONGOING) {
            return entry.getExecutionStatus();
        }
        
        if (this.hasTriggers(entry.getTaskId())) {
            return entry.getExecutionStatus();
        }
        
        entry.setExecutionStatus(AsyncTaskExecutionStatus.KILLED);
        this.asyncTaskDao.updateEndStatus(entry);
        LOGGER.info(String.format("Async task %s is assumed to have been killed upon execution.", entry.getTaskId()));
        
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
    
    protected ResourceNotFoundException createTaskNotFoundException(String taskId) {
        return new ResourceNotFoundException("Could not find task with id " + taskId);
    }
    
}
