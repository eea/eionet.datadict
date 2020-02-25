package eionet.datadict.errors;

public class AclAccessControllerInitializationException extends Exception {

    public AclAccessControllerInitializationException() {
    }

    public AclAccessControllerInitializationException(String message) {
        super(message);
    }


    public AclAccessControllerInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}