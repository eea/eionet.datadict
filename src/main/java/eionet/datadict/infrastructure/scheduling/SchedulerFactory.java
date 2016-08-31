package eionet.datadict.infrastructure.scheduling;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component("jobScheduler")
public class SchedulerFactory extends SchedulerFactoryBean {
    
    private final DataSource nonTxDataSource;
    private final Properties quartzProperties;
    
    @Autowired
    public SchedulerFactory(@Qualifier("nonTxDataSource") DataSource nonTxDataSource, 
            @Qualifier("quartzProperties") Properties quartzProperties) {
        this.nonTxDataSource = nonTxDataSource;
        this.quartzProperties = quartzProperties;
    }
    
    @PostConstruct
    public void init() {
        this.setBeanName("jobScheduler");
        this.setNonTransactionalDataSource(this.nonTxDataSource);
        this.setQuartzProperties(this.quartzProperties);
    }
    
}
