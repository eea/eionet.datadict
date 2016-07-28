package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManagementException;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.fail;
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
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

public class AsyncTaskManagerTest {
    
    @Mock
    private Scheduler scheduler;
    @Spy
    private AsyncJobKeyBuilder asyncJobKeyBuilder;
    @Mock
    private AsyncTaskDao asyncTaskDao;
    @Mock
    private JobListener asyncJobListener;
    @Spy
    private AsyncTaskDataSerializerImpl asyncTaskDataSerializer;
    
    @InjectMocks
    @Spy
    private AsyncTaskManagerImpl asyncTaskManager;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testInit() throws SchedulerException {
        ListenerManager listenerManager = Mockito.mock(ListenerManager.class);
        when(this.scheduler.getListenerManager()).thenReturn(listenerManager);
        
        this.asyncTaskManager.init();
        
        ArgumentCaptor<Matcher> matcherCaptor = ArgumentCaptor.forClass(Matcher.class);
        verify(listenerManager, times(1)).addJobListener(eq(this.asyncJobListener), matcherCaptor.capture());
        Matcher matcher = matcherCaptor.getValue();
        assertThat("Job key matcher is not an instance of GroupMatcher", matcher instanceof GroupMatcher, is(equalTo(true)));
        GroupMatcher<JobKey> groupMatcher = (GroupMatcher) matcher;
        assertThat("Group matcher does not match standard async job key.", groupMatcher.isMatch(this.asyncJobKeyBuilder.createNew()), is(equalTo(true)));
    }
    
    @Test
    public void testInitWrapSchedulerException() throws SchedulerException {
        when(this.scheduler.getListenerManager()).thenThrow(SchedulerException.class);
        
        try {
            this.asyncTaskManager.init();
            fail("Should have thrown AsyncTaskManagementException");
        }
        catch (AsyncTaskManagementException ex) {
            assertThat(ex.getCause(), is(notNullValue()));
            assertThat(ex.getCause(), is(instanceOf(SchedulerException.class)));
        }
    }
    
    @Test
    public void testExecuteAsync() throws SchedulerException {
        final Class<AsyncJobTestTask> taskType = AsyncJobTestTask.class;
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("param1", 1);
        doNothing().when(this.asyncTaskManager).createTaskEntry(any(JobKey.class), any(String.class), any(Map.class));
        
        this.asyncTaskManager.executeAsync(taskType, taskParams);
        
        verify(this.asyncJobKeyBuilder, times(1)).createNew();
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(this.scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());
        JobDetail capturedJobDetail = jobDetailCaptor.getValue();
        Trigger capturedTrigger = triggerCaptor.getValue();
        assertThat(capturedJobDetail.getJobClass(), is(equalTo((Class) AsyncJob.class)));
        assertThat(capturedTrigger.getJobKey(), is(equalTo(capturedJobDetail.getKey())));
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(capturedJobDetail.getJobDataMap());
        assertThat(dataMapAdapter.getTaskType(), is(equalTo((Class) taskType)));
        assertThat(dataMapAdapter.getParameters(), is(equalTo(taskParams)));
        verify(this.asyncTaskManager, times(1)).createTaskEntry(capturedJobDetail.getKey(), dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());
        verify(this.asyncJobKeyBuilder, times(1)).getTaskId(capturedJobDetail.getKey());
    }
    
    @Test
    public void testExecuteAsyncRejectNullTaskType() {
        try {
            this.asyncTaskManager.executeAsync(null, null);
            fail("Should have not accepted null async task type.");
        }
        catch (IllegalArgumentException ex) { }
    }
    
    @Test
    public void testExecuteAsyncAcceptNullParameters() throws SchedulerException {
        final Class<AsyncJobTestTask> taskType = AsyncJobTestTask.class;
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        doNothing().when(this.asyncTaskManager).createTaskEntry(any(JobKey.class), any(String.class), any(Map.class));
        
        this.asyncTaskManager.executeAsync(taskType, null); // should be able to work with null as well
        
        verify(this.asyncJobKeyBuilder, times(1)).createNew();
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(this.scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());
        JobDetail capturedJobDetail = jobDetailCaptor.getValue();
        Trigger capturedTrigger = triggerCaptor.getValue();
        assertThat(capturedJobDetail.getJobClass(), is(equalTo((Class) AsyncJob.class)));
        assertThat(capturedTrigger.getJobKey(), is(equalTo(capturedJobDetail.getKey())));
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(capturedJobDetail.getJobDataMap());
        assertThat(dataMapAdapter.getTaskType(), is(equalTo((Class) taskType)));
        assertThat(dataMapAdapter.getParameters(), is(equalTo(taskParams))); // The adapter should always create a map, even if empty
        verify(this.asyncTaskManager, times(1)).createTaskEntry(capturedJobDetail.getKey(), dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());
        verify(this.asyncJobKeyBuilder, times(1)).getTaskId(capturedJobDetail.getKey());
    }
    
    @Test
    public void testExecuteWrapSchedulerException() throws SchedulerException {
        final Class<AsyncJobTestTask> taskType = AsyncJobTestTask.class;
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        when(this.scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenThrow(SchedulerException.class);
        
        try {
            this.asyncTaskManager.executeAsync(taskType, taskParams);
            fail("Should have thrown AsyncTaskManagementException");
        }
        catch (AsyncTaskManagementException ex) {
            assertThat(ex.getCause(), is(notNullValue()));
            assertThat(ex.getCause(), is(instanceOf(SchedulerException.class)));
        }
        
        verify(this.asyncTaskManager, times(0)).createTaskEntry(any(JobKey.class), any(String.class), any(Map.class));
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
        
        this.asyncTaskManager.createTaskEntry(jobKey, taskTypeName, taskParams);
        
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
    public void testGetExecutionStatus() throws ResourceNotFoundException {
        final String taskId = "some-task-id";
        final AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(taskId);
        when(this.asyncTaskDao.getStatusEntry(taskId)).thenReturn(entry);
        doNothing().when(this.asyncTaskManager).fixEntryExecutionStatus(entry);
        
        this.asyncTaskManager.getExecutionStatus(taskId);
        
        verify(this.asyncTaskDao, times(1)).getStatusEntry(taskId);
        verify(this.asyncTaskManager, times(1)).fixEntryExecutionStatus(entry);
    }
    
    @Test
    public void testGetExecutionStatusTaskNotFound() {
        final String taskId = "some-task-id";
        when(this.asyncTaskDao.getStatusEntry(taskId)).thenReturn(null);
        
        try {
            this.asyncTaskManager.getExecutionStatus(taskId);
            fail("Should have thrown ResourceNotFoundExeption");
        }
        catch (ResourceNotFoundException ex) { }
        
        verify(this.asyncTaskDao, times(1)).getStatusEntry(taskId);
        verify(this.asyncTaskManager, times(0)).fixEntryExecutionStatus(any(AsyncTaskExecutionEntry.class));
    }
    
    @Test
    public void testGetTaskEntry() throws ResourceNotFoundException {
        final String taskId = "some-task-id";
        final AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(taskId);
        when(this.asyncTaskDao.getFullEntry(taskId)).thenReturn(entry);
        doNothing().when(this.asyncTaskManager).fixEntryExecutionStatus(entry);
        
        AsyncTaskExecutionEntry result = this.asyncTaskManager.getTaskEntry(taskId);
        
        assertThat(result, is(equalTo(entry)));
        verify(this.asyncTaskDao, times(1)).getFullEntry(taskId);
        verify(this.asyncTaskManager, times(1)).fixEntryExecutionStatus(entry);
    }
    
    @Test
    public void testGetTaskEntryTaskNotFound() {
        final String taskId = "some-task-id";
        when(this.asyncTaskDao.getFullEntry(taskId)).thenReturn(null);
        
        try {
            this.asyncTaskManager.getTaskEntry(taskId);
            fail("Should have thrown ResourceNotFoundExeption");
        }
        catch (ResourceNotFoundException ex) { }
        
        verify(this.asyncTaskDao, times(1)).getFullEntry(taskId);
        verify(this.asyncTaskManager, times(0)).fixEntryExecutionStatus(any(AsyncTaskExecutionEntry.class));
    }
    
    @Test
    public void testFixEntryExecutionStatus1() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED, true, AsyncTaskExecutionStatus.SCHEDULED, 1, 0);
    }
    
    @Test
    public void testFixEntryExecutionStatus2() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED, false, AsyncTaskExecutionStatus.KILLED, 1, 1);
    }
    
    @Test
    public void testFixEntryExecutionStatus3() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.ONGOING, true, AsyncTaskExecutionStatus.ONGOING, 1, 0);
    }
    
    @Test
    public void testFixEntryExecutionStatus4() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.ONGOING, false, AsyncTaskExecutionStatus.KILLED, 1, 1);
    }
    
    @Test
    public void testFixEntryExecutionStatus5() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.COMPLETED, null, AsyncTaskExecutionStatus.COMPLETED, 0, 0);
    }
    
    @Test
    public void testFixEntryExecutionStatus6() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.ABORTED, null, AsyncTaskExecutionStatus.ABORTED, 0, 0);
    }
    
    @Test
    public void testFixEntryExecutionStatus7() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.FAILED, null, AsyncTaskExecutionStatus.FAILED, 0, 0);
    }
    
    @Test
    public void testFixEntryExecutionStatus8() {
        this.testFixEntryExecutionStatus(AsyncTaskExecutionStatus.KILLED, null, AsyncTaskExecutionStatus.KILLED, 0, 0);
    }
    
    private void testFixEntryExecutionStatus(AsyncTaskExecutionStatus dbStatus, Boolean hasTriggers, 
            AsyncTaskExecutionStatus expectedStatus, int verifiedHasTriggersCalls, int verifiedDaoCalls) {
        final String taskId = "some-task-id";
        final AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(taskId);
        entry.setExecutionStatus(dbStatus);
        
        if (hasTriggers != null) {
            doReturn(hasTriggers).when(this.asyncTaskManager).hasTriggers(taskId);
        }
        
        this.asyncTaskManager.fixEntryExecutionStatus(entry);
        
        assertThat(entry.getExecutionStatus(), is(equalTo(expectedStatus)));
        verify(this.asyncTaskManager, times(verifiedHasTriggersCalls)).hasTriggers(taskId);
        verify(this.asyncTaskDao, times(verifiedDaoCalls)).updateEndStatus(entry);
        
    }
    
    @Test
    public void testHasTriggers1() throws SchedulerException {
        this.testHasTriggers();
    }
    
    @Test
    public void testHasTriggers2() throws SchedulerException {
        this.testHasTriggers(Mockito.mock(Trigger.class));
    }
    
    private void testHasTriggers(Trigger... triggers) throws SchedulerException {
        final String taskId = "some-task-id";
        final JobKey jobKey = this.asyncJobKeyBuilder.create(taskId);
        Mockito.reset(this.asyncJobKeyBuilder);
        when(this.scheduler.getTriggersOfJob(jobKey)).thenReturn((List) Arrays.asList(triggers));
        
        boolean result = this.asyncTaskManager.hasTriggers(taskId);
        
        assertThat(result, is(equalTo(triggers.length > 0)));
        verify(this.asyncJobKeyBuilder, times(1)).create(taskId);
        verify(this.scheduler, times(1)).getTriggersOfJob(jobKey);
    }
    
}
