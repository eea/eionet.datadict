package eionet.datadict.errors;

import eionet.datadict.resources.ResourceIdInfo;
import eionet.datadict.resources.ResourceType;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class ResourceNotFoundException extends Exception {

    private final ResourceType resourceType;
    private final ResourceIdInfo resourceIdInfo;

    public ResourceNotFoundException(ResourceType resourceType, ResourceIdInfo resourceIdInfo) {
        this.resourceType = resourceType;
        this.resourceIdInfo = resourceIdInfo;
    }

    public ResourceNotFoundException(ResourceType resourceType, ResourceIdInfo resourceIdInfo, String message) {
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
