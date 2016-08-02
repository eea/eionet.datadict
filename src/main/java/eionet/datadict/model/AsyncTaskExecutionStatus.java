package eionet.datadict.model;

public enum AsyncTaskExecutionStatus {

    SCHEDULED,
    ONGOING,
    COMPLETED,
    ABORTED,
    FAILED,
    KILLED;
    
    public static boolean isPending(AsyncTaskExecutionStatus status) {
        return status == SCHEDULED || status == ONGOING;
    }
    
}
