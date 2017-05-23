package eionet.datadict.model;

import java.util.Date;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class AsyncTaskExecutionEntryHistory extends AsyncTaskExecutionEntry{
    private Long id;

    public AsyncTaskExecutionEntryHistory(AsyncTaskExecutionEntry asyncTaskEntry) {
        super(asyncTaskEntry.getTaskId(), asyncTaskEntry.getTaskClassName(), asyncTaskEntry.getExecutionStatus(), asyncTaskEntry.getScheduledDate(), asyncTaskEntry.getStartDate(), asyncTaskEntry.getEndDate(), 
                asyncTaskEntry.getSerializedParameters(), asyncTaskEntry.getSerializedResult());
       
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
