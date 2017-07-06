package eionet.datadict.security;

import eionet.meta.DDUser;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class AuthenticationRequestFilter extends GenericFilterBean {

    private static final List<String> INTERCEPTED_URL_PATTERNS;
    private static final String BASE_URL = Props.getRequiredProperty(PropsIF.DD_URL);

    static {
        List<String> interceptedUrlPatterns = new ArrayList<String>();
        interceptedUrlPatterns.add("/datasets/add");
        interceptedUrlPatterns.add("/restore_datasets.jsp");
        interceptedUrlPatterns.add("/doc_upload.jsp.jsp");
        interceptedUrlPatterns.add("/cache");
        interceptedUrlPatterns.add("/delem_attribute.jsp?mode=add");
        interceptedUrlPatterns.add("/dataelements/add/?common=true");
        interceptedUrlPatterns.add("/checkouts");
        interceptedUrlPatterns.add("/attributes.jsp");
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
        INTERCEPTED_URL_PATTERNS = Collections.unmodifiableList(interceptedUrlPatterns);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (getUser(httpRequest) == null && isAuthenticationRequired(getFullURL(httpRequest))) {
            httpServletResponse.sendRedirect(SecurityUtil.getLoginURL(httpRequest));
        } else {
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