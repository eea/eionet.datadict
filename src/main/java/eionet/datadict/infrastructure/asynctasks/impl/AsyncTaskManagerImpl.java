package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.dal.QuartzSchedulerDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManagementException;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsyncTaskManagerImpl implements AsyncTaskManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskManagerImpl.class);

    private final Scheduler scheduler;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final AsyncTaskDao asyncTaskDao;
    private final QuartzSchedulerDao quartzSchedulerDao;
    private final JobListener asyncJobListener;
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;
    private final AsyncTaskHistoryDao asyncTaskHistoryDao;

    @Autowired
    public AsyncTaskManagerImpl(@Qualifier("jobScheduler") Scheduler scheduler,
            @Qualifier("asyncJobListener") JobListener asyncJobListener,
            AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDao asyncTaskDao,
            QuartzSchedulerDao quartzSchedulerDao, AsyncTaskDataSerializer asyncTaskDataSerializer, AsyncTaskHistoryDao asyncTaskHistoryDao) {
        this.scheduler = scheduler;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDao = asyncTaskDao;
        this.quartzSchedulerDao = quartzSchedulerDao;
        this.asyncJobListener = asyncJobListener;
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
        this.asyncTaskHistoryDao = asyncTaskHistoryDao;
    }

    @PostConstruct
    public void init() {
        try {
            this.scheduler.getListenerManager().addJobListener(asyncJobListener, GroupMatcher.jobGroupEquals(asyncJobKeyBuilder.getGroup()));
        } catch (SchedulerException ex) {
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
        } catch (SchedulerException ex) {
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
        } catch (Exception ex) {
            throw new AsyncTaskManagementException(ex);
        }
    }

    protected ResourceNotFoundException createTaskNotFoundException(String taskId) {
        return new ResourceNotFoundException("Could not find task with id " + taskId);
    }

    @Override
    public <T> String scheduleTask(Class<T> taskType, Map<String, Object> parameters, Integer intervalMinutes) {
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

        SimpleTrigger trigger = newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(intervalMinutes).repeatForever()
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .forJob(jobDetail.getKey())
                .build();
        try {
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
        this.createTaskEntry(jobKey, dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());
        return this.asyncJobKeyBuilder.getTaskId(jobKey);
    }

    @Override
    public List<AsyncTaskExecutionEntry> getAllScheduledTaskEntries() {
        Set<String> scheduledTasksClassNames = new HashSet<String>();
        scheduledTasksClassNames.add(VocabularyRdfImportFromUrlTask.class.getCanonicalName());
        List<AsyncTaskExecutionEntry> asyncTaskEntries = this.asyncTaskDao.getAllEntriesByTaskClassNames(scheduledTasksClassNames);
        return asyncTaskEntries;
    }

    @Override
    public List<AsyncTaskExecutionEntryHistory> getTaskEntriesHistory() {
        return this.asyncTaskHistoryDao.retrieveAllTasksHistory();
    }

    @Override
    public void deleteTask(String taskId) {
        JobKey key = this.asyncJobKeyBuilder.create(taskId);
        try {
            this.scheduler.deleteJob(key);
            AsyncTaskExecutionEntry entry = this.asyncTaskDao.getFullEntry(taskId);
            this.asyncTaskDao.delete(entry);
        } catch (SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
    }

    @Override
    public AsyncTaskExecutionEntryHistory getTaskEntryHistory(String id) {
        return this.asyncTaskHistoryDao.retrieveTaskHistoryById(id);
    }

    @Override
    public <T> String updateScheduledTask(Class<T> taskType, Map<String, Object> parameters, Integer intervalMinutes, String taskId) {

        if (taskType == null) {
            throw new IllegalArgumentException("Task type cannot be null.");
        }
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(new JobDataMap());
        dataMapAdapter.setTaskType(taskType);
        if (parameters != null) {
            dataMapAdapter.putParameters(parameters);
        }

        JobKey jobKey = this.asyncJobKeyBuilder.create(taskId);
        JobDetail jobDetail = JobBuilder.newJob(AsyncJob.class)
                .withIdentity(jobKey)
                .setJobData(dataMapAdapter.getDataMap()).storeDurably()
                .build();

        SimpleTrigger trigger = newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(intervalMinutes).repeatForever()
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .forJob(jobDetail.getKey())
                .build();
        try {
            List<Trigger> triggers = (List<Trigger>) this.scheduler.getTriggersOfJob(jobKey);
            this.scheduler.addJob(jobDetail, true);
            this.scheduler.rescheduleJob(triggers.get(0).getKey(), trigger);
        } catch (SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
        AsyncTaskExecutionEntry existingEntry = this.asyncTaskDao.getFullEntry(taskId);
        existingEntry.setSerializedParameters(this.asyncTaskDataSerializer.serializeParameters(parameters));
        this.asyncTaskDao.updateTaskParameters(existingEntry);
        return this.asyncJobKeyBuilder.getTaskId(jobKey);
    }

    @Override
    public List<AsyncTaskExecutionEntryHistory> getTaskEntryHistoryByTaskId(String taskId) {
        return asyncTaskHistoryDao.retrieveTaskHistoryByTaskId(taskId);
    }

    @Override
    public List<AsyncTaskExecutionEntryHistory> retrieveLimitedTaskHistoryByTaskId(String taskId, int limit) {
        return asyncTaskHistoryDao.retrieveLimitedTaskHistoryByTaskId(taskId, limit);
    }
}
