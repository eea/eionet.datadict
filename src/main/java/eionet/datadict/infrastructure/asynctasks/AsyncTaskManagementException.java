package eionet.datadict.infrastructure.asynctasks;

public class AsyncTaskManagementException extends RuntimeException {

    public AsyncTaskManagementException() {
    }

    public AsyncTaskManagementException(String message) {
        super(message);
    }

    public AsyncTaskManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncTaskManagementException(Throwable cause) {
        super(cause);
    }
    
}
