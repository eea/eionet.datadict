package eionet.meta.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import eionet.util.QueryString;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class DatasetJspFilter implements Filter {

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Auto-generated method stub
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
        } else {
            doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
    ServletException {

        String event = request.getParameter("mode");
        String action = request.getParameter("action");
        if (action != null && !action.isEmpty()) {
            event = action;
        }

        if (StringUtils.isBlank(event)) {
            event = "view";
        }

        String datasetId = request.getParameter("ds_id");

        StringBuilder buf = new StringBuilder(request.getContextPath());
        buf.append("/datasets");
        if (!StringUtils.isBlank(datasetId)){
            buf.append("/").append(datasetId);
        }
        buf.append("/").append(event);

        Map parameterMap = request.getParameterMap()==null ? null : new HashMap(request.getParameterMap());
        if (parameterMap!=null && !parameterMap.isEmpty()){

            parameterMap.remove("ds_id");
            parameterMap.remove("mode");
            parameterMap.remove("action");

            String queryString = QueryString.toQueryString(parameterMap, "UTF-8");
            if (!StringUtils.isBlank(queryString)){
                buf.append("/");
                if (!queryString.startsWith("?")){
                    buf.append("?");
                }
                buf.append(queryString);
            }

        }

        response.sendRedirect(buf.toString());
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Auto-generated method stub
    }
}
