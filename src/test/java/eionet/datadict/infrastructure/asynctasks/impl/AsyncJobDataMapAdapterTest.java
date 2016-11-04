package eionet.datadict.infrastructure.asynctasks.impl;

import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;

public class AsyncJobDataMapAdapterTest {

    private JobDataMap dataMap;
    
    @Before
    public void setUp() {
        this.dataMap = new JobDataMap();
    }
    
    @Test
    public void testParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("param1", 5);
        parameters.put("param2", "text");
        parameters.put("param3", false);
        AsyncJobDataMapAdapter adapter1 = new AsyncJobDataMapAdapter(dataMap);
        adapter1.putParameters(parameters);
        AsyncJobDataMapAdapter adapter2 = new AsyncJobDataMapAdapter(dataMap);
        assertThat(adapter2.getParameters(), is(equalTo(parameters)));
    }
    
    @Test
    public void testTaskType() {
        final Class taskType = AsyncJobTestTask.class;
        AsyncJobDataMapAdapter adapter1 = new AsyncJobDataMapAdapter(dataMap);
        adapter1.setTaskType(taskType);
        AsyncJobDataMapAdapter adapter2 = new AsyncJobDataMapAdapter(dataMap);
        assertThat(adapter2.getTaskType(), is(equalTo(taskType)));
    }
    
    @Test
    public void testNullTaskType() {
        AsyncJobDataMapAdapter adapter = new AsyncJobDataMapAdapter(dataMap);
        assertThat(adapter.getTaskType(), is(nullValue()));
        assertThat(adapter.getTaskTypeName(), is(nullValue()));
    }
    
}
