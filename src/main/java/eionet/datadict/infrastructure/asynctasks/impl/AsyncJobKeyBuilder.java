package eionet.datadict.infrastructure.asynctasks.impl;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;

@Service
public class AsyncJobKeyBuilder {
    
    private static final String GROUP_ID = "asyncjobs";
    
    public String getGroup() {
        return GROUP_ID;
    }
    
    public JobKey createNew() {
        return JobKey.jobKey(UUID.randomUUID().toString(), GROUP_ID);
    }
    
    public JobKey create(String taskId) {
        return JobKey.jobKey(taskId, GROUP_ID);
    }
    
    public String getTaskId(JobKey jobKey) {
        if (!StringUtils.equals(GROUP_ID, jobKey.getGroup())) {
            throw new IllegalArgumentException("Invalid async task job key: " + jobKey.toString());
        }
        
        return jobKey.getName();
    }
    
}
