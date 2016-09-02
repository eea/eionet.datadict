package eionet.datadict.services;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.ConflictException;
import eionet.datadict.model.Attribute;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.datadict.model.DataDictEntity;

public interface AttributeService {
    
    /**
     * Saves the given attribute on behalf of the given user.
     * 
     * @param attribute the {@link Attribute} to be saved.
     * @param user the {@link DDUser} who requested for the attribute to be saved.
     * @return the id of the attribute which was saved.
     * 
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws BadRequestException 
     */
    public int save(Attribute attribute, DDUser user) 
            throws UserAuthenticationException, UserAuthorizationException, BadRequestException;
    
    
    /**
     * Saves the given attribute value of the attribute with the given id, owned by the specified owner,
     * on behalf of the specified user. This attribute value corresponds to a vocabulary concept of the vocabulary 
     * linked to the attribute with the given id.
     * 
     * @param attributeId the id of the attribute whose value is to be saved.
     * @param ownerEntity the owner of the attribute value to be saved.
     * @param value the value to be saved.
     * @param user the {@link DDUser} who requested for the attribute value to be saved.
     * 
     * @throws ConflictException
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
    public void saveAttributeVocabularyValue(int attributeId, DataDictEntity ownerEntity, String value, DDUser user)
            throws ConflictException, UserAuthenticationException, UserAuthorizationException;
    
    /**
     * Deletes the attribute with the given id on behalf of the given user.
     * 
     * @param attributeId the id of the attribute to be deleted.
     * @param user the {@link DDUser} who requested for the attribute to be deleted.
     * 
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
    public void delete(int attributeId, DDUser user)
            throws UserAuthenticationException, UserAuthorizationException;
    
    /**
     * Deletes the attribute value of the attribute with the given value and the given id, 
     * which corresponds to the the given owner, on behalf on the given user.
     * 
     * @param attributeId the id of the attribute to be deleted.
     * @param ownerEntity the owner of the attribute to be deleted.
     * @param value the value of the attribute to be deleted.
     * @param user the {@link DDUser} who requested for the attribute to be deleted.
     * 
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
    public void deleteAttributeValue(int attributeId, DataDictEntity ownerEntity, String value, DDUser user) 
            throws UserAuthenticationException, UserAuthorizationException;
    
    /**
     * Deletes all attributes values of the attribute with the given id corresponding to the specified owner,
     * on behalf of the specified user.
     * 
     * @param attributeId the id of the attribute whose attribute values are to be deleted.
     * @param ownerEntity the owner of the attribute values to be deleted.
     * @param user the {@link DDUser} who requested for the attribute values to be deleted.
     * 
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
    public void deleteAllAttributeValues(int attributeId, DataDictEntity ownerEntity, DDUser user)
            throws UserAuthenticationException, UserAuthorizationException;
}
