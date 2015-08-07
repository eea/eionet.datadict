package eionet.meta.application.errors.fixedvalues;

import eionet.meta.dao.domain.FixedValue;

/**
 * Thrown to indicate an attempt to create/modify a {@link FixedValue} by 
 * providing an empty value property.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class EmptyValueException extends Exception {
    
}
