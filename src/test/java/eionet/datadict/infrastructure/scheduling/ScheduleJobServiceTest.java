package eionet.datadict.infrastructure.scheduling;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.dal.QuartzSchedulerDao;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJob;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJobDataMapAdapter;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJobKeyBuilder;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJobTestTask;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncTaskDataSerializerImpl;
import eionet.datadict.infrastructure.scheduling.impl.ScheduleJobServiceImpl;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.ListenerManager;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class ScheduleJobServiceTest {

    @Mock
    private Scheduler scheduler;
    @Spy
    private AsyncJobKeyBuilder asyncJobKeyBuilder;
    @Mock
    private AsyncTaskDao asyncTaskDao;
    @Mock
    private AsyncTaskHistoryDao asyncTaskHistoryDao;
    @Mock
    private QuartzSchedulerDao quartzSchedulerDao;
    @Mock
    private JobListener asyncJobListener;
    @Spy
    private AsyncTaskDataSerializerImpl asyncTaskDataSerializer;

    @InjectMocks
    @Spy
    private ScheduleJobServiceImpl scheduleJobsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit() throws SchedulerException {
        ListenerManager listenerManager = Mockito.mock(ListenerManager.class);
        when(this.scheduler.getListenerManager()).thenReturn(listenerManager);
        this.scheduleJobsService.init();
        ArgumentCaptor<Matcher> matcherCaptor = ArgumentCaptor.forClass(Matcher.class);
        verify(listenerManager, times(1)).addJobListener(eq(this.asyncJobListener), matcherCaptor.capture());
        Matcher matcher = matcherCaptor.getValue();
        assertThat("Job key matcher is not an instance of GroupMatcher", matcher instanceof GroupMatcher, is(equalTo(true)));
        GroupMatcher<JobKey> groupMatcher = (GroupMatcher) matcher;
        assertThat("Group matcher does not match standard async job key.", groupMatcher.isMatch(this.asyncJobKeyBuilder.createNew()), is(equalTo(true)));
    }

    @Test(expected = ScheduleJobServiceException.class)
    public void testInitThrowsScheduleJobServiceException() throws SchedulerException {
        when(this.scheduler.getListenerManager()).thenThrow(ScheduleJobServiceException.class);
        this.scheduleJobsService.init();
    }

    @Test
    public void testScheduleJob() throws SchedulerException {
        final Class<VocabularyRdfImportFromUrlTask> taskType = VocabularyRdfImportFromUrlTask.class;
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("param1", 1);
        Integer scheduleIntervalMinutes = 10;
        doNothing().when(this.scheduleJobsService).createTaskEntry(any(JobKey.class), any(String.class), any(Map.class));
        this.scheduleJobsService.scheduleJob(taskType, taskParams, scheduleIntervalMinutes);
        verify(this.asyncJobKeyBuilder, times(1)).createNew();
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<SimpleTrigger> triggerCaptor = ArgumentCaptor.forClass(SimpleTrigger.class);
        verify(this.scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());
        JobDetail capturedJobDetail = jobDetailCaptor.getValue();
        SimpleTrigger capturedTrigger = triggerCaptor.getValue();
        assertThat(capturedJobDetail.getJobClass(), is(equalTo((Class) AsyncJob.class)));
        assertThat(capturedTrigger.getJobKey(), is(equalTo(capturedJobDetail.getKey())));
        assertThat(capturedTrigger.getRepeatInterval(), is(equalTo(TimeUnit.MINUTES.toMillis(scheduleIntervalMinutes.longValue()))));
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(capturedJobDetail.getJobDataMap());
        assertThat(dataMapAdapter.getTaskType(), is(equalTo((Class) taskType)));
        assertThat(dataMapAdapter.getParameters(), is(equalTo(taskParams)));
        verify(this.scheduleJobsService, times(1)).createTaskEntry(capturedJobDetail.getKey(), dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());
        verify(this.asyncJobKeyBuilder, times(1)).getTaskId(capturedJobDetail.getKey());
    }

    @Test
    public void testCreateTaskEntry() {
        Date testStartDate = new Date();
        final JobKey jobKey = this.asyncJobKeyBuilder.createNew();
        final String taskTypeName = AsyncJobTestTask.class.getName();
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("param1", 1);
        final String serializedParams = "{ param1: 1 }";
        doReturn(serializedParams).when(this.asyncTaskDataSerializer).serializeParameters(taskParams);
        this.scheduleJobsService.createTaskEntry(jobKey, taskTypeName, taskParams);
        verify(this.asyncJobKeyBuilder, times(1)).getTaskId(jobKey);
        verify(this.asyncTaskDataSerializer, times(1)).serializeParameters(taskParams);
        ArgumentCaptor<AsyncTaskExecutionEntry> entryCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionEntry.class);
        verify(this.asyncTaskDao, times(1)).create(entryCaptor.capture());
        AsyncTaskExecutionEntry capturedEntry = entryCaptor.getValue();
        assertThat(capturedEntry.getTaskId(), is(equalTo(this.asyncJobKeyBuilder.getTaskId(jobKey))));
        assertThat(capturedEntry.getTaskClassName(), is(equalTo(taskTypeName)));
        assertThat(capturedEntry.getScheduledDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(capturedEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.SCHEDULED)));
        assertThat(capturedEntry.getSerializedParameters(), is(equalTo(serializedParams)));
    }

    @Test
    public void testGetAllScheduledTaskEntries() {
        AsyncTaskExecutionEntry entry1 = new AsyncTaskExecutionEntry();
        entry1.setTaskId("1");
        AsyncTaskExecutionEntry entry2 = new AsyncTaskExecutionEntry();
        entry1.setTaskId("2");
        List<AsyncTaskExecutionEntry> taskEntries = new LinkedList<AsyncTaskExecutionEntry>();
        taskEntries.add(entry1);
        taskEntries.add(entry2);
        when(this.asyncTaskDao.getAllEntries()).thenReturn(taskEntries);
        List<AsyncTaskExecutionEntry> allEntries = this.scheduleJobsService.getAllScheduledTaskEntries();
        verify(this.asyncTaskDao, times(1)).getAllEntries();
        Assert.assertEquals(taskEntries, allEntries);
    }

    @Test
    public void testGetAllScheduledTaskEntriesHistory() {
        AsyncTaskExecutionEntry entry1 = new AsyncTaskExecutionEntry();
        entry1.setTaskId("1");
        AsyncTaskExecutionEntryHistory hEntry1 = new AsyncTaskExecutionEntryHistory(entry1);
        AsyncTaskExecutionEntry entry2 = new AsyncTaskExecutionEntry();
        entry1.setTaskId("2");
        AsyncTaskExecutionEntryHistory hEntry2 = new AsyncTaskExecutionEntryHistory(entry2);
        List<AsyncTaskExecutionEntryHistory> entriesHistory = new LinkedList<AsyncTaskExecutionEntryHistory>();
        entriesHistory.add(hEntry1);
        entriesHistory.add(hEntry2);
        when(this.asyncTaskHistoryDao.retrieveAllTasksHistory()).thenReturn(entriesHistory);
        List<AsyncTaskExecutionEntryHistory> allEntriesHistoryToBeTested = this.scheduleJobsService.getTaskEntriesHistory();
        verify(this.asyncTaskHistoryDao, times(1)).retrieveAllTasksHistory();
        Assert.assertEquals(entriesHistory, allEntriesHistoryToBeTested);
    }

    @Test
    public void testGetTaskEntry() {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        String taskId = "22";
        entry.setTaskId(taskId);
        when(this.asyncTaskDao.getFullEntry(taskId)).thenReturn(entry);
        AsyncTaskExecutionEntry result = this.scheduleJobsService.getTaskEntry(taskId);
        assertThat(result, is(equalTo(entry)));
        verify(this.asyncTaskDao, times(1)).getFullEntry(taskId);
    }

    @Test
    public void testGetTaskEntryHistory() {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        String taskId = "22";
        entry.setTaskId(taskId);
        AsyncTaskExecutionEntryHistory hEntry = new AsyncTaskExecutionEntryHistory(entry);
        hEntry.setId(Long.parseLong("1"));
        when(this.asyncTaskHistoryDao.retrieveTaskByTaskHistoryId("1")).thenReturn(hEntry);
        AsyncTaskExecutionEntryHistory result = this.scheduleJobsService.getTaskEntryHistory("1");
        assertThat(result, is(equalTo(hEntry)));
        verify(this.asyncTaskHistoryDao, times(1)).retrieveTaskByTaskHistoryId("1");
    }

    @Test
    public void testDeleteJob() throws SchedulerException {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        String taskId = "22";
        entry.setTaskId(taskId);
        when(this.asyncTaskDao.getFullEntry(taskId)).thenReturn(entry);
        this.scheduleJobsService.deleteJob(taskId);
        verify(this.asyncJobKeyBuilder, times(1)).create(taskId);
        verify(this.scheduler, times(1)).deleteJob(any(JobKey.class));
        verify(this.asyncTaskDao,times(1)).delete(entry);
    }
    
    @Test
    public void testUpdateScheduledJob() throws SchedulerException {
        final Class<VocabularyRdfImportFromUrlTask> taskType = VocabularyRdfImportFromUrlTask.class;
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        String taskId = "22624";
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(taskId);
        Integer intervalMinutes = 80;
        taskParams.put("param1", 1);
        SimpleTrigger trigger = newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(intervalMinutes).repeatForever()
                        .withMisfireHandlingInstructionIgnoreMisfires()).withIdentity(new TriggerKey("some Trigger"))
                .build();
        List<Trigger> triggers = new ArrayList<Trigger>();
        triggers.add(trigger);
        doReturn(triggers).when(this.scheduler).getTriggersOfJob(any(JobKey.class));
        when(this.asyncTaskDao.getFullEntry(taskId)).thenReturn(entry);
        this.scheduleJobsService.updateScheduledJob(taskType, taskParams, intervalMinutes, taskId);
        verify(this.asyncJobKeyBuilder, times(1)).create(taskId);
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<TriggerKey> triggerKeyCaptor = ArgumentCaptor.forClass(TriggerKey.class);
        ArgumentCaptor<SimpleTrigger> triggerCaptor = ArgumentCaptor.forClass(SimpleTrigger.class);
        verify(this.scheduler, times(1)).addJob(jobDetailCaptor.capture(), any(Boolean.class));
        verify(this.scheduler, times(1)).rescheduleJob(triggerKeyCaptor.capture(), triggerCaptor.capture());
        JobDetail capturedJobDetail = jobDetailCaptor.getValue();
        SimpleTrigger capturedTrigger = triggerCaptor.getValue();
        TriggerKey triggerKey = triggerKeyCaptor.getValue();
        assertThat(capturedJobDetail.getJobClass(), is(equalTo((Class) AsyncJob.class)));
        assertThat(capturedTrigger.getJobKey(), is(equalTo(capturedJobDetail.getKey())));
        assertThat(capturedTrigger.getRepeatInterval(), is(equalTo(TimeUnit.MINUTES.toMillis(intervalMinutes.longValue()))));
        assertThat(triggerKey, is(equalTo(triggers.get(0).getKey())));
        verify(this.asyncTaskDao, times(1)).getFullEntry(taskId);
        verify(this.asyncTaskDao, times(1)).updateTaskParameters(entry);
        verify(this.asyncJobKeyBuilder, times(1)).getTaskId(capturedJobDetail.getKey());
    }
}
