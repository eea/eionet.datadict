package eionet.datadict.errors;

public class BadFormatException extends BadRequestException {

    public BadFormatException() { }

    public BadFormatException(String message) {
        super(message);
    }

    public BadFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadFormatException(Throwable cause) {
        super(cause);
    }
    
}
