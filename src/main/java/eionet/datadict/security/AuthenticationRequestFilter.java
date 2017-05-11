package eionet.datadict.security;

import eionet.meta.DDUser;
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
import org.springframework.web.filter.GenericFilterBean;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class AuthenticationRequestFilter extends GenericFilterBean {

    private static final List<String> INTERCEPTED_URL_PATTERNS;

    static {
        List<String> interceptedUrlPatterns = new ArrayList<String>();
        interceptedUrlPatterns.add("delem_attribute.jsp?mode=add");
        interceptedUrlPatterns.add("attribute/edit/");
        interceptedUrlPatterns.add("vocabulary?add=");
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
        for (String pattern : INTERCEPTED_URL_PATTERNS) {
            if (url.contains(pattern)) {
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