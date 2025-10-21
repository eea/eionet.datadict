package eionet.meta.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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

import org.apache.commons.lang3.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.util.QueryString;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.SQL;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class DataElementJspFilter implements Filter {

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

        String elementId = request.getParameter("delem_id");
        String elementIdentifier = request.getParameter("delem_idf");
        String parentNamespaceIdentifier = request.getParameter("pns");
        boolean isLatestRequested = elementIdentifier != null;
        boolean isCommonElement = StringUtils.isBlank(parentNamespaceIdentifier);

        StringBuilder buf = new StringBuilder(request.getContextPath());

        if (isLatestRequested) {
            if (isCommonElement) {
                buf.append("/dataelements").append("/latest/").append(elementIdentifier);
            } else {
                String[] parentIdentifiers = getParentIdentifiers(parentNamespaceIdentifier);
                buf.append("/datasets/latest/").append(parentIdentifiers[0]).append("/tables/").append(parentIdentifiers[1])
                .append("/elements/").append(elementIdentifier);
            }

        } else {
            buf.append("/dataelements");
            if (!StringUtils.isBlank(elementId)) {
                buf.append("/").append(elementId);
            }
            if (!StringUtils.isBlank(event)) {
                buf.append("/").append(event);
            }
        }

        Map parameterMap = request.getParameterMap() == null ? null : new HashMap(request.getParameterMap());
        if (parameterMap != null && !parameterMap.isEmpty()) {

            parameterMap.remove("delem_id");
            parameterMap.remove("mode");
            if (isLatestRequested) {
                parameterMap.remove("delem_idf");
                parameterMap.remove("pns");
            }

            String queryString = QueryString.toQueryString(parameterMap, "UTF-8");
            if (!StringUtils.isBlank(queryString)) {
                buf.append("/");
                if (!queryString.startsWith("?")) {
                    buf.append("?");
                }
                buf.append(queryString);
            }
        }

        response.sendRedirect(buf.toString());
    }

    /**
     * 
     * @param parentNamespaceIdentifier
     * @return
     * @throws IOException
     */
    private String[] getParentIdentifiers(String parentNamespaceIdentifier) throws IOException {

        Connection conn = null;
        try {
            conn = ConnectionUtil.getConnection();
            String[] parentIdentifiers = new DDSearchEngine(conn).getDataElementParentIdentifiers(parentNamespaceIdentifier);
            if (parentIdentifiers == null || parentIdentifiers.length == 0) {
                throw new IOException("Could not find table and dataset identifiers by this table's namespace: "
                        + parentNamespaceIdentifier);
            } else {
                return parentIdentifiers;
            }
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            SQL.close(conn);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
