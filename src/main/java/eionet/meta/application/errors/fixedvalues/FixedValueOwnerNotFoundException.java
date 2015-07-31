package eionet.meta.application.errors.fixedvalues;

import eionet.meta.application.errors.ResourceNotFoundException;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public final class FixedValueOwnerNotFoundException extends ResourceNotFoundException {
    
    public FixedValueOwnerNotFoundException(int ownerId) {
        super(ownerId);
    }

    public int getOwnerId() {
        return (Integer) super.getResourceId();
    }

}
