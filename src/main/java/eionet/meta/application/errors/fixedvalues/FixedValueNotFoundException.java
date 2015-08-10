/*
 */
package eionet.meta.application.errors.fixedvalues;

import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.FixedValue;

/**
 * Thrown to indicate that a requested {@link FixedValue} was not found.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public final class FixedValueNotFoundException extends ResourceNotFoundException {

    public FixedValueNotFoundException(String value) {
        super(value);
    }

    public String getFixedValue() {
        return (String) super.getResourceId();
    }

}
