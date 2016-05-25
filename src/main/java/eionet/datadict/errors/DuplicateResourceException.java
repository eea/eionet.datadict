package eionet.datadict.errors;

import eionet.datadict.resources.ResourceIdInfo;
import eionet.datadict.resources.ResourceType;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DuplicateResourceException extends Exception {

    private final ResourceType resourceType;
    private final ResourceIdInfo resourceIdInfo;
    
    public DuplicateResourceException(ResourceType resourceType, ResourceIdInfo resourceIdInfo) {
        this(resourceType, resourceIdInfo, null);
    }

    public DuplicateResourceException(ResourceType resourceType, ResourceIdInfo resourceIdInfo ,String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceIdInfo = resourceIdInfo;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public ResourceIdInfo getResourceIdInfo() {
        return resourceIdInfo;
    }
    
}
