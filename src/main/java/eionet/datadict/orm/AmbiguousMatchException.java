package eionet.datadict.orm;

public class AmbiguousMatchException extends OrmReflectionException {

    public AmbiguousMatchException() {
    }

    public AmbiguousMatchException(String message) {
        super(message);
    }

    public AmbiguousMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousMatchException(Throwable cause) {
        super(cause);
    }
    
}
