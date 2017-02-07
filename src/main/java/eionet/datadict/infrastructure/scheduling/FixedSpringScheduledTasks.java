/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.infrastructure.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class FixedSpringScheduledTasks {
    
    
    @Scheduled(fixedRate = 10000)
public void scheduleFixedRateTask() {
    System.out.println(
      "Fixed rate task - " + System.currentTimeMillis() / 1000);
}
}
