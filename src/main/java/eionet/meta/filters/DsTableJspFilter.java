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
public class DsTableJspFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Auto-generated method stub
    }

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
        if (action != null && action.equals("subscribe")) {
            event = action;
        }

        String tableId = request.getParameter("table_id");
        String tableIdentifier = request.getParameter("table_idf");
        String parentNamespaceIdentifier = request.getParameter("pns");
        boolean isLatestRequested = tableIdentifier != null && parentNamespaceIdentifier != null;

        StringBuilder buf = new StringBuilder(request.getContextPath());

        if (isLatestRequested) {
            String datasetIdentifier = getDatasetIdentifierByNamespace(parentNamespaceIdentifier);
            buf.append("/datasets/latest/").append(datasetIdentifier).append("/tables/").append(tableIdentifier);
        } else {
            buf.append("/tables");
            if (!StringUtils.isBlank(tableId)) {
                buf.append("/").append(tableId);
            }
            if (!StringUtils.isBlank(event)) {
                buf.append("/").append(event);
            }
        }

        Map parameterMap = request.getParameterMap() == null ? null : new HashMap(request.getParameterMap());
        if (parameterMap != null && !parameterMap.isEmpty()) {

            parameterMap.remove("table_id");
            parameterMap.remove("mode");
            if (isLatestRequested) {
                parameterMap.remove("table_idf");
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

    @Override
    public void destroy() {
        // Auto-generated method stub
    }

    /**
     * 
     * @param namespaceId
     * @return
     * @throws IOException
     */
    private String getDatasetIdentifierByNamespace(String namespaceId) throws IOException {

        Connection conn = null;
        try {
            conn = ConnectionUtil.getConnection();
            return new DDSearchEngine(conn).getDatasetIdentifierByNamespace(namespaceId);
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            SQL.close(conn);
        }
    }
}
