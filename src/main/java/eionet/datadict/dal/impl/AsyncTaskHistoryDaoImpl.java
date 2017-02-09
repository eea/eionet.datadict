/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.dal.impl;

import eionet.datadict.commons.sql.ResultSetUtils;
import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.dal.impl.converters.DateTimeToLongConverter;
import eionet.datadict.dal.impl.converters.ExecutionStatusToByteConverter;
import eionet.datadict.model.AsyncTaskExecutionEntry;
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
    public AsyncTaskExecutionEntry retrieveTaskById(String id) {
        String sql = "select * from ASYNC_TASK_ENTRY_HISTORY where ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new ResultEntryRowMapper());

        return IterableUtils.firstOrDefault(results);
    }

    @Override
    public void store(AsyncTaskExecutionEntry entry) {
        String sql
                = "insert into ASYNC_TASK_ENTRY_HISTORY(TASK_ID, TASK_CLASS_NAME, EXECUTION_STATUS,START_DATE ,END_DATE, SCHEDULED_DATE, SERIALIZED_PARAMETERS) "
                + "values (:taskId, :className, :executionStatus, :startDate, :endDate, :scheduledDate, :serializedParameters)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("className", entry.getTaskClassName());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("startDate", this.dateTimeToLongConverter.convert(entry.getStartDate()));
        params.put("endDate", this.dateTimeToLongConverter.convert(entry.getEndDate()));
        params.put("scheduledDate", this.dateTimeToLongConverter.convert(entry.getScheduledDate()));
        params.put("serializedParameters", entry.getSerializedParameters());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public List<AsyncTaskExecutionEntry> retrieveTasksByTaskId(String taskId) {
        String sql = "select * from ASYNC_TASK_ENTRY_HISTORY where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new ResultEntryRowMapper());
        return results;
    }

    @Override
    public List<AsyncTaskExecutionEntry> retrieveAllTasksHistory() {
        String sql = "select * from ASYNC_TASK_ENTRY_HISTORY";
        Map<String, Object> params = new HashMap<String, Object>();
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new AsyncTaskHistoryDaoImpl.ResultEntryRowMapper());

        return results;
    }

    @Override
    public void deleteRecordsOlderThan(Date date) {
        String sql = "DELETE  FROM ASYNC_TASK_ENTRY_HISTORY  WHERE FROM_UNIXTIME(SCHEDULED_DATE/1000) < FROM_UNIXTIME(:time/1000)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("time", date.getTime());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    protected class ResultEntryRowMapper implements RowMapper<AsyncTaskExecutionEntry> {

        @Override
        public AsyncTaskExecutionEntry mapRow(ResultSet rs, int i) throws SQLException {
            AsyncTaskExecutionEntry result = new AsyncTaskExecutionEntry();
            result.setTaskId(rs.getString("TASK_ID"));
            result.setTaskClassName(rs.getString("TASK_CLASS_NAME"));
            result.setExecutionStatus(executionStatusToByteConverter.convertBack(rs.getByte("EXECUTION_STATUS")));
            result.setScheduledDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "SCHEDULED_DATE")));
            result.setStartDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "START_DATE")));
            result.setEndDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "END_DATE")));
            result.setSerializedParameters(rs.getString("SERIALIZED_PARAMETERS"));
            result.setSerializedResult(rs.getString("SERIALIZED_RESULT"));
            return result;
        }

    }
}
