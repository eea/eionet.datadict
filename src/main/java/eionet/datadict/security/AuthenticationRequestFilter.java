package eionet.datadict.security;

import eionet.datadict.services.VerifyJWTTokenService;
import eionet.datadict.services.impl.VerifyJWTTokenServiceImpl;
import eionet.meta.DDUser;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class AuthenticationRequestFilter extends GenericFilterBean {

    private static final List<String> INTERCEPTED_URL_PATTERNS;
    private static final List<String> JWT_AUTHENTICATION_REQUESTED_URL_PATTERNS;
    private static final String BASE_URL = Props.getRequiredProperty(PropsIF.DD_URL);
    private VerifyJWTTokenService jwtServiceForVerification = new VerifyJWTTokenServiceImpl();

    static {
        List<String> authenticationRequestedPatterns = new ArrayList<String>();
        authenticationRequestedPatterns.add("v2/datasetTable/all");
        authenticationRequestedPatterns.add("v2/dataset/releaseInfo/");
        JWT_AUTHENTICATION_REQUESTED_URL_PATTERNS = Collections.unmodifiableList(authenticationRequestedPatterns);
    }

    static {
        List<String> interceptedUrlPatterns = new ArrayList<String>();
        interceptedUrlPatterns.add("/datasets/add");
        interceptedUrlPatterns.add("/restore_datasets.jsp");
        interceptedUrlPatterns.add("/doc_upload.jsp.jsp");
        interceptedUrlPatterns.add("/cache");
        interceptedUrlPatterns.add("/dataelements/add/?common=true");
        interceptedUrlPatterns.add("/administration");
        interceptedUrlPatterns.add("/checkouts");
        interceptedUrlPatterns.add("/attributes");
        interceptedUrlPatterns.add("/attribute");
        interceptedUrlPatterns.add("/import.jsp");
        interceptedUrlPatterns.add("/cleanup");
        interceptedUrlPatterns.add("/subscribe.jsp");
        interceptedUrlPatterns.add("/schemaset?add=");
        interceptedUrlPatterns.add("/schemasets/browse/workingCopies");
        interceptedUrlPatterns.add("/schema/root?add=");
        interceptedUrlPatterns.add("/vocabulary?add=");
        interceptedUrlPatterns.add("/vocabularies/maintain");
        interceptedUrlPatterns.add("**/datasets/*/edit");
        interceptedUrlPatterns.add("/inference_rules");
        interceptedUrlPatterns.add("/generateJWTToken");
        interceptedUrlPatterns.add("/v2/admintools/**");
        INTERCEPTED_URL_PATTERNS = Collections.unmodifiableList(interceptedUrlPatterns);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (getUser(httpRequest) == null && isAuthenticationRequired(getFullURL(httpRequest))) {
            httpServletResponse.sendRedirect(SecurityUtil.getLoginURL(httpRequest));
        }
        else if (getUser(httpRequest) == null && isJWTAuthenticationRequired(getFullURL(httpRequest))) {
            String token = ((HttpServletRequest) request).getHeader(Props.getProperty(PropsIF.DD_JWT_HEADER));
            if(StringUtils.isNotBlank(token) && jwtServiceForVerification.verifyToken(token)){
                chain.doFilter(request, response);
            }
            else{
                httpServletResponse.sendError(HttpStatus.SC_UNAUTHORIZED);
            }
        }
        else {
            chain.doFilter(request, response);
        }
    }

    private boolean isAuthenticationRequired(String url) {
        AntPathMatcher urlMatcher = new AntPathMatcher();
        for (String interceptedUrlPattern : INTERCEPTED_URL_PATTERNS) {
            if (url.startsWith(BASE_URL + interceptedUrlPattern)) {
                return true;
            }
            if(urlMatcher.match(interceptedUrlPattern, url)){
                return true;
            }
        }
        return false;
    }

    private boolean isJWTAuthenticationRequired(String url) {
        AntPathMatcher urlMatcher = new AntPathMatcher();
        for (String interceptedUrlPattern : JWT_AUTHENTICATION_REQUESTED_URL_PATTERNS) {
            if (url.startsWith(BASE_URL + interceptedUrlPattern)) {
                return true;
            }
            if(urlMatcher.match(interceptedUrlPattern, url)){
                return true;
            }

            if(url.contains(interceptedUrlPattern)){
                return true;
            }
        }
        return false;
    }


    private DDUser getUser(HttpServletRequest request) {
        return SecurityUtil.getUser(request);
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

}