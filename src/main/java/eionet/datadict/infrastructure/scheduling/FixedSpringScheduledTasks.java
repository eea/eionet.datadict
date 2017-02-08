package eionet.datadict.infrastructure.scheduling;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class FixedSpringScheduledTasks {
    
        private static final Logger LOGGER = Logger.getLogger(FixedSpringScheduledTasks.class);

    
    @Scheduled(fixedRate = 10000)
public void scheduleFixedRateTask() {
    LOGGER.info("Fixed rate task - " + System.currentTimeMillis() / 1000);
}
}
