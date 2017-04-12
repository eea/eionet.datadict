package eionet.datadict.dal;

import eionet.datadict.dal.impl.JdbcDaoBase;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

@SpringApplicationContext("mock-spring-context.xml")
public class AsyncTaskDaoTest extends UnitilsJUnit4 {
    
    @SpringBeanByType
    private AsyncTaskDao asyncTaskDao;
    @SpringBeanByName
    private DataSource dataSource;
    private TestAssistanceDao testAssistanceDao;
    
    
    private AsyncTaskExecutionEntry baseEntry;
    
    @Before
    public void setUp() {
        this.testAssistanceDao = new TestAssistanceDao(this.dataSource);
        this.baseEntry = this.createBaseEntry();
        this.asyncTaskDao.create(this.baseEntry);
    }
    
    @After
    public void tearDown() {
        this.testAssistanceDao.deleteEntry(this.baseEntry.getTaskId());
    }
    
    @Test
    public void testFullFetches() {
        AsyncTaskExecutionEntry entry1Full = this.asyncTaskDao.getFullEntry(baseEntry.getTaskId());
        assertThat(entry1Full.getTaskId(), is(equalTo(baseEntry.getTaskId())));
        assertThat(entry1Full.getTaskClassName(), is(equalTo(baseEntry.getTaskClassName())));
        assertThat(entry1Full.getExecutionStatus(), is(equalTo(baseEntry.getExecutionStatus())));
        assertThat(entry1Full.getScheduledDate(), is(equalTo(baseEntry.getScheduledDate())));
        assertThat(entry1Full.getSerializedParameters(), is(equalTo(baseEntry.getSerializedParameters())));
        assertThat(entry1Full.getStartDate(), is(nullValue()));
        assertThat(entry1Full.getEndDate(), is(nullValue()));
        assertThat(entry1Full.getSerializedResult(), is(nullValue()));
        
        baseEntry.setExecutionStatus(AsyncTaskExecutionStatus.ONGOING);
        this.asyncTaskDao.updateStartStatus(baseEntry);
        
        AsyncTaskExecutionEntry entry2Full = this.asyncTaskDao.getFullEntry(baseEntry.getTaskId());
        assertThat(entry2Full.getTaskId(), is(equalTo(baseEntry.getTaskId())));
        assertThat(entry2Full.getTaskClassName(), is(equalTo(baseEntry.getTaskClassName())));
        assertThat(entry2Full.getExecutionStatus(), is(equalTo(baseEntry.getExecutionStatus())));
        assertThat(entry2Full.getScheduledDate(), is(equalTo(baseEntry.getScheduledDate())));
        assertThat(entry2Full.getSerializedParameters(), is(equalTo(baseEntry.getSerializedParameters())));
        assertThat(entry2Full.getStartDate(), is(equalTo(baseEntry.getStartDate())));
        assertThat(entry2Full.getEndDate(), is(nullValue()));
        assertThat(entry2Full.getSerializedResult(), is(nullValue()));
        
        baseEntry.setExecutionStatus(AsyncTaskExecutionStatus.COMPLETED);
        this.asyncTaskDao.updateEndStatus(baseEntry);
        
        AsyncTaskExecutionEntry entry3Full = this.asyncTaskDao.getFullEntry(baseEntry.getTaskId());
        assertThat(entry3Full.getTaskId(), is(equalTo(baseEntry.getTaskId())));
        assertThat(entry3Full.getTaskClassName(), is(equalTo(baseEntry.getTaskClassName())));
        assertThat(entry3Full.getExecutionStatus(), is(equalTo(baseEntry.getExecutionStatus())));
        assertThat(entry3Full.getScheduledDate(), is(equalTo(baseEntry.getScheduledDate())));
        assertThat(entry3Full.getSerializedParameters(), is(equalTo(baseEntry.getSerializedParameters())));
        assertThat(entry3Full.getStartDate(), is(equalTo(baseEntry.getStartDate())));
        assertThat(entry3Full.getEndDate(), is(equalTo(baseEntry.getEndDate())));
        assertThat(entry3Full.getSerializedResult(), is(equalTo(baseEntry.getSerializedResult())));
    }
    
    @Test
    public void testStatusFetches() {
        AsyncTaskExecutionEntry entry1Full = this.asyncTaskDao.getStatusEntry(baseEntry.getTaskId());
        assertThat(entry1Full.getTaskId(), is(equalTo(baseEntry.getTaskId())));
        assertThat(entry1Full.getExecutionStatus(), is(equalTo(baseEntry.getExecutionStatus())));
        assertThat(entry1Full.getScheduledDate(), is(equalTo(baseEntry.getScheduledDate())));
        assertThat(entry1Full.getStartDate(), is(nullValue()));
        assertThat(entry1Full.getEndDate(), is(nullValue()));
        assertThat(entry1Full.getTaskClassName(), is(nullValue()));
        assertThat(entry1Full.getSerializedParameters(), is(nullValue()));
        assertThat(entry1Full.getSerializedResult(), is(nullValue()));
        
        baseEntry.setExecutionStatus(AsyncTaskExecutionStatus.ONGOING);
        this.asyncTaskDao.updateStartStatus(baseEntry);
        
        AsyncTaskExecutionEntry entry2Full = this.asyncTaskDao.getStatusEntry(baseEntry.getTaskId());
        assertThat(entry2Full.getTaskId(), is(equalTo(baseEntry.getTaskId())));
        assertThat(entry2Full.getExecutionStatus(), is(equalTo(baseEntry.getExecutionStatus())));
        assertThat(entry2Full.getScheduledDate(), is(equalTo(baseEntry.getScheduledDate())));
        assertThat(entry2Full.getStartDate(), is(equalTo(baseEntry.getStartDate())));
        assertThat(entry2Full.getEndDate(), is(nullValue()));
        assertThat(entry2Full.getTaskClassName(), is(nullValue()));
        assertThat(entry2Full.getSerializedParameters(), is(nullValue()));
        assertThat(entry2Full.getSerializedResult(), is(nullValue()));
        
        baseEntry.setExecutionStatus(AsyncTaskExecutionStatus.COMPLETED);
        this.asyncTaskDao.updateEndStatus(baseEntry);
        
        AsyncTaskExecutionEntry entry3Full = this.asyncTaskDao.getStatusEntry(baseEntry.getTaskId());
        assertThat(entry3Full.getTaskId(), is(equalTo(baseEntry.getTaskId())));
        assertThat(entry3Full.getExecutionStatus(), is(equalTo(baseEntry.getExecutionStatus())));
        assertThat(entry3Full.getScheduledDate(), is(equalTo(baseEntry.getScheduledDate())));
        assertThat(entry3Full.getStartDate(), is(equalTo(baseEntry.getStartDate())));
        assertThat(entry3Full.getEndDate(), is(equalTo(baseEntry.getEndDate())));
        assertThat(entry3Full.getTaskClassName(), is(nullValue()));
        assertThat(entry3Full.getSerializedParameters(), is(nullValue()));
        assertThat(entry3Full.getSerializedResult(), is(nullValue()));
    }
    
    @Test
    public void testDeleteAsyncTaskEntry(){
        this.asyncTaskDao.delete(baseEntry);
        AsyncTaskExecutionEntry entry1Full = this.asyncTaskDao.getFullEntry(baseEntry.getTaskId());
        assertNull(entry1Full);
    }
    
    @Test
    public void testgetVocabularyRdfImportTaskTypeAndVocabularyName() {
        AsyncTaskExecutionEntry entry1 = new AsyncTaskExecutionEntry();
        entry1.setTaskId(UUID.randomUUID().toString());
        entry1.setScheduledDate(new Date());
        entry1.setTaskClassName(VocabularyRdfImportFromUrlTask.class.getCanonicalName());
        entry1.setSerializedParameters("{\"@class\":\"java.util.HashMap\",\"scheduleInterval\":7,\"rdfPurgeOption\":\"DONT_PURGE\",\"vocabularyIdentifier\":\"testIdentifier\",\"scheduleIntervalUnit\":\"days\"}");
        entry1.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry1.setStartDate(new Date(entry1.getScheduledDate().getTime() + 200));
        entry1.setEndDate(new Date(entry1.getStartDate().getTime() + 2000));
        this.asyncTaskDao.create(entry1);
        AsyncTaskExecutionEntry resultEntry = this.asyncTaskDao.getVocabularyRdfImportTaskEntryByVocabularyName("testIdentifier");
        assertThat(resultEntry.getTaskId(), is(equalTo(entry1.getTaskId())));
        assertThat(resultEntry.getTaskClassName(), is(equalTo(entry1.getTaskClassName())));
        assertThat(resultEntry.getExecutionStatus(), is(equalTo(entry1.getExecutionStatus())));
        assertThat(resultEntry.getScheduledDate(), is(equalTo(entry1.getScheduledDate())));
        assertThat(resultEntry.getSerializedParameters(), is(equalTo(entry1.getSerializedParameters())));
        this.testAssistanceDao.deleteEntry(entry1.getTaskId());
    }
    
    @Test
    public void testGetAllEntriesByTaskClassNames(){
          AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(UUID.randomUUID().toString());
        entry.setScheduledDate(new Date());
        entry.setTaskClassName("some.other.class.Name");
        entry.setSerializedParameters("{ param1: 1, param2: 2, param3: 3 }");
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setStartDate(new Date(entry.getScheduledDate().getTime() + 300));
        entry.setEndDate(new Date(entry.getStartDate().getTime() + 5000));
        entry.setSerializedResult("{ value: 42 }");
        this.asyncTaskDao.create(entry);
        Set<String> taskClassNames = new HashSet<String>();
        taskClassNames.add(this.baseEntry.getTaskClassName());
        List<AsyncTaskExecutionEntry> results = this.asyncTaskDao.getAllEntriesByTaskClassNames(taskClassNames);
        assertThat(results.size(), is(1));
       AsyncTaskExecutionEntry resultEntry = results.get(0);
       assertThat(resultEntry.getTaskId(), is(equalTo(this.baseEntry.getTaskId())));
        assertThat(resultEntry.getTaskClassName(), is(equalTo(this.baseEntry.getTaskClassName())));
        assertThat(resultEntry.getExecutionStatus(), is(equalTo(this.baseEntry.getExecutionStatus())));
        assertThat(resultEntry.getScheduledDate(), is(equalTo(this.baseEntry.getScheduledDate())));
        assertThat(resultEntry.getSerializedParameters(), is(equalTo(this.baseEntry.getSerializedParameters())));
    }

    private AsyncTaskExecutionEntry createBaseEntry() {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(UUID.randomUUID().toString());
        entry.setScheduledDate(new Date());
        entry.setTaskClassName("some.class.Name");
        entry.setSerializedParameters("{ param1: 1, param2: 2 }");
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setStartDate(new Date(entry.getScheduledDate().getTime() + 200));
        entry.setEndDate(new Date(entry.getStartDate().getTime() + 2000));
        entry.setSerializedResult("{ value: 5 }");
        
        return entry;
    }
    
    private static class TestAssistanceDao extends JdbcDaoBase {

        public TestAssistanceDao(DataSource dataSource) {
            super(dataSource);
        }
        
        public void deleteEntry(String taskId) {
            String sql = "delete from ASYNC_TASK_ENTRY where TASK_ID = :taskId";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("taskId", taskId);
            this.getNamedParameterJdbcTemplate().update(sql, params);
        }
        
    }
    
}
