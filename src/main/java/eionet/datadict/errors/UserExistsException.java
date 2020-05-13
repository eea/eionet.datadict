package eionet.datadict.errors;

public class UserExistsException extends Exception {

    public UserExistsException() {
    }

    public UserExistsException(String message) {
        super(message);
    }


    public UserExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}