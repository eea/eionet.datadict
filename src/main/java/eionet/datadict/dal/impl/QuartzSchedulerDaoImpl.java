package eionet.datadict.dal.impl;

import eionet.datadict.dal.QuartzSchedulerDao;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class QuartzSchedulerDaoImpl extends JdbcDaoBase implements QuartzSchedulerDao {

    @Autowired
    public QuartzSchedulerDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public boolean hasTriggersOfJob(String schedulerName, JobKey jobKey) {
        String sql = "select count(*) from QRTZ_TRIGGERS where SCHED_NAME = :schedulerName and JOB_NAME = :jobName and JOB_GROUP = :jobGroup";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("schedulerName", schedulerName);
        parameters.put("jobName", jobKey.getName());
        parameters.put("jobGroup", jobKey.getGroup());
        int triggerCount = this.getNamedParameterJdbcTemplate().queryForInt(sql, parameters);
        
        return triggerCount > 0;
    }
    
}
