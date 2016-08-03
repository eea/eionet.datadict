package eionet.datadict.errors;

/**
 * Thrown to indicate that an attempted action requires user login.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class UserAuthenticationException extends BadRequestException {

    public UserAuthenticationException() {
        super();
    }
    
    public UserAuthenticationException(String message) {
        super(message);
    }

    public UserAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAuthenticationException(Throwable cause) {
        super(cause);
    }
    
}
