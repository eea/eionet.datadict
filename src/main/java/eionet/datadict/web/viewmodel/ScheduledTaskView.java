package eionet.datadict.web.viewmodel;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import java.util.Map;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class ScheduledTaskView {

    private String type;
    private Long asyncTaskExecutionEntryHistoryId;
    private AsyncTaskExecutionEntry details;
    private String additionalDetails;
    private Map<String,Object> taskParameters;
    private Object taskResult;
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AsyncTaskExecutionEntry getDetails() {
        return details;
    }

    public void setDetails(AsyncTaskExecutionEntry details) {
        this.details = details;
    }

    public String getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(String additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public Map<String, Object> getTaskParameters() {
        return taskParameters;
    }

    public void setTaskParameters(Map<String, Object> taskParameters) {
        this.taskParameters = taskParameters;
    }

    public Object getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(Object taskResult) {
        this.taskResult = taskResult;
    }

    public Long getAsyncTaskExecutionEntryHistoryId() {
        return asyncTaskExecutionEntryHistoryId;
    }

    public void setAsyncTaskExecutionEntryHistoryId(Long asyncTaskExecutionEntryHistoryId) {
        this.asyncTaskExecutionEntryHistoryId = asyncTaskExecutionEntryHistoryId;
    }

    
}
