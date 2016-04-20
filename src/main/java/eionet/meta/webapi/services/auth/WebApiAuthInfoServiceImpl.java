package eionet.meta.webapi.services.auth;

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
