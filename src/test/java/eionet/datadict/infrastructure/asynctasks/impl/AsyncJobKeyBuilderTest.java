package eionet.datadict.infrastructure.asynctasks.impl;

import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.quartz.JobKey;

public class AsyncJobKeyBuilderTest {
    
    private final AsyncJobKeyBuilder keyBuilder = new AsyncJobKeyBuilder();
    
    @Test
    public void testCreate() {
        JobKey key = this.keyBuilder.createNew();
        assertThat(key.getGroup(), is(equalTo("asyncjobs")));
        UUID taskId = UUID.fromString(key.getName());
        assertThat(taskId, is(notNullValue()));
    }
    
    @Test
    public void testCreateFromTaskId() {
        final String taskId = UUID.randomUUID().toString();
        JobKey key = this.keyBuilder.create(taskId);
        assertThat(key.getGroup(), is(equalTo("asyncjobs")));
        assertThat(key.getName(), is(equalTo(taskId)));
    }
    
    @Test
    public void testGetTaskId() {
        final String taskId = UUID.randomUUID().toString();
        JobKey validKey = this.keyBuilder.create(taskId);
        assertThat(this.keyBuilder.getTaskId(validKey), is(equalTo(taskId)));
        
        JobKey invalidKey = JobKey.jobKey(taskId, "invalid_group");
        
        try {
            this.keyBuilder.getTaskId(invalidKey);
            fail("Should have not accepted " + invalidKey.getGroup() + " as an async job group name.");
        }
        catch (IllegalArgumentException ex) { }
    }
    
}
