package eionet.meta.application.errors;

/**
 * Thrown to indicate parsing failure of an entity's id.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class MalformedIdentifierException extends Exception {

    private final String identifier;
    
    public MalformedIdentifierException(String identifier) {
        this(identifier, null, null);
    }

    public MalformedIdentifierException(String identifier, String message) {
        this(identifier, message, null);
    }

    public MalformedIdentifierException(String identifier, String message, Throwable cause) {
        super(message, cause);
        this.identifier = identifier;
    }

    public MalformedIdentifierException(String identifier, Throwable cause) {
        this(identifier, null, cause);
    }    
    
    public String getId() {
        return this.identifier;
    }
}
