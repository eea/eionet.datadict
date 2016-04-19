package eionet.meta.service;

import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface WebApiAuthService {

    DDUser authenticate(HttpServletRequest request) throws UserAuthenticationException;
    
}
