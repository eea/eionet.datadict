package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializerException;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;
import org.junit.Test;

public class AsyncTaskDataSerializerTest {

    private final AsyncTaskDataSerializer asyncTaskDataSerializer = new AsyncTaskDataSerializerImpl();
    
    @Test
    public void testParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("param1", 3);
        parameters.put("param2", false);
        parameters.put("param3", 2L);
        parameters.put("param4", "val");
        parameters.put("param5", 'c');
        String serializedParameters = this.asyncTaskDataSerializer.serializeParameters(parameters);
        
        assertThat(this.asyncTaskDataSerializer.deserializeParameters(serializedParameters), is(equalTo(parameters)));
    }
    
    @Test
    public void testNullParameters() {
        String serializedParameters = this.asyncTaskDataSerializer.serializeParameters(null);
        assertThat(serializedParameters, is(nullValue()));
        
        Map<String, Object> parameters1 = this.asyncTaskDataSerializer.deserializeParameters(null);
        assertThat(parameters1, is(nullValue()));
        
        Map<String, Object> parameters2 = this.asyncTaskDataSerializer.deserializeParameters("");
        assertThat(parameters2, is(nullValue()));
        
        Map<String, Object> parameters3 = this.asyncTaskDataSerializer.deserializeParameters("    ");
        assertThat(parameters3, is(nullValue()));
    }
    
    @Test
    public void testResult() {
        DummyTaskResult result = new DummyTaskResult(12, "Some message.");
        String serializedResult = this.asyncTaskDataSerializer.serializeResult(result);
        
        assertThat(this.asyncTaskDataSerializer.deserializeResult(serializedResult), is(equalTo((Object) result)));
    }
    
    @Test
    public void testNullResult() {
        String serializedResult = this.asyncTaskDataSerializer.serializeResult(null);
        assertThat(serializedResult, is(nullValue()));
        
        Object result1 = this.asyncTaskDataSerializer.deserializeResult(null);
        assertThat(result1, is(nullValue()));
        
        Object result2 = this.asyncTaskDataSerializer.deserializeResult("");
        assertThat(result2, is(nullValue()));
        
        Object result3 = this.asyncTaskDataSerializer.deserializeResult("   ");
        assertThat(result3, is(nullValue()));
    }
    
    @Test
    public void testSerializerFailure() {
        try {
            this.asyncTaskDataSerializer.deserializeParameters("{ param1: ' }");
            fail("Should have thrown AsyncTaskDataSerializerException");
        }
        catch (AsyncTaskDataSerializerException ex) { }
        
        try {
            this.asyncTaskDataSerializer.deserializeResult("{ prop1: ' }");
            fail("Should have thrown AsyncTaskDataSerializerException");
        }
        catch (AsyncTaskDataSerializerException ex) { }
    }
    
}
