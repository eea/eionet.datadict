package eionet.datadict.errors;

/**
 * Thrown to indicate an attempt to edit an entity that cannot be modified unless
 * in working copy state.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class NotAWorkingCopyException extends ConflictException {

    public NotAWorkingCopyException() {
    }

    public NotAWorkingCopyException(String message) {
        super(message);
    }

    public NotAWorkingCopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAWorkingCopyException(Throwable cause) {
        super(cause);
    }
        
}
