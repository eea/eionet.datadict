package eionet.datadict.dal;

import org.quartz.JobKey;

public interface QuartzSchedulerDao {

    boolean hasTriggersOfJob(String schedulerName, JobKey jobKey);
    
}
