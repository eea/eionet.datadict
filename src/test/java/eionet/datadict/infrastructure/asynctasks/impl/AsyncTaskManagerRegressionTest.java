package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-context.xml" })
public class AsyncTaskManagerRegressionTest {

    @Autowired
    private AsyncTaskManager asyncTaskManager;
    
    @Autowired
    private AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    @Before
    public void setUp() {
        System.err.println("----------------------------------------------------------------");
    }
    
    @Test
    public void testTaskSuccess() throws InterruptedException, ResourceNotFoundException {
        final Date testStartDate = new Date();
        final long taskDurationMillis = 5 * 1000;
        final Class taskClass = AsyncJobTestTask.class;
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(AsyncJobTestTask.PARAM_DURATION, taskDurationMillis);

        String taskId = this.asyncTaskManager.executeAsync(taskClass, parameters);

        Thread.sleep(taskDurationMillis / 2);
        AsyncTaskExecutionStatus intermediateStatus = this.asyncTaskManager.getExecutionStatus(taskId);
        
        this.awaitTaskCompletion(taskId, 5 * 60 * 1000);
        
        AsyncTaskExecutionEntry finalEntry = this.asyncTaskManager.getTaskEntry(taskId);
        
        assertThat(intermediateStatus, is(anyOf(equalTo(AsyncTaskExecutionStatus.SCHEDULED), equalTo(AsyncTaskExecutionStatus.ONGOING))));
        assertThat(finalEntry, is(notNullValue()));
        assertThat(finalEntry.getTaskId(), is(equalTo(taskId)));
        assertThat(finalEntry.getTaskClassName(), is(equalTo(taskClass.getName())));
        assertThat(finalEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.COMPLETED)));
        assertThat(finalEntry.getScheduledDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(finalEntry.getStartDate(), is(greaterThanOrEqualTo(finalEntry.getScheduledDate())));
        assertThat(finalEntry.getEndDate(), is(greaterThan(finalEntry.getStartDate())));
        Map<String, Object> deserializedParameters = this.asyncTaskDataSerializer.deserializeParameters(finalEntry.getSerializedParameters());
        assertThat(deserializedParameters, is(equalTo(parameters)));
        Object deserializedResult = this.asyncTaskDataSerializer.deserializeResult(finalEntry.getSerializedResult());
        assertThat(deserializedResult, is(equalTo((Object) AsyncJobTestTask.RESULT_VALUE)));
    }
    
    @Test
    public void testTaskFailure() throws InterruptedException, ResourceNotFoundException {
        final Date testStartDate = new Date();
        final Class taskClass = AsyncJobTestTask.class;
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(AsyncJobTestTask.PARAM_RAISE_ERROR, true);

        String taskId = this.asyncTaskManager.executeAsync(taskClass, parameters);
        
        this.awaitTaskCompletion(taskId, 5 * 60 * 1000);
        
        AsyncTaskExecutionEntry finalEntry = this.asyncTaskManager.getTaskEntry(taskId);
        
        assertThat(finalEntry, is(notNullValue()));
        assertThat(finalEntry.getTaskId(), is(equalTo(taskId)));
        assertThat(finalEntry.getTaskClassName(), is(equalTo(taskClass.getName())));
        assertThat(finalEntry.getExecutionStatus(), is(equalTo(AsyncTaskExecutionStatus.FAILED)));
        assertThat(finalEntry.getScheduledDate(), is(greaterThanOrEqualTo(testStartDate)));
        assertThat(finalEntry.getStartDate(), is(greaterThanOrEqualTo(finalEntry.getScheduledDate())));
        assertThat(finalEntry.getEndDate(), is(greaterThan(finalEntry.getStartDate())));
        Map<String, Object> deserializedParameters = this.asyncTaskDataSerializer.deserializeParameters(finalEntry.getSerializedParameters());
        assertThat(deserializedParameters, is(equalTo(parameters)));
        Object deserializedResult = this.asyncTaskDataSerializer.deserializeResult(finalEntry.getSerializedResult());
        assertThat(deserializedResult, is(instanceOf(Exception.class)));
        assertThat(((Exception) deserializedResult).getMessage(), is(equalTo(AsyncJobTestTask.ERROR_MESSAGE)));
    }
    
    private void awaitTaskCompletion(String taskId, long maxAwaitDurationMillis) throws InterruptedException, ResourceNotFoundException {
        final long sleepDurationMillis = 1000;
        long timeRemainingMillis = maxAwaitDurationMillis;
        AsyncTaskExecutionStatus status;
        
        do {
            timeRemainingMillis -= sleepDurationMillis;
            Thread.sleep(sleepDurationMillis);
            status = this.asyncTaskManager.getExecutionStatus(taskId);
        }
        while (AsyncTaskExecutionStatus.isPending(status) && timeRemainingMillis > 0);
    }
    
}
