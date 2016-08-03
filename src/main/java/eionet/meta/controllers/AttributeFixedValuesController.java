package eionet.meta.controllers;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import java.util.List;

/**
 * Controller containing business logic related to viewing/editing fixed values
 * for simple attributes.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface AttributeFixedValuesController {
   
    /**
     * Fetches info about an specific simple attribute that accepts fixed values.
     * 
     * @param ownerAttributeId the attribute id.
     * 
     * @return a {@link SimpleAttribute} containing the requested info.
     * 
     * @throws ResourceNotFoundException if the attribute cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    SimpleAttribute getOwnerAttribute(int ownerAttributeId)
            throws ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches info about an specific simple attribute that accepts fixed values.
     * This method concerns edit-based scenarios, where the user must be authorized
     * to edit the requested resource.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id.
     * 
     * @return a {@link SimpleAttribute} containing the requested info.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the attribute cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    SimpleAttribute getEditableOwnerAttribute(AppContextProvider contextProvider, int ownerAttributeId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches all the info required about a fixed value and its respective
     * owner. The results are packed into a {@link CompoundDataObject} instance,
     * given the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_ATTRIBUTE}: the {@link SimpleAttribute} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUE}: the {@link FixedValue} instance</li>
     * </ul>
     * 
     * @param ownerAttributeId the attribute id.
     * @param fixedValueId the id of the requested value.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed value, and the owner attribute.
     * 
     * @throws ResourceNotFoundException if the attribute or the requested fixed value cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    CompoundDataObject getSingleValueModel(int ownerAttributeId, int fixedValueId)
            throws ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches all the info required about a fixed value and its respective
     * owner. The results are packed into a {@link CompoundDataObject} instance,
     * given the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_ATTRIBUTE}: the {@link SimpleAttribute} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUE}: the {@link FixedValue} instance</li>
     * </ul>
     * This method concerns edit-based scenarios, where the user must be authorized
     * to edit the requested resource.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id.
     * @param fixedValueId the id of the requested value.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed value, and the owner attribute.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the attribute or the requested fixed value cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    CompoundDataObject getEditableSingleValueModel(AppContextProvider contextProvider, int ownerAttributeId, int fixedValueId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches all the info required about the fixed values of a specific owner.
     * The results are packed into a {@link CompoundDataObject} instance, given 
     * the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_ATTRIBUTE}: a {@link SimpleAttribute} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUES}: a {@link List} of {@link FixedValue} instances</li>
     * </ul>
     * 
     * @param ownerAttributeId the attribute id.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed values, and the owner attribute.
     * 
     * @throws ResourceNotFoundException if the attribute cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    CompoundDataObject getAllValuesModel(int ownerAttributeId)
            throws ResourceNotFoundException, ConflictException;
    
    /**
     * Fetches all the info required about the fixed values of a specific owner.
     * The results are packed into a {@link CompoundDataObject} instance, given 
     * the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_ATTRIBUTE}: a {@link SimpleAttribute} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUES}: a {@link List} of {@link FixedValue} instances</li>
     * </ul>
     * This method concerns edit-based scenarios, where the user must be authorized
     * to edit the requested resource.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed values, and the owner attribute.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the attribute cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    CompoundDataObject getEditableAllValuesModel(AppContextProvider contextProvider, int ownerAttributeId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException;
    
    /**
     * Persists the modified data of the fixed value of a specified owner. 
     * Performs an update operation if the value exists; otherwise it will 
     * perform a create operation.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id.
     * @param fixedValue the modification payload.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the attribute or the requested fixed value cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     * @throws EmptyParameterException if the value property of the payload is blank.
     * @throws DuplicateResourceException if the updated/created value already exists.
     */
    void saveFixedValue(AppContextProvider contextProvider, int ownerAttributeId, FixedValue fixedValue)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException;
    
    /**
     * Deletes the requested fixed value of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id.
     * @param fixedValueId the id of the value to be deleted.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the attribute or the requested fixed value cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    void deleteFixedValue(AppContextProvider contextProvider, int ownerAttributeId, int fixedValueId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException;
    
    /**
     * Deletes all fixed values of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws ResourceNotFoundException if the attribute cannot be found.
     * @throws ConflictException if the attribute cannot be associated to fixed values.
     */
    void deleteFixedValues(AppContextProvider contextProvider, int ownerAttributeId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException;
    
    public static final String PROPERTY_OWNER_ATTRIBUTE = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
}
