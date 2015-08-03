package eionet.meta.controllers;

import eionet.util.CompoundDataObject;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.ServiceException;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface AttributeFixedValuesController {
    
    Attribute getOwnerAttribute(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException;
    
    CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException;
    
    CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException;
    
    void saveFixedValue(AppContextProvider contextProvider, String ownerAttributeId, FixedValue fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                   EmptyValueException, DuplicateResourceException, ServiceException;
    
    void deleteFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException;
    
    void deleteFixedValues(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException;
    
    public static final String PROPERTY_OWNER_ATTRIBUTE = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
}
