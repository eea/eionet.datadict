package eionet.datadict.infrastructure.asynctasks.impl;

public class AsyncTaskResultSerializerException extends RuntimeException {
    
    public AsyncTaskResultSerializerException() {
    }

    public AsyncTaskResultSerializerException(String message) {
        super(message);
    }

    public AsyncTaskResultSerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncTaskResultSerializerException(Throwable cause) {
        super(cause);
    }
    
}
