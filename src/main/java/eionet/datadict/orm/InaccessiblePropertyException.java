package eionet.datadict.orm;

public class InaccessiblePropertyException extends OrmReflectionException {

    public InaccessiblePropertyException() {
    }

    public InaccessiblePropertyException(String message) {
        super(message);
    }

    public InaccessiblePropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InaccessiblePropertyException(Throwable cause) {
        super(cause);
    }
    
}
