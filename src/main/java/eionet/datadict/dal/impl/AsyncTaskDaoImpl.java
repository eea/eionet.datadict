package eionet.datadict.dal.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.impl.converters.DateTimeToLongConverter;
import eionet.datadict.dal.impl.converters.ExecutionStatusToByteConverter;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.commons.sql.ResultSetUtils;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("asyncTaskDao")
public class AsyncTaskDaoImpl extends JdbcDaoBase implements AsyncTaskDao {

    private final ExecutionStatusToByteConverter executionStatusToByteConverter;
    private final DateTimeToLongConverter dateTimeToLongConverter;
    
    @Autowired
    public AsyncTaskDaoImpl(DataSource dataSource, 
            ExecutionStatusToByteConverter executionStatusToByteConverter,
            DateTimeToLongConverter dateTimeToLongConverter) {
        super(dataSource);
        this.executionStatusToByteConverter = executionStatusToByteConverter;
        this.dateTimeToLongConverter = dateTimeToLongConverter;
    }

    @Override
    public AsyncTaskExecutionEntry getStatusEntry(String taskId) {
        String sql = "select TASK_ID, EXECUTION_STATUS, SCHEDULED_DATE, START_DATE, END_DATE from ASYNC_TASK_ENTRY where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params, 
                new StatusEntryRowMapper());
        
        return IterableUtils.firstOrDefault(results);
    }

    @Override
    public AsyncTaskExecutionEntry getFullEntry(String taskId) {
        String sql = "select * from ASYNC_TASK_ENTRY where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params, 
                new ResultEntryRowMapper());
        
        return IterableUtils.firstOrDefault(results);
    }
    
    @Override
    public void create(AsyncTaskExecutionEntry entry) {
        String sql = 
            "insert into ASYNC_TASK_ENTRY(TASK_ID, TASK_CLASS_NAME, EXECUTION_STATUS, SCHEDULED_DATE, SERIALIZED_PARAMETERS) " + 
            "values (:taskId, :className, :executionStatus, :scheduledDate, :serializedParameters)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("className", entry.getTaskClassName());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("scheduledDate", this.dateTimeToLongConverter.convert(entry.getScheduledDate()));
        params.put("serializedParameters", entry.getSerializedParameters());
        this.getNamedParameterJdbcTemplate().update(sql, params);
        
    }

    @Override
    public void updateStartStatus(AsyncTaskExecutionEntry entry) {
        String sql = 
            "update ASYNC_TASK_ENTRY set START_DATE = :startDate, EXECUTION_STATUS = :executionStatus where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("startDate", this.dateTimeToLongConverter.convert(entry.getStartDate()));
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }
    
    @Override
    public AsyncTaskExecutionEntry updateEndStatus(AsyncTaskExecutionEntry entry) {
        String sql = 
            "update ASYNC_TASK_ENTRY set END_DATE = :endDate, EXECUTION_STATUS = :executionStatus, SERIALIZED_RESULT = :serializedResult where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("endDate", this.dateTimeToLongConverter.convert(entry.getEndDate()));
        params.put("serializedResult", entry.getSerializedResult());
        this.getNamedParameterJdbcTemplate().update(sql, params);
        return this.getFullEntry(entry.getTaskId());
    }
    
    @Override
    public List<AsyncTaskExecutionEntry> getAllEntries() {
          String sql = "select * from ASYNC_TASK_ENTRY";
        Map<String, Object> params = new HashMap<String, Object>();
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params, 
                new ResultEntryRowMapper());
        
        return results;
    }

    @Override
    public AsyncTaskExecutionEntry updateScheduledDate(AsyncTaskExecutionEntry entry) {
        String sql = 
            "update ASYNC_TASK_ENTRY set SCHEDULED_DATE = :scheduledDate where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
                params.put("taskId", entry.getTaskId());
        params.put("scheduledDate", this.dateTimeToLongConverter.convert(entry.getScheduledDate()));
        this.getNamedParameterJdbcTemplate().update(sql, params);
        return this.getFullEntry(entry.getTaskId());
    }

    @Override
    public void delete(AsyncTaskExecutionEntry entry) {
        String sql = "delete from ASYNC_TASK_ENTRY where TASK_ID = :taskId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("taskId", entry.getTaskId());
        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    @Override
    public void updateTaskParameters(AsyncTaskExecutionEntry entry) {
        String sql
                = "update ASYNC_TASK_ENTRY set  SERIALIZED_PARAMETERS = :serializedParameters where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("serializedParameters", entry.getSerializedParameters());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public AsyncTaskExecutionEntry getVocabularyRdfImportTaskTypeAndVocabularyName(String vocabularyIdentifier) {
        String sql
                = "select * from   ASYNC_TASK_ENTRY where TASK_CLASS_NAME = :taskClassName AND SERIALIZED_PARAMETERS like  :likeQuery";
        String vocabularyIdentifierLikeQuery = "%\"" + VocabularyRdfImportFromUrlTask.PARAM_VOCABULARY_IDENTIFIER + "\":\"" + vocabularyIdentifier + "\"%";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskClassName", VocabularyRdfImportFromUrlTask.class.getCanonicalName());
        params.put("likeQuery", vocabularyIdentifierLikeQuery);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params,
                new ResultEntryRowMapper());

        return IterableUtils.firstOrDefault(results);
    }

    protected class StatusEntryRowMapper implements RowMapper<AsyncTaskExecutionEntry> {
        
        @Override
        public AsyncTaskExecutionEntry mapRow(ResultSet rs, int i) throws SQLException {
            AsyncTaskExecutionEntry result = new AsyncTaskExecutionEntry();
            result.setTaskId(rs.getString("TASK_ID"));
            result.setExecutionStatus(executionStatusToByteConverter.convertBack(rs.getByte("EXECUTION_STATUS")));
            result.setScheduledDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "SCHEDULED_DATE")));
            result.setStartDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "START_DATE")));
            result.setEndDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "END_DATE")));
            
            return result;
        }
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
