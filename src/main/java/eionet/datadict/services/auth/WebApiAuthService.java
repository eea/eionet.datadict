package eionet.datadict.services.auth;

import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface WebApiAuthService {
    
    DDUser authenticate(WebApiAuthInfo contextInfo) throws UserAuthenticationException;
    
}
