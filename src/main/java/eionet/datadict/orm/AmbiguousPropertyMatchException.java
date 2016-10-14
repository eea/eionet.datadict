package eionet.datadict.orm;

public class AmbiguousPropertyMatchException extends OrmReflectionException {

    public AmbiguousPropertyMatchException() {
    }

    public AmbiguousPropertyMatchException(String message) {
        super(message);
    }

    public AmbiguousPropertyMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousPropertyMatchException(Throwable cause) {
        super(cause);
    }
    
}
