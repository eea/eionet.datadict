package eionet.meta.application.errors;

/**
 * Thrown to indicate that a requested resource was not found.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class ResourceNotFoundException extends Exception {

    private final Object resourceId;
    
    public ResourceNotFoundException(Object resourceId) {
        super();
        this.resourceId = resourceId;
    }

    public Object getResourceId() {
        return this.resourceId;
    }
    
}
