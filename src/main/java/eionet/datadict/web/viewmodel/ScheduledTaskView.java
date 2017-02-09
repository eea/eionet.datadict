package eionet.datadict.web.viewmodel;

import eionet.datadict.model.AsyncTaskExecutionEntry;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class ScheduledTaskView {

    private String type;
    private AsyncTaskExecutionEntry details;
    private String additionalDetails;

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
    
    
}
