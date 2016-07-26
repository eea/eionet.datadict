package eionet.datadict.services.auth;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface WebApiAuthInfoService {

    WebApiAuthInfo getAuthenticationInfo(HttpServletRequest request);
    
}