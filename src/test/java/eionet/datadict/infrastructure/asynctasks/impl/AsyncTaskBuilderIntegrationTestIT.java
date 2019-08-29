package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskBuilder;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-context.xml" })
public class AsyncTaskBuilderIntegrationTestIT {
    
    @Autowired
    private AsyncTaskBuilder asyncTaskBuilder;
    
    @Test
    public void testCreate() {
        final Class taskType = AsyncJobTestTask.class;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("param1", "value1");
        AsyncTask task = this.asyncTaskBuilder.create(taskType, parameters);
        assertThat(task, is(instanceOf(taskType)));
        assertThat(((AsyncJobTestTask) task).getParameters(), is(equalTo(parameters)));
    }
    
}
