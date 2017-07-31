package eionet.datadict.orm;

public class InvalidRelationException extends OrmReflectionException {

    public InvalidRelationException() {
    }

    public InvalidRelationException(String message) {
        super(message);
    }

    public InvalidRelationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRelationException(Throwable cause) {
        super(cause);
    }
    
}
