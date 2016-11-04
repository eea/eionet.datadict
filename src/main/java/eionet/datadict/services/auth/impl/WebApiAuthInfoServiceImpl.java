package eionet.datadict.services.auth.impl;

import eionet.datadict.services.auth.WebApiAuthInfo;
import eionet.datadict.services.auth.WebApiAuthInfoService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class WebApiAuthInfoServiceImpl implements WebApiAuthInfoService {

    public static final String JWT_API_KEY_HEADER = "X-DD-API-KEY";
    
    @Override
    public WebApiAuthInfo getAuthenticationInfo(HttpServletRequest request) {
        String authToken = request.getHeader(JWT_API_KEY_HEADER);
        
        return new WebApiAuthInfo(authToken, request.getRemoteHost(), request.getRemoteAddr());
    }
    
}
