package eionet.datadict.security;

import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final String VOCABULARY_RESOURCE = "vocabulary";
    private static final String VOCABULARIES_RESOURCE ="vocabularies";
    private static final Map<String, List<String>> INTERCEPTED_DATADICT_ACTIONS;
  
    private static final String GENERIC_DD_UNAUTHORIZED_ACCESS_PAGE_URL="/error.action?type=NOT_AUTHENTICATED_401&message=You+have+to+login+to+access+this+page";
    static {
        Map<String, List<String>> dataDictActions = new LinkedHashMap<String, List<String>>();
        dataDictActions.put(VOCABULARY_RESOURCE, Arrays.asList("add","ScheduledJobsQueue","ScheduleSynchronizationView"));
        dataDictActions.put(VOCABULARIES_RESOURCE, Arrays.asList("maintain"));
        INTERCEPTED_DATADICT_ACTIONS = Collections.unmodifiableMap(dataDictActions);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURL = httpRequest.getRequestURL().toString();

        if (!requiresAuthentication(requestURL, httpRequest)) {
            chain.doFilter(request, response);
            return;
        } else {
           String contextPath = servletContext.getContextPath();
            httpServletResponse.sendRedirect(contextPath==null?"":""+contextPath+GENERIC_DD_UNAUTHORIZED_ACCESS_PAGE_URL);
        }
    }

    private boolean requiresAuthentication(String url, HttpServletRequest request) {
        if (url.contains(VOCABULARY_RESOURCE)) {
            List<String> vocabularyActions = INTERCEPTED_DATADICT_ACTIONS.get(VOCABULARY_RESOURCE);
            for (String vocabularyAction : vocabularyActions) {
                if (request.getParameter(vocabularyAction)!=null) {
                    if(getUser(request)==null){
                     return true; 
                    }
                }
            }
        }
        return false;
    }

    private DDUser getUser(HttpServletRequest request) {
        return SecurityUtil.getUser(request);
    }
    
}
