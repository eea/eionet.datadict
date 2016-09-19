package eionet.datadict.infrastructure.asynctasks;

import java.util.Map;

public interface AsyncTaskDataSerializer {

    String serializeParameters(Map<String, Object> parameters) throws AsyncTaskDataSerializerException;
    
    Map<String, Object> deserializeParameters(String serializedParameters) throws AsyncTaskDataSerializerException;
    
    String serializeResult(Object result) throws AsyncTaskDataSerializerException;
    
    Object deserializeResult(String serializedResult) throws AsyncTaskDataSerializerException;
    
}
