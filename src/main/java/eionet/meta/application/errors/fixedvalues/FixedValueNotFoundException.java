/*
 */
package eionet.meta.application.errors.fixedvalues;

import eionet.meta.application.errors.ResourceNotFoundException;

/**
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
