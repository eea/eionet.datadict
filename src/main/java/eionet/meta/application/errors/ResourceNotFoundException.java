package eionet.meta.application.errors;

/**
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
