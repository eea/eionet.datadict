package eionet.meta.application.errors;

/**
 * Thrown to indicate that an attempted action requires user login.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class UserAuthenticationException extends Exception {

    public UserAuthenticationException() {
        super();
    }
    
    public UserAuthenticationException(String message) {
        super(message);
    }
    
}
