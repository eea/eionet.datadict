package eionet.datadict.security;

import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class AuthenticationRequestFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private ServletContext servletContext;

    private static final List<String> INTERCEPTED_URL_PATTERNS;
    private static final String GENERIC_DD_UNAUTHORIZED_ACCESS_PAGE_URL = "/error.action?type=NOT_AUTHENTICATED_401&message=You+have+to+login+to+access+this+page";

    static {
        List<String> interceptedUrlPatterns = new LinkedList<String>();
        interceptedUrlPatterns.add("delem_attribute.jsp?mode=add");
        interceptedUrlPatterns.add("attribute/edit/");
        interceptedUrlPatterns.add("vocabulary?add=");
        INTERCEPTED_URL_PATTERNS = Collections.unmodifiableList(interceptedUrlPatterns);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (!isUserAuthenticated(httpRequest)) {
            chain.doFilter(request, response);
            return;
        } else {
            String contextPath = servletContext.getContextPath();
            httpServletResponse.sendRedirect(contextPath == null ? "" : "" + contextPath + GENERIC_DD_UNAUTHORIZED_ACCESS_PAGE_URL);
        }
    }

    private boolean isUserAuthenticated(HttpServletRequest request) {
        String completeUrl = this.getFullURL(request);
        for (String pattern : INTERCEPTED_URL_PATTERNS) {
            if (completeUrl.contains(pattern)) {
                if (getUser(request) == null) {
                    return true;
                }
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