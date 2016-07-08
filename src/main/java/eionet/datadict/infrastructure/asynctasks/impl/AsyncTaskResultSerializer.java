package eionet.datadict.infrastructure.asynctasks.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskResultSerializer {

    private final ObjectMapper mapper;
    
    public AsyncTaskResultSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }
    
    public String serializeResult(Object result) throws AsyncTaskResultSerializerException {
        try {
            Object toSerialize = result instanceof JobExecutionException ? ((Exception) result).getCause() : result;
            
            return this.mapper.writeValueAsString(toSerialize);
        }
        catch (JsonProcessingException ex) {
            throw new AsyncTaskResultSerializerException(ex);
        }
    }
    
    public Object deserializeResult(String serializedResult) throws AsyncTaskResultSerializerException {
        try {
            return this.mapper.readValue(serializedResult, Object.class);
        } 
        catch (IOException ex) {
            throw new AsyncTaskResultSerializerException(ex);
        }
    }
    
}
