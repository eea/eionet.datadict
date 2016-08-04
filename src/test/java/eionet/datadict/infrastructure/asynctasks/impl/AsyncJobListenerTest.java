package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskExecutionError;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import org.apache.commons.lang.exception.ExceptionUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

public class AsyncJobListenerTest {
    
    @Mock
    private AsyncTaskDao asyncTaskDao;
    @Spy
    private AsyncJobKeyBuilder asyncJobKeyBuilder;
    @Mock
    private AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    @InjectMocks
    private AsyncJobListener asyncJobListener;
    
    
    @Mock
    private JobExecutionContext jec;
    @Mock
    private JobDetail jobDetail;
    
    private JobKey jobKey;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        this.jobKey = this.asyncJobKeyBuilder.createNew();
        when(this.jobDetail.getKey()).thenReturn(this.jobKey);
        when(this.jec.getJobDetail()).thenReturn(this.jobDetail);
    }
    
    @Test
    public void testJobToBeExecuted() {
        Date testStartDate = new Date();
        
        this.asyncJobListener.jobToBeExecuted(this.jec);
        
        ArgumentCaptor<AsyncTaskExecutionEntry> entryCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionEntry.class);
        verify(this.asyncTaskDao, times(1)).updateStartStatus(entryCaptor.capture());
        
        AsyncTaskExecutionEntry capturedEntry = entryCaptor.getValue();
        
        assertThat(capturedEntry.getTaskId(), is(equalTo(this.asyncJobKeyBuilder.getTaskId(this.jobKey))));
        assertThat(capturedEntry.getStartDate(), is(notNullValue()));
        assertThat(capturedEntry.getStartDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(capturedEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.ONGOING)));
    }
    
    @Test
    public void testJobExecutionVetoed() {
        Date testStartDate = new Date();
        
        this.asyncJobListener.jobExecutionVetoed(this.jec);
        
        ArgumentCaptor<AsyncTaskExecutionEntry> entryCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionEntry.class);
        verify(this.asyncTaskDao, times(1)).updateEndStatus(entryCaptor.capture());
        
        AsyncTaskExecutionEntry capturedEntry = entryCaptor.getValue();
        
        assertThat(capturedEntry.getTaskId(), is(equalTo(this.asyncJobKeyBuilder.getTaskId(this.jobKey))));
        assertThat(capturedEntry.getEndDate(), is(notNullValue()));
        assertThat(capturedEntry.getEndDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(capturedEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.ABORTED)));
    }
    
    @Test
    public void testJobWasExecuted() {
        Date testStartDate = new Date();
        
        this.asyncJobListener.jobWasExecuted(this.jec, null);
        
        ArgumentCaptor<AsyncTaskExecutionEntry> entryCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionEntry.class);
        verify(this.asyncTaskDao, times(1)).updateEndStatus(entryCaptor.capture());
        verify(this.asyncTaskDataSerializer, times(0)).serializeResult(any());
        
        AsyncTaskExecutionEntry capturedEntry = entryCaptor.getValue();
        
        assertThat(capturedEntry.getTaskId(), is(equalTo(this.asyncJobKeyBuilder.getTaskId(this.jobKey))));
        assertThat(capturedEntry.getEndDate(), is(notNullValue()));
        assertThat(capturedEntry.getEndDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(capturedEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.COMPLETED)));
    }
    
    @Test
    public void testJobWasExecutedWithResult() {
        Date testStartDate = new Date();
        final DummyTaskResult result = new DummyTaskResult(5, "Some success message.");
        final String serializedResult = "{ value: 5, message: 'Some success message' }";
        when(this.jec.getResult()).thenReturn(result);
        when(this.asyncTaskDataSerializer.serializeResult(result)).thenReturn(serializedResult);
        
        this.asyncJobListener.jobWasExecuted(this.jec, null);
        
        ArgumentCaptor<AsyncTaskExecutionEntry> entryCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionEntry.class);
        verify(this.asyncTaskDao, times(1)).updateEndStatus(entryCaptor.capture());
        verify(this.asyncTaskDataSerializer, times(1)).serializeResult(result);
        
        AsyncTaskExecutionEntry capturedEntry = entryCaptor.getValue();
        
        assertThat(capturedEntry.getTaskId(), is(equalTo(this.asyncJobKeyBuilder.getTaskId(this.jobKey))));
        assertThat(capturedEntry.getEndDate(), is(notNullValue()));
        assertThat(capturedEntry.getEndDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(capturedEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.COMPLETED)));
        assertThat(capturedEntry.getSerializedResult(), is(equalTo(serializedResult)));
    }
    
    @Test
    public void testJobWasExecutedWithException() {
        Date testStartDate = new Date();
        final JobExecutionException jee = new JobExecutionException(new IllegalArgumentException("Some argument was wrong."));
        final String serializedError = "{ message: 'Some argument was wrong.' }";
        
        when(this.asyncTaskDataSerializer.serializeResult(any())).thenReturn(serializedError);
        
        this.asyncJobListener.jobWasExecuted(this.jec, jee);
        
        ArgumentCaptor<AsyncTaskExecutionEntry> entryCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionEntry.class);
        verify(this.asyncTaskDao, times(1)).updateEndStatus(entryCaptor.capture());
        ArgumentCaptor<AsyncTaskExecutionError> errorCaptor = ArgumentCaptor.forClass(AsyncTaskExecutionError.class);
        verify(this.asyncTaskDataSerializer, times(1)).serializeResult(errorCaptor.capture());
        
        AsyncTaskExecutionEntry capturedEntry = entryCaptor.getValue();
        
        assertThat(capturedEntry.getTaskId(), is(equalTo(this.asyncJobKeyBuilder.getTaskId(this.jobKey))));
        assertThat(capturedEntry.getEndDate(), is(notNullValue()));
        assertThat(capturedEntry.getEndDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(capturedEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.FAILED)));
        assertThat(capturedEntry.getSerializedResult(), is(equalTo(serializedError)));
        
        AsyncTaskExecutionError capturedError = errorCaptor.getValue();
        
        assertThat(capturedError.getMessage(), is(equalTo(jee.getCause().getMessage())));
        assertThat(capturedError.getTechnicalDetails(), is(equalTo(ExceptionUtils.getFullStackTrace(jee.getCause()))));
    }
    
}
