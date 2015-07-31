package eionet.meta.controllers;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.ServiceException;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ElementFixedValuesController {
    
    DataElement getOwnerDataElement(String ownerDataElementId, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException;
    
    CompoundDataObject getSingleValueModel(String ownerDataElementId, String fixedValue, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException;
    
    CompoundDataObject getAllValuesModel(String ownerDataElementId, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException;
    
    void saveFixedValue(String ownerDataElementId, FixedValue fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException, 
                   DuplicateResourceException, EmptyValueException, ServiceException;
    
    void deleteFixedValue(String ownerDataElementId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException;
    
    void deleteFixedValues(String ownerDataElementId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException;
    
    public static final String PROPERTY_OWNER_DATA_ELEMENT = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
    public static class FixedValueOwnerNotEditableException extends Exception {
        
    }
    
}
