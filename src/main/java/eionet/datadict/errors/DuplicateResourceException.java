package eionet.datadict.errors;

public class DuplicateResourceException extends ConflictException {

    public DuplicateResourceException() {
    }

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateResourceException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return String.format("%s with %s already exists", this.resourceType.toString(), this.resourceIdInfo.getIdDescription());
    }
    
}
