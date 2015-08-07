package eionet.meta.application.errors.fixedvalues;

import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.FixedValue;

/**
 * Thrown to indicate that the owner of a {@link FixedValue} was not found.
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
