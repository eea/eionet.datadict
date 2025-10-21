package eionet.datadict.infrastructure.asynctasks.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializerException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskDataSerializerImpl implements AsyncTaskDataSerializer {

    private final ObjectMapper mapper;
    
    public AsyncTaskDataSerializerImpl() {
        this.mapper = new ObjectMapper();
        this.mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }
    
    @Override
    public String serializeParameters(Map<String, Object> parameters) throws AsyncTaskDataSerializerException {
        if (parameters == null) {
            return null;
        }
        
        try {
            return this.mapper.writeValueAsString(parameters);
        }
        catch (JsonProcessingException ex) {
            throw new AsyncTaskDataSerializerException(ex);
        }
    }

    @Override
    public Map<String, Object> deserializeParameters(String serializedParameters) throws AsyncTaskDataSerializerException {
        if (StringUtils.isBlank(serializedParameters)) {
            return null;
        }
        
        try {
            return this.mapper.readValue(serializedParameters, Map.class);
        } 
        catch (IOException ex) {
            throw new AsyncTaskDataSerializerException(ex);
        }
    }
    
    @Override
    public String serializeResult(Object result) throws AsyncTaskDataSerializerException {
        if (result == null) {
            return null;
        }
        
        try {
            Object toSerialize = result instanceof JobExecutionException ? ((Exception) result).getCause() : result;
            
            return this.mapper.writeValueAsString(toSerialize);
        }
        catch (JsonProcessingException ex) {
            throw new AsyncTaskDataSerializerException(ex);
        }
    }
    
    @Override
    public Object deserializeResult(String serializedResult) throws AsyncTaskDataSerializerException {
        if (StringUtils.isBlank(serializedResult)) {
            return null;
        }
        
        try {
            return this.mapper.readValue(serializedResult, Object.class);
        } 
        catch (IOException ex) {
            throw new AsyncTaskDataSerializerException(ex);
        }
    }
    
}
