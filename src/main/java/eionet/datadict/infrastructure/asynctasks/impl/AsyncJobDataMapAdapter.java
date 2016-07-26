package eionet.datadict.infrastructure.asynctasks.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.quartz.JobDataMap;

public class AsyncJobDataMapAdapter {

    private static final String TASK_TYPE_KEY = "async_task_class";
    private static final String PARAM_KEY_PREFIX = "prm_";
    
    private final JobDataMap dataMap;
    
    public AsyncJobDataMapAdapter(JobDataMap dataMap) {
        this.dataMap = dataMap;
    }
    
    public JobDataMap getDataMap() {
        return this.dataMap;
    }
    
    public String getTaskTypeName() {
        return this.dataMap.getString(TASK_TYPE_KEY);
    }
    
    public Class getTaskType() {
        String taskTypeName = this.dataMap.getString(TASK_TYPE_KEY);
        
        if (taskTypeName == null) {
            return null;
        }
        
        try {
            return Class.forName(taskTypeName);
        } 
        catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
    public void setTaskType(Class taskType) {
        this.dataMap.put(TASK_TYPE_KEY, taskType.getName());
    }
    
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        
        for (Entry<String, Object> entry : this.dataMap.entrySet()) {
            if (entry.getKey().startsWith(PARAM_KEY_PREFIX)) {
                String paramKey = entry.getKey().substring(PARAM_KEY_PREFIX.length());
                parameters.put(paramKey, entry.getValue());
            }
        }
        
        return parameters;
    }
    
    public void putParameters(Map<String, Object> parameters) {
        for (Entry<String, Object> entry : parameters.entrySet()) {
            String paramKey = PARAM_KEY_PREFIX + entry.getKey();
            this.dataMap.put(paramKey, entry.getValue());
        }
    }
    
}
