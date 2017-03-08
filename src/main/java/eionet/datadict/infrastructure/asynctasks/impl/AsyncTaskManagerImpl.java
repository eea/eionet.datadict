package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.QuartzSchedulerDao;
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
    private final QuartzSchedulerDao quartzSchedulerDao;
    private final JobListener asyncJobListener;
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    @Autowired
    public AsyncTaskManagerImpl(@Qualifier("jobScheduler") Scheduler scheduler, 
            @Qualifier("asyncJobListener") JobListener asyncJobListener,
            AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDao asyncTaskDao,
            QuartzSchedulerDao quartzSchedulerDao, AsyncTaskDataSerializer asyncTaskDataSerializer) {
        this.scheduler = scheduler;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDao = asyncTaskDao;
        this.quartzSchedulerDao = quartzSchedulerDao;
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
        
        this.createTaskEntry(jobKey, dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());

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
            throw this.createTaskNotFoundException(taskId);
        }
        
        this.fixEntryExecutionStatus(entry);
        
        return entry.getExecutionStatus();
    }
    
    @Override
    @Transactional
    public AsyncTaskExecutionEntry getTaskEntry(String taskId) throws AsyncTaskManagementException, ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = this.asyncTaskDao.getFullEntry(taskId);
        
        if (entry == null) {
            throw this.createTaskNotFoundException(taskId);
        }
        
        this.fixEntryExecutionStatus(entry);
        
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
    
    protected void fixEntryExecutionStatus(AsyncTaskExecutionEntry entry) throws AsyncTaskManagementException {
        if (!AsyncTaskExecutionStatus.isPending(entry.getExecutionStatus())) {
            return;
        }
        
        if (this.hasTriggers(entry.getTaskId())) {
            return;
        }
        
        entry.setExecutionStatus(AsyncTaskExecutionStatus.KILLED);
        this.asyncTaskDao.updateEndStatus(entry);
        LOGGER.info(String.format("Async task %s is assumed to have been killed upon execution.", entry.getTaskId()));
    }
    
    protected boolean hasTriggers(String taskId) throws AsyncTaskManagementException {
        JobKey jobKey = this.asyncJobKeyBuilder.create(taskId);
        
        try {
            return this.quartzSchedulerDao.hasTriggersOfJob(this.scheduler.getSchedulerName(), jobKey);
        }
        catch (Exception ex) {
            throw new AsyncTaskManagementException(ex);
        }
    }
    
    protected ResourceNotFoundException createTaskNotFoundException(String taskId) {
        return new ResourceNotFoundException("Could not find task with id " + taskId);
    }
    
}
