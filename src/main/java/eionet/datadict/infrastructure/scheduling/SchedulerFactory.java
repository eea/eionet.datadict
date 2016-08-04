package eionet.datadict.infrastructure.scheduling;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component("jobScheduler")
public class SchedulerFactory extends SchedulerFactoryBean {
    
    private final DataSource dataSource;
    private final DataSource nonTxDataSource;
    private final PlatformTransactionManager transactionManager;
    private final Properties quartzProperties;
    
    @Autowired
    public SchedulerFactory(
            @Qualifier("dataSource") DataSource dataSource, 
            @Qualifier("nonTxDataSource") DataSource nonTxDataSource,
            PlatformTransactionManager transactionManager,
            @Qualifier("quartzProperties") Properties quartzProperties) {
        this.dataSource = dataSource;
        this.nonTxDataSource = nonTxDataSource;
        this.transactionManager = transactionManager;
        this.quartzProperties = quartzProperties;
    }
    
    @PostConstruct
    public void init() {
        this.setBeanName("jobScheduler");
        this.setDataSource(this.dataSource);
        this.setNonTransactionalDataSource(this.nonTxDataSource);
        this.setTransactionManager(this.transactionManager);
        this.setQuartzProperties(this.quartzProperties);
    }
    
}
