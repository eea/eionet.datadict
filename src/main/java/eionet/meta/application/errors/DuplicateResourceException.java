package eionet.meta.application.errors;

/**
 * An {@link Exception} type that indicates a failed attempt to create a entity 
 * that already exists.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DuplicateResourceException extends Exception {
    
    private final Object resourceId;
    
    /**
     * Creates a new {@link DuplicateResourceException} instance.
     * 
     * @param resourceId The id/value that identifies the duplicate resource.
     */
    public DuplicateResourceException(Object resourceId) {
        super();
        this.resourceId = resourceId;
    }

    public Object getResourceId() {
        return this.resourceId;
    }
}
