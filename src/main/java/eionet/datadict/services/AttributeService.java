package eionet.datadict.services;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.datadict.model.DataDictEntity;
import eionet.meta.dao.domain.VocabularyConcept;
import java.util.List;

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
    
    /**
     * First fetches the vocabulary which is bound to the attribute with the given id. Then fetches all the vocabulary values which 
     * are assigned to this same attribute owned by the given DataDict Entity.
     * 
     * @param attributeId The id of the attribute whose values are to be fetched.
     * @param ddEntity The owner of the attribute values.
     * @param inheritanceMode the inheritance mode
     * 
     * @return {@link List} of the {@link VocabularyConcept} elements.  
     * 
     * @throws eionet.datadict.errors.ResourceNotFoundException  
     * @throws eionet.datadict.errors.EmptyParameterException  
     */
    public List<VocabularyConcept> getAttributeVocabularyConcepts(int attributeId, DataDictEntity ddEntity, ValueInheritanceMode inheritanceMode)
            throws ResourceNotFoundException, EmptyParameterException ; 
    
    /**
     * First fetches the vocabulary which is bound to the attribute with the given id. Then fetches all the vocabulary values of this  
     * attribute which the given DataDictEntity inherits.
     * 
     * @param attributeId The id of the attribute whose values are to be fetched.
     * @param ddEntity The owner of the attribute values.
     * 
     * @return {@link List} of the {@link VocabularyConcept} elements.  
     * 
     * @throws eionet.datadict.errors.ResourceNotFoundException  
     * @throws eionet.datadict.errors.EmptyParameterException  
     */
    public List<VocabularyConcept> getInherittedAttributeVocabularyConcepts(int attributeId, DataDictEntity ddEntity)
            throws ResourceNotFoundException, EmptyParameterException;
    
}
