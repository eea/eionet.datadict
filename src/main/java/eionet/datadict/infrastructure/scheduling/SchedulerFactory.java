package eionet.datadict.infrastructure.scheduling;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component("jobScheduler")
public class SchedulerFactory extends SchedulerFactoryBean {
    
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    
    @Autowired
    public SchedulerFactory(DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }
    
    @PostConstruct
    public void init() {
        this.setBeanName("jobScheduler");
        this.setDataSource(this.dataSource);
        this.setTransactionManager(this.transactionManager);
    }
    
}
