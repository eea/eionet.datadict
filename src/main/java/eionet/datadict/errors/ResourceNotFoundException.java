package eionet.datadict.errors;

public class ResourceNotFoundException extends BadRequestException {

    public ResourceNotFoundException() {
        this("The requested resource was not found");
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }
    
}
