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
		if (pathInfo==null || pathInfo.length()==0){
			request.getRequestDispatcher("search_results_tbl.jsp").forward(request, response);
		}
		else{
			String[] parts = splitPathInfo(pathInfo);
			
			String id = parts[0];
			String action = parts.length==1 ? "view" : (parts[1].length()==0 ? "view" : parts[1]);			
			if (action.equals("view") || action.equals("edit")){
				
				QueryString qs = new QueryString(request.getContextPath() + "/dstable.jsp");
				qs.changeParam("table_id", id);
				qs.changeParam("mode", action);
				
				response.sendRedirect(qs.getValue());
			}
			else if (action.equals("rdf")){
				DDHttpServletRequestWrapper wrappedRequest = new DDHttpServletRequestWrapper(request);
				wrappedRequest.addParameter("id", id);
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
