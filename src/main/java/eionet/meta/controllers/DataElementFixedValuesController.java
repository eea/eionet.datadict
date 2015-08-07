package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface DataElementFixedValuesController {
    
    /**
     * Fetches info about an specific data element that accepts fixed values.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id in {@link String} form.
     * @param isEditRequest a flag indicating whether the source operation concerns
     * editing, or viewing only.
     * 
     * @return a {@link DataElement} containing the requested info.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the data element id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the data element cannot be found.
     * @throws NotAFixedValueOwnerException if the data element cannot be associated to fixed values.
     * @throws FixedValueOwnerNotEditableException if the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    DataElement getOwnerDataElement(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    /**
     * Fetches all the info required about a fixed value and its respective
     * owner. The results are packed into a {@link CompoundDataObject} instance,
     * given the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_DATA_ELEMENT}: the {@link DataElement} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUE}: the {@link FixedValue} instance</li>
     * </ul>
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id in {@link String} format.
     * @param fixedValue the requested value.
     * @param isEditRequest a flag indicating whether the source operation concerns
     * editing, or viewing only.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed value, and the owner data element.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the data element id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the data element cannot be found.
     * @throws FixedValueNotFoundException if the fixed value cannot be found.
     * @throws NotAFixedValueOwnerException if the data element cannot be associated to fixed values.
     * @throws FixedValueOwnerNotEditableException if the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    /**
     * Fetches all the info required about the fixed values of a specific owner.
     * The results are packed into a {@link CompoundDataObject} instance, given 
     * the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_DATA_ELEMENT}: a {@link DataElement} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUES}: a {@link List} of {@link FixedValue} instances</li>
     * </ul>
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id in {@link String} format.
     * @param isEditRequest a flag indicating whether the source operation concerns
     * editing, or viewing only.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed values, and the owner data element.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the data element id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the data element cannot be found.
     * @throws NotAFixedValueOwnerException if the data element cannot be associated to fixed values.
     * @throws FixedValueOwnerNotEditableException if the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    /**
     * Persists the modified data of the fixed value of a specified owner. 
     * Performs an update operation if the value exists; otherwise it will 
     * perform a create operation.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id in {@link String} format.
     * @param originalValue the value of the {@link FixedValue} instance before the modification.
     * @param fixedValue the modification payload.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the data element id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the data element cannot be found.
     * @throws FixedValueNotFoundException if the fixed value cannot be found.
     * @throws NotAFixedValueOwnerException if the data element cannot be associated to fixed values.
     * @throws FixedValueOwnerNotEditableException if the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     * @throws EmptyValueException if the value property of the payload is blank.
     * @throws DuplicateResourceException if the updated/created value already exists.
     */
    void saveFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String originalValue, FixedValue fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException, 
                   NotAFixedValueOwnerException, DuplicateResourceException, EmptyValueException;
    
    /**
     * Deletes the requested fixed value of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id in {@link String} format.
     * @param fixedValue the value to be deleted.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the data element id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the data element cannot be found.
     * @throws NotAFixedValueOwnerException if the data element cannot be associated to fixed values.
     * @throws FixedValueNotFoundException if the fixed value cannot be found.
     * @throws FixedValueOwnerNotEditableException if the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    void deleteFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   NotAFixedValueOwnerException, FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    /**
     * Deletes all fixed values of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id in {@link String} format.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the data element id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the data element cannot be found.
     * @throws NotAFixedValueOwnerException if the data element cannot be associated to fixed values.
     * @throws FixedValueOwnerNotEditableException if the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    void deleteFixedValues(AppContextProvider contextProvider, String ownerDataElementId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    public static final String PROPERTY_OWNER_DATA_ELEMENT = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
    /**
     * Thrown to indicate that the current user cannot edit the requested fixed value.
     * Typically this will occur if the fixed value owner is not in editable/working state.
     */
    public static class FixedValueOwnerNotEditableException extends Exception {

        public FixedValueOwnerNotEditableException() { }
        
        public FixedValueOwnerNotEditableException(Throwable cause) {
            super(cause);
        }
        
    }
    
}
