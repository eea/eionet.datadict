package eionet.datadict.services;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.model.Attribute;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;

public interface AttributeService {
    
    int save(Attribute attribute, DDUser user) 
            throws UserAuthenticationException, UserAuthorizationException, BadRequestException;
    
    void delete(int attributeId, DDUser user)
            throws UserAuthenticationException, UserAuthorizationException;
}
