package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
    
}
