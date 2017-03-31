package eionet.datadict.dal.impl;

import eionet.datadict.commons.sql.ResultSetUtils;
import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.dal.impl.converters.DateTimeToLongConverter;
import eionet.datadict.dal.impl.converters.ExecutionStatusToByteConverter;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Repository
public class AsyncTaskHistoryDaoImpl extends JdbcDaoBase implements AsyncTaskHistoryDao {

    private final ExecutionStatusToByteConverter executionStatusToByteConverter;
    private final DateTimeToLongConverter dateTimeToLongConverter;

    @Autowired
    public AsyncTaskHistoryDaoImpl(ExecutionStatusToByteConverter executionStatusToByteConverter, DateTimeToLongConverter dateTimeToLongConverter, DataSource dataSource) {
        super(dataSource);
        this.executionStatusToByteConverter = executionStatusToByteConverter;
        this.dateTimeToLongConverter = dateTimeToLongConverter;
    }

    @Override
    public AsyncTaskExecutionEntryHistory retrieveTaskHistoryById(String id) {
        String sql = "select * from ASYNC_TASK_ENTRY_HISTORY where ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        List<AsyncTaskExecutionEntryHistory> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new ResultEntryRowMapper());
        return IterableUtils.firstOrDefault(results);
    }

    @Override
    public void storeAsyncTaskEntry(AsyncTaskExecutionEntry entry) {
        String sql
                = "insert into ASYNC_TASK_ENTRY_HISTORY(TASK_ID, TASK_CLASS_NAME, EXECUTION_STATUS,START_DATE ,END_DATE, SCHEDULED_DATE, SERIALIZED_PARAMETERS, SERIALIZED_RESULT) "
                + "values (:taskId, :className, :executionStatus, :startDate, :endDate, :scheduledDate, :serializedParameters, :serializedResult)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("className", entry.getTaskClassName());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("startDate", this.dateTimeToLongConverter.convert(entry.getStartDate()));
        params.put("endDate", this.dateTimeToLongConverter.convert(entry.getEndDate()));
        params.put("scheduledDate", this.dateTimeToLongConverter.convert(entry.getScheduledDate()));
        params.put("serializedParameters", entry.getSerializedParameters());
        params.put("serializedResult",entry.getSerializedResult());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public List<AsyncTaskExecutionEntryHistory> retrieveTasksByTaskId(String taskId) {
        String sql = "select * from ASYNC_TASK_ENTRY_HISTORY where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        List<AsyncTaskExecutionEntryHistory> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new ResultEntryRowMapper());
        return results;
    }

    @Override
    public List<AsyncTaskExecutionEntryHistory> retrieveAllTasksHistory() {
        String sql = "select * from ASYNC_TASK_ENTRY_HISTORY";
        Map<String, Object> params = new HashMap<String, Object>();
        List<AsyncTaskExecutionEntryHistory> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new AsyncTaskHistoryDaoImpl.ResultEntryRowMapper());
        return results;
    }

    @Override
    public void deleteRecordsWithScheduledDateOlderThan(Date date) {
        String sql = "DELETE  FROM ASYNC_TASK_ENTRY_HISTORY  WHERE FROM_UNIXTIME(SCHEDULED_DATE/1000) < FROM_UNIXTIME(:time/1000)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("time", date.getTime());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void updateExecutionStatusAndSerializedResult(AsyncTaskExecutionEntry entry) {
          String sql = 
            "update ASYNC_TASK_ENTRY_HISTORY set  EXECUTION_STATUS = :executionStatus, SERIALIZED_RESULT = :serializedResult where TASK_ID = :taskId and START_DATE = :startDate";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("startDate", this.dateTimeToLongConverter.convert(entry.getStartDate()));
        params.put("serializedResult", entry.getSerializedResult());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from ASYNC_TASK_ENTRY_HISTORY where ID = :id";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id);
        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);  
    }

    protected class ResultEntryRowMapper implements RowMapper<AsyncTaskExecutionEntryHistory> {

        @Override
        public AsyncTaskExecutionEntryHistory mapRow(ResultSet rs, int i) throws SQLException {
            AsyncTaskExecutionEntry asyncTaskEntry = new AsyncTaskExecutionEntry();
            asyncTaskEntry.setTaskId(rs.getString("TASK_ID"));
            asyncTaskEntry.setTaskClassName(rs.getString("TASK_CLASS_NAME"));
            asyncTaskEntry.setExecutionStatus(executionStatusToByteConverter.convertBack(rs.getByte("EXECUTION_STATUS")));
            asyncTaskEntry.setScheduledDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "SCHEDULED_DATE")));
            asyncTaskEntry.setStartDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "START_DATE")));
            asyncTaskEntry.setEndDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "END_DATE")));
            asyncTaskEntry.setSerializedParameters(rs.getString("SERIALIZED_PARAMETERS"));
            asyncTaskEntry.setSerializedResult(rs.getString("SERIALIZED_RESULT"));
            AsyncTaskExecutionEntryHistory entryHistory = new AsyncTaskExecutionEntryHistory(asyncTaskEntry);
            entryHistory.setId(rs.getLong("ID"));
            return entryHistory;
        }
    }
}
