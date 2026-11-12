package eionet.datadict.services;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.model.Attribute;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;

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
}
