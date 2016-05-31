package eionet.datadict.services;

import eionet.datadict.model.Attribute;
import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;

public interface AttributeService {
    
    int save(Attribute attribute, DDUser user) 
            throws UserAuthenticationException, UserAuthorizationException;
    
}
