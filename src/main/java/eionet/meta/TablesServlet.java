package eionet.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.filters.DDHttpServletRequestWrapper;
import eionet.util.QueryString;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class TablesServlet extends HttpServlet{
    
    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        
        String pathInfo = request.getPathInfo();
        if (pathInfo==null || pathInfo.length()==0 || pathInfo.equals("/")){
            request.getRequestDispatcher("/search_results_tbl.jsp").forward(request, response);
        }

        String[] pathInfoParts = splitPathInfo(pathInfo);
        if (pathInfoParts.length==1 && pathInfoParts[0].equals("rdf")){
            
            DDHttpServletRequestWrapper wrappedRequest = new DDHttpServletRequestWrapper(request);
            wrappedRequest.getRequestDispatcher("/GetRdf").forward(wrappedRequest, response);
            return;
        }

        // at this point we can assume the array length >= 1
        String tableId = pathInfoParts[0];
        String mode = "view";
        
        if (pathInfoParts.length>=2){
            mode = pathInfoParts[1];
        }
        
        if (mode.equals("rdf")){
            
            DDHttpServletRequestWrapper wrappedRequest = new DDHttpServletRequestWrapper(request);
            wrappedRequest.addParameter("id", tableId);
            wrappedRequest.getRequestDispatcher("/GetRdf").forward(wrappedRequest, response);
        }
        else if (mode.equals("view") || mode.equals("edit")){
            
            QueryString qs = new QueryString(request.getContextPath() + "/dstable.jsp");
            qs.changeParam("table_id", tableId);
            qs.changeParam("mode", mode);
            response.sendRedirect(qs.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service_(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        
        String pathInfo = request.getPathInfo();
        String[] pathInfoParts = null;
        if (pathInfo!=null && pathInfo.length()>0){
            pathInfoParts = splitPathInfo(pathInfo);
        }
        
        if (pathInfoParts==null || pathInfoParts.length==0){
            request.getRequestDispatcher("/search_results_tbl.jsp").forward(request, response);
        }
        else{
            String tableId = pathInfoParts[0];
            String action = "view";
            if (pathInfoParts.length>1 && pathInfoParts[1].length()>0){
                action = pathInfoParts[1];          
            }
            
            if (action.equals("view") || action.equals("edit")){
                
                QueryString qs = new QueryString(request.getContextPath() + "/dstable.jsp");
                qs.changeParam("table_id", tableId);
                qs.changeParam("mode", action);
                
                response.sendRedirect(qs.getValue());
            }
            else if (action.equals("rdf")){
                DDHttpServletRequestWrapper wrappedRequest = new DDHttpServletRequestWrapper(request);
                wrappedRequest.addParameter("id", tableId);
                wrappedRequest.getRequestDispatcher("/GetRdf").forward(wrappedRequest, response);
            }
        }
    }
    
    /**
     * 
     * @param pathInfo
     * @return
     */
    protected String[] splitPathInfo(String pathInfo){
        
        ArrayList list = new ArrayList();
        StringTokenizer st = new StringTokenizer(pathInfo, "/");
        while (st.hasMoreTokens()){
            String token = st.nextToken().trim();
            if (token.length()>0){
                list.add(token);
            }
        }
        
        return (String[])list.toArray(new String[list.size()]);
    }
}
