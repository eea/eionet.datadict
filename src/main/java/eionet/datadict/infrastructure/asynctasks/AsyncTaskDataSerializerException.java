package eionet.datadict.infrastructure.asynctasks;

public class AsyncTaskDataSerializerException extends RuntimeException {

    public AsyncTaskDataSerializerException() {
    }

    public AsyncTaskDataSerializerException(String message) {
        super(message);
    }

    public AsyncTaskDataSerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncTaskDataSerializerException(Throwable cause) {
        super(cause);
    }
    
}
