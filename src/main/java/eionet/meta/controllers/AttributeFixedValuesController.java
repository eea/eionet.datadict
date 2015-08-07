package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface AttributeFixedValuesController {
    
    SimpleAttribute getOwnerAttribute(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException;
    
    CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, FixedValueNotFoundException;
    
    CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException;
    
    void saveFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String originalValue, FixedValue fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, FixedValueNotFoundException, 
                   EmptyValueException, DuplicateResourceException;
    
    void deleteFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, FixedValueNotFoundException;
    
    void deleteFixedValues(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException;
    
    public static final String PROPERTY_OWNER_ATTRIBUTE = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
}
