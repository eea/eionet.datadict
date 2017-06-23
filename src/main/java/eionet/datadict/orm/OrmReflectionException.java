package eionet.datadict.orm;

public class OrmReflectionException extends RuntimeException {

    public OrmReflectionException() {
    }

    public OrmReflectionException(String message) {
        super(message);
    }

    public OrmReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrmReflectionException(Throwable cause) {
        super(cause);
    }
    
}
