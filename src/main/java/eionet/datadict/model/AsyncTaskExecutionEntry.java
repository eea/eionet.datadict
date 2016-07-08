package eionet.datadict.model;

import java.util.Date;

public class AsyncTaskExecutionEntry {
    
    private String taskId;
    private String taskClassName;
    private AsyncTaskExecutionStatus executionStatus;
    private Date startDate;
    private Date endDate;
    private String serializedParameters;
    private String serializedResult;
    
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskClassName() {
        return taskClassName;
    }

    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }

    public AsyncTaskExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(AsyncTaskExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSerializedParameters() {
        return serializedParameters;
    }

    public void setSerializedParameters(String serializedParameters) {
        this.serializedParameters = serializedParameters;
    }

    public String getSerializedResult() {
        return serializedResult;
    }

    public void setSerializedResult(String serializedResult) {
        this.serializedResult = serializedResult;
    }
    
}
