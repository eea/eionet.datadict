package eionet.meta.controllers;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import java.util.List;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface DataElementFixedValuesController {
    
    /**
     * Fetches info about an specific data element that accepts fixed values.
     * 
     * @param ownerDataElementId the data element id.
     * 
     * @return a {@link DataElement} containing the requested info.
     * 
     * @throws ResourceNotFoundException if the data element cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values.
     */
    DataElement getOwnerDataElement(int ownerDataElementId)
            throws ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches info about an specific data element that accepts fixed values. 
     * This method concerns edit-based scenarios, where the user must be authorized
     * to edit the requested resource.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id.
     * 
     * @return a {@link DataElement} containing the requested info.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the data element cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values or the data element is not in working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    DataElement getEditableOwnerDataElement(AppContextProvider contextProvider, int ownerDataElementId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException;
    
    /**
     * Fetches all the info required about a fixed value and its respective
     * owner. The results are packed into a {@link CompoundDataObject} instance,
     * given the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_DATA_ELEMENT}: the {@link DataElement} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUE}: the {@link FixedValue} instance</li>
     * </ul>
     *
     * @param ownerDataElementId the data element id.
     * @param fixedValueId the id of the requested value.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed value, and the owner data element.
     * 
     * @throws ResourceNotFoundException if the data element or the fixed value cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values.
     */
    CompoundDataObject getSingleValueModel(int ownerDataElementId, int fixedValueId)
            throws ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches all the info required about a fixed value and its respective
     * owner. The results are packed into a {@link CompoundDataObject} instance,
     * given the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_DATA_ELEMENT}: the {@link DataElement} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUE}: the {@link FixedValue} instance</li>
     * </ul>
     * This method concerns edit-based scenarios, where the user must be authorized
     * to edit the requested resource.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id.
     * @param fixedValueId the id of the requested value.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed value, and the owner data element.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the data element or the fixed value  cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values, or it is not in a working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    CompoundDataObject getEditableSingleValueModel(AppContextProvider contextProvider, int ownerDataElementId, int fixedValueId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException;
    
    /**
     * Fetches all the info required about the fixed values of a specific owner.
     * The results are packed into a {@link CompoundDataObject} instance, given 
     * the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_DATA_ELEMENT}: a {@link DataElement} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUES}: a {@link List} of {@link FixedValue} instances</li>
     * </ul>
     * 
     * @param ownerDataElementId the data element id.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed values, and the owner data element.
     * 
     * @throws ResourceNotFoundException if the data element cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values.
     */
    CompoundDataObject getAllValuesModel(int ownerDataElementId)
            throws ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches all the info required about the fixed values of a specific owner.
     * The results are packed into a {@link CompoundDataObject} instance, given 
     * the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_DATA_ELEMENT}: a {@link DataElement} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUES}: a {@link List} of {@link FixedValue} instances</li>
     * </ul>
     * This method concerns edit-based scenarios, where the user must be authorized
     * to edit the requested resource.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed values, and the owner data element.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the data element cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values, or it is not in a working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    CompoundDataObject getEditableAllValuesModel(AppContextProvider contextProvider, int ownerDataElementId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException;
    
    /**
     * Persists the modified data of the fixed value of a specified owner. 
     * Performs an update operation if the value exists; otherwise it will 
     * perform a create operation.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id.
     * @param fixedValue the modification payload.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the data element or the fixed value cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values, or it is not in a working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     * @throws EmptyParameterException if the value property of the payload is blank.
     * @throws DuplicateResourceException if the updated/created value already exists.
     */
    void saveFixedValue(AppContextProvider contextProvider, int ownerDataElementId, FixedValue fixedValue)
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, 
                   ConflictException, DuplicateResourceException, EmptyParameterException;
    
    /**
     * Deletes the requested fixed value of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id.
     * @param fixedValueId the id of the value to be deleted.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the data element or the fixed value cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values, or it is not in a working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    void deleteFixedValue(AppContextProvider contextProvider, int ownerDataElementId, int fixedValueId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException;
    
    /**
     * Deletes all fixed values of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerDataElementId the data element id.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the data element cannot be found.
     * @throws ConflictException if the data element cannot be associated to fixed values, or it is not in a working copy state.
     * @throws UserAuthorizationException if the user is not the owner of the working copy.
     */
    void deleteFixedValues(AppContextProvider contextProvider, int ownerDataElementId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException;
    
    public static final String PROPERTY_OWNER_DATA_ELEMENT = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
}
