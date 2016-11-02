package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTaskBuilder;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AsyncJobTest {
    
    @Mock
    private JobExecutionContext jec;
    
    @Spy
    private JobDataMap dataMap;
    
    @Spy
    @InjectMocks
    private AsyncJobDataMapAdapter dataMapAdapter;
    
    @Mock
    private AsyncTaskBuilder asyncTaskBuilder;
    
    @Spy
    @InjectMocks
    private AsyncJob asyncJob;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testSuccessfullTaskExecution() throws JobExecutionException {
        this.dataMapAdapter.setTaskType(AsyncJobTestTask.class);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(AsyncJobTestTask.PARAM_RAISE_ERROR, false);
        this.dataMapAdapter.putParameters(parameters);
        when(this.asyncTaskBuilder.create(AsyncJobTestTask.class, parameters)).thenReturn(new AsyncJobTestTask(parameters));
        
        when(jec.getMergedJobDataMap()).thenReturn(this.dataMap);
        
        this.asyncJob.execute(this.jec);
        
        verify(this.jec, times(1)).getMergedJobDataMap();
        verify(this.asyncTaskBuilder, times(1)).create(AsyncJobTestTask.class, parameters);
        verify(this.jec, times(1)).setResult(eq(AsyncJobTestTask.RESULT_VALUE));
    }
    
    @Test
    public void testRaiseTaskError() {
        this.dataMapAdapter.setTaskType(AsyncJobTestTask.class);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(AsyncJobTestTask.PARAM_RAISE_ERROR, true);
        this.dataMapAdapter.putParameters(parameters);
        when(this.asyncTaskBuilder.create(AsyncJobTestTask.class, parameters)).thenReturn(new AsyncJobTestTask(parameters));
        
        when(jec.getMergedJobDataMap()).thenReturn(this.dataMap);
        
        try {
            this.asyncJob.execute(this.jec);
            fail("Should have raised JobExecutionException.");
        }
        catch (JobExecutionException ex) {
            assertThat(ex.getCause(), is(notNullValue()));
            assertThat(ex.getCause().getMessage(), is(equalTo(AsyncJobTestTask.ERROR_MESSAGE)));
        }
        
        verify(this.jec, times(1)).getMergedJobDataMap();
        verify(this.asyncTaskBuilder, times(1)).create(AsyncJobTestTask.class, parameters);
        verify(this.jec, times(0)).setResult(any());
    }
    
    @Test
    public void testUndefinedTaskType() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(AsyncJobTestTask.PARAM_RAISE_ERROR, true);
        this.dataMapAdapter.putParameters(parameters);
        
        when(jec.getMergedJobDataMap()).thenReturn(this.dataMap);
        
        try {
            this.asyncJob.execute(this.jec);
            fail("Should have raised JobExecutionException.");
        }
        catch (JobExecutionException ex) {
            assertThat(ex.getCause(), is(notNullValue()));
            assertThat(ex.getCause().getClass(), is(equalTo((Class) IllegalArgumentException.class)));
        }
        
        verify(this.jec, times(1)).getMergedJobDataMap();
        verify(this.asyncTaskBuilder, times(0)).create(any(Class.class), any(Map.class));
        verify(this.jec, times(0)).setResult(any());
    }
    
    @Test
    public void testAnyRandomJobError() throws Exception {
        Exception error = new Exception("Any kind of random error.");
        doThrow(error).when(this.asyncJob).executeCore(this.jec);
        
        try {
            this.asyncJob.execute(this.jec);
            fail("Should have raised JobExecutionException.");
        }
        catch (JobExecutionException ex) {
            assertThat(ex.getCause(), is(equalTo((Throwable) error)));
        }
        
        verify(this.jec, times(0)).getMergedJobDataMap();
        verify(this.jec, times(0)).setResult(any());
    }
    
}
