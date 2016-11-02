package eionet.datadict.infrastructure.asynctasks;

import org.apache.commons.lang.StringUtils;

public class AsyncTaskExecutionError {

    private String message;
    private String technicalDetails;

    public AsyncTaskExecutionError() { }
    
    public AsyncTaskExecutionError(String message, String technicalDetails) {
        this.message = message;
        this.technicalDetails = technicalDetails;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTechnicalDetails() {
        return technicalDetails;
    }

    public void setTechnicalDetails(String technicalDetails) {
        this.technicalDetails = technicalDetails;
    }
    
}
