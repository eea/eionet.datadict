package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
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
    public void testResult() {
        DummyTaskResult result = new DummyTaskResult(12, "Some message.");
        String serializedResult = this.asyncTaskDataSerializer.serializeResult(result);
        
        assertThat(this.asyncTaskDataSerializer.deserializeResult(serializedResult), is(equalTo((Object) result)));
    }
    
    @Test
    public void testErrorResult() {
        Exception error = this.generateError();
        String serializedResult = this.asyncTaskDataSerializer.serializeResult(error);
        Object deserializedResult = this.asyncTaskDataSerializer.deserializeResult(serializedResult);
        
        assertThat(deserializedResult.getClass(), is(equalTo((Class) error.getClass())));
        
        Exception deserializedError = (Exception) deserializedResult;
        
        assertThat(deserializedError.getMessage(), is(equalTo(error.getMessage())));
        assertThat("Stacktrace size mismatch", deserializedError.getStackTrace().length, is(equalTo(error.getStackTrace().length)));
    }
    
    private Exception generateError() {
        try {
            throw new Exception("Something went wrong.");
        }
        catch (Exception ex) {
            return ex;
        }
    }
    
}
