package eionet.datadict.dal;

import eionet.datadict.dal.impl.JdbcDaoBase;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.Before;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@SpringApplicationContext("mock-spring-context.xml")
public class AsyncTaskHistoryDaoTest extends UnitilsJUnit4 {

    @SpringBeanByType
    private AsyncTaskHistoryDao asyncTaskHistoryDao;
    @SpringBeanByName
    private DataSource dataSource;
    private TestAssistanceDao testAssistanceDao;

    @Before
    private void setup(){
    this.testAssistanceDao= new TestAssistanceDao(this.dataSource);
    
    }
    
    
     private AsyncTaskExecutionEntryHistory createBaseHistoryEntry() {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(UUID.randomUUID().toString());
        entry.setScheduledDate(new Date());
        entry.setTaskClassName("some.class.Name");
        entry.setSerializedParameters("{ param1: 1, param2: 2 }");
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setStartDate(new Date(entry.getScheduledDate().getTime() + 200));
        entry.setEndDate(new Date(entry.getStartDate().getTime() + 2000));
        entry.setSerializedResult("{ value: 5 }");
        AsyncTaskExecutionEntryHistory historyEntry = new AsyncTaskExecutionEntryHistory(entry);
        return historyEntry;
    }
    

    private static class TestAssistanceDao extends JdbcDaoBase {

        public TestAssistanceDao(DataSource dataSource) {
            super(dataSource);
        }

    }
}
