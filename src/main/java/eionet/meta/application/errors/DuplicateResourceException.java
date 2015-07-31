package eionet.meta.application.errors;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DuplicateResourceException extends Exception {
    
    private final Object resourceId;
    
    public DuplicateResourceException(Object resourceId) {
        super();
        this.resourceId = resourceId;
    }

    public Object getResourceId() {
        return this.resourceId;
    }
}
