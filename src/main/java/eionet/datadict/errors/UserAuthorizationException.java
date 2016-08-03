package eionet.datadict.errors;

/**
 * Thrown to indicate that the current user is not authorized to perform a 
 * specific action related to some resource.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class UserAuthorizationException extends BadRequestException {

    public UserAuthorizationException() {
    }

    public UserAuthorizationException(String message) {
        super(message);
    }

    public UserAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAuthorizationException(Throwable cause) {
        super(cause);
    }
    
}
