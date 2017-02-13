package eionet.datadict.security;

import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class AuthenticationReqFilter extends UsernamePasswordAuthenticationFilter {

    private static final List<String> INTERCEPTED_URLS = Collections.unmodifiableList(Arrays.asList("ScheduleSynchronizationView", "viewScheduledTaskDetails","ScheduledJobsQueue"));
    private static final List<String> INTERCEPTED_URL_PARAMETERS=Collections.unmodifiableList(Arrays.asList("ScheduledJobsQueue","viewScheduledTaskDetails","ScheduledJobsQueue"));
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURL = httpRequest.getRequestURL().toString();

        //Comes the user, he requires authentication?
        if (!requiresAuthentication(requestURL, httpRequest)) {
            chain.doFilter(request, response);
            return;
        } else {
         httpServletResponse.sendRedirect("/datadict/error.action?type=NOT_AUTHENTICATED_401&message=You+have+to+login+to+access+this+page");
        }

    }

    private boolean requiresAuthentication(String url, HttpServletRequest request) {
        for (String interceptedUrl : INTERCEPTED_URLS) {
            if (url.contains(interceptedUrl)) {
                return getUser(request)!= null;
            }
        }
        for (String urlParameter : INTERCEPTED_URL_PARAMETERS) {
            if (request.getParameterMap().containsKey(urlParameter)) {
                return getUser(request)!= null;                
            }
        }
        return false;
    }

    private DDUser getUser(HttpServletRequest request) {
        return SecurityUtil.getUser(request);
    }

}
