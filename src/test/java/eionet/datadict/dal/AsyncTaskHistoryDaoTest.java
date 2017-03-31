package eionet.datadict.dal;

import eionet.datadict.dal.impl.JdbcDaoBase;
import eionet.datadict.dal.impl.converters.DateTimeToLongConverter;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
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
    @SpringBeanByType
    private static DateTimeToLongConverter dateTimeToLongConverter;

    @SpringBeanByName
    private DataSource dataSource;
    private TestAssistanceDao testAssistanceDao;
    private AsyncTaskExecutionEntryHistory baseHistoryEntry;
    private static List<String> testHistoryEntriesTaskIds = new LinkedList<String>(Arrays.asList("1", "1", "2"));
    private static Date entriesBaseDate = new Date();

    @Before
    public void setup() {
        this.testAssistanceDao = new TestAssistanceDao(this.dataSource);
        this.baseHistoryEntry = this.createBaseHistoryEntry();
        this.asyncTaskHistoryDao.storeAsyncTaskEntry(baseHistoryEntry);
        for (String baseHistoryEntriesTaskId : testHistoryEntriesTaskIds) {
            this.asyncTaskHistoryDao.storeAsyncTaskEntry(this.createBaseHistoryEntry(baseHistoryEntriesTaskId));
            // We create basically 3 same entities with different task IDs
        }
    }

    @After
    public void tearDown() {
        this.testAssistanceDao.deleteAllHistoryEntries();

    }

   @Test
    public void testRetrieveTaskById() {
        AsyncTaskExecutionEntryHistory historyEntry = this.asyncTaskHistoryDao.retrieveTaskHistoryById(this.asyncTaskHistoryDao.retrieveTasksByTaskId(baseHistoryEntry.getTaskId()).get(0).getId().toString());
        assertThat(historyEntry.getId(), is(equalTo(this.asyncTaskHistoryDao.retrieveTasksByTaskId(baseHistoryEntry.getTaskId()).get(0).getId())));
        assertThat(historyEntry.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(historyEntry.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(historyEntry.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(historyEntry.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(historyEntry.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(historyEntry.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(historyEntry.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
    }

   @Test
    public void testStoreAsyncTaskEntry() {
        this.testAssistanceDao.deleteHistoryEntry(baseHistoryEntry.getTaskId());
        this.asyncTaskHistoryDao.storeAsyncTaskEntry(baseHistoryEntry);
        AsyncTaskExecutionEntryHistory newHistoryEntry = this.asyncTaskHistoryDao.retrieveTaskHistoryById(this.asyncTaskHistoryDao.retrieveTasksByTaskId(baseHistoryEntry.getTaskId()).get(0).getId().toString());
        assertThat(newHistoryEntry.getTaskId(), is(equalTo(baseHistoryEntry.getTaskId())));
        assertThat(newHistoryEntry.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(newHistoryEntry.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(newHistoryEntry.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(newHistoryEntry.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(newHistoryEntry.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(newHistoryEntry.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(newHistoryEntry.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
    }

   @Test
    public void testRetrieveTasksByTaskId() {
        List<AsyncTaskExecutionEntryHistory> historyEntries = this.asyncTaskHistoryDao.retrieveTasksByTaskId("1");
        assertEquals(historyEntries.size(), 2);
        AsyncTaskExecutionEntryHistory entry1 = historyEntries.get(0);
        assertThat(entry1.getTaskId(), is(equalTo("1")));
        assertThat(entry1.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(entry1.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(entry1.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(entry1.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(entry1.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(entry1.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(entry1.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
        AsyncTaskExecutionEntryHistory entry2 = historyEntries.get(1);
        assertThat(entry2.getTaskId(), is(equalTo("1")));
        assertThat(entry2.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(entry2.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(entry2.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(entry2.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(entry2.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(entry2.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(entry2.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
    }

   @Test
    public void testRetrieveAllTasksHistory() {
        List<AsyncTaskExecutionEntryHistory> historyEntries = this.asyncTaskHistoryDao.retrieveAllTasksHistory();
        assertEquals(4, historyEntries.size());
        //According to the test @Setup, the first entry to make it into the Database should be the baseHistoryEntry.
        AsyncTaskExecutionEntryHistory newHistoryEntry = historyEntries.get(0);
            assertThat(newHistoryEntry.getTaskId(), is(equalTo(baseHistoryEntry.getTaskId())));
        assertThat(newHistoryEntry.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(newHistoryEntry.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(newHistoryEntry.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(newHistoryEntry.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(newHistoryEntry.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(newHistoryEntry.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(newHistoryEntry.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
        AsyncTaskExecutionEntryHistory entry1 = historyEntries.get(1);
        assertThat(entry1.getTaskId(), is(equalTo("1")));
        assertThat(entry1.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(entry1.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(entry1.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(entry1.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(entry1.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(entry1.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(entry1.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
        AsyncTaskExecutionEntryHistory entry2 = historyEntries.get(2);
        assertThat(entry2.getTaskId(), is(equalTo("1")));
        assertThat(entry2.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(entry2.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(entry2.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(entry2.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(entry2.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(entry2.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(entry2.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
        AsyncTaskExecutionEntryHistory entry3 = historyEntries.get(3);
        assertThat(entry3.getTaskId(), is(equalTo("2")));
        assertThat(entry3.getTaskClassName(), is(equalTo(baseHistoryEntry.getTaskClassName())));
        assertThat(entry3.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(entry3.getScheduledDate(), is(equalTo(baseHistoryEntry.getScheduledDate())));
        assertThat(entry3.getSerializedParameters(), is(equalTo(baseHistoryEntry.getSerializedParameters())));
        assertThat(entry3.getStartDate(), is(equalTo(baseHistoryEntry.getStartDate())));
        assertThat(entry3.getEndDate(), is(equalTo(baseHistoryEntry.getEndDate())));
        assertThat(entry3.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
    }

    @Test
    public void testUpdateExecutionStatusAndSerializedResult() {
        this.baseHistoryEntry.setExecutionStatus(AsyncTaskExecutionStatus.COMPLETED);
        this.baseHistoryEntry.setSerializedResult("{ value: 42 }");
        this.asyncTaskHistoryDao.updateExecutionStatusAndSerializedResult(baseHistoryEntry);
        AsyncTaskExecutionEntryHistory updatedEntry = this.asyncTaskHistoryDao.retrieveTaskHistoryById(this.asyncTaskHistoryDao.retrieveTasksByTaskId(baseHistoryEntry.getTaskId()).get(0).getId().toString());
        assertThat(updatedEntry.getExecutionStatus(), is(equalTo(baseHistoryEntry.getExecutionStatus())));
        assertThat(updatedEntry.getSerializedResult(), is(equalTo(baseHistoryEntry.getSerializedResult())));
    }

   @Test
    public void testDeleteRecordsWithScheduledDateOlderThanASpecificDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date olderDate = cal.getTime();
        cal.add(Calendar.MONTH, -2);
        Date evenOlderDate = cal.getTime();
        this.testAssistanceDao.updateHistoryEntryScheduledDate("2", evenOlderDate);
        this.asyncTaskHistoryDao.deleteRecordsWithScheduledDateOlderThan(olderDate);
        List<AsyncTaskExecutionEntryHistory> leftOverHistoryEntries = this.asyncTaskHistoryDao.retrieveAllTasksHistory();
        assertEquals(leftOverHistoryEntries.size(), 3);
        List<AsyncTaskExecutionEntryHistory> sameTaskIdHistoryEntries = this.asyncTaskHistoryDao.retrieveTasksByTaskId("1");
        assertEquals(sameTaskIdHistoryEntries.size(), 2);
        List<AsyncTaskExecutionEntryHistory> clearedHistoryEntries = this.asyncTaskHistoryDao.retrieveTasksByTaskId("2");
        assertTrue(clearedHistoryEntries.isEmpty());
    }
    
   @Test
    public void testDeleteAsyncTaskHistoryEntry(){
       this.asyncTaskHistoryDao.storeAsyncTaskEntry(baseHistoryEntry);
       AsyncTaskExecutionEntryHistory hEntry = asyncTaskHistoryDao.retrieveTasksByTaskId(this.baseHistoryEntry.getTaskId()).get(0);
       asyncTaskHistoryDao.delete(hEntry.getId());
       AsyncTaskExecutionEntryHistory oldHEntry =  asyncTaskHistoryDao.retrieveTaskHistoryById(String.valueOf(hEntry.getId()));
        assertNull(oldHEntry);
    }

    private AsyncTaskExecutionEntryHistory createBaseHistoryEntry() {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId("42");
        entry.setScheduledDate(entriesBaseDate);
        entry.setTaskClassName("some.class.Name");
        entry.setSerializedParameters("{ param1: 1, param2: 2 }");
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setStartDate(new Date(entriesBaseDate.getTime() + 200));
        entry.setEndDate(new Date(entriesBaseDate.getTime() + 2000));
        entry.setSerializedResult("{ value: 5 }");
        AsyncTaskExecutionEntryHistory historyEntry = new AsyncTaskExecutionEntryHistory(entry);
        return historyEntry;
    }

    private AsyncTaskExecutionEntryHistory createBaseHistoryEntry(String taskId) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(taskId);
        entry.setScheduledDate(entriesBaseDate);
        entry.setTaskClassName("some.class.Name");
        entry.setSerializedParameters("{ param1: 1, param2: 2 }");
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setStartDate(new Date(entriesBaseDate.getTime() + 200));
        entry.setEndDate(new Date(entriesBaseDate.getTime() + 2000));
        entry.setSerializedResult("{ value: 5 }");
        AsyncTaskExecutionEntryHistory historyEntry = new AsyncTaskExecutionEntryHistory(entry);
        return historyEntry;
    }

    private static class TestAssistanceDao extends JdbcDaoBase {

        public TestAssistanceDao(DataSource dataSource) {
            super(dataSource);
        }

        public void deleteHistoryEntry(String taskId) {
            String sql = "delete from ASYNC_TASK_ENTRY_HISTORY where TASK_ID = :taskId";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("taskId", taskId);
            this.getNamedParameterJdbcTemplate().update(sql, params);
        }

        public void deleteAllHistoryEntries() {
            String sql = "truncate ASYNC_TASK_ENTRY_HISTORY ";
            Map<String, Object> params = new HashMap<String, Object>();
            this.getNamedParameterJdbcTemplate().update(sql, params);
        }
        public void updateHistoryEntryScheduledDate(String taskId, Date date) {
            String sql = "update ASYNC_TASK_ENTRY_HISTORY set  SCHEDULED_DATE = :scheduledDate  where TASK_ID = :taskId";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("taskId", taskId);
            params.put("scheduledDate", dateTimeToLongConverter.convert(date));
            this.getNamedParameterJdbcTemplate().update(sql, params);
        }
    }
}
