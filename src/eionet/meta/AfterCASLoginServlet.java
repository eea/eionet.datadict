package eionet.meta;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class AfterCASLoginServlet extends HttpServlet{
	
	/** */
	public static final String AFTER_LOGIN_ATTR_NAME = "afterLogin";

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	
		String afterLogin = (String)((HttpServletRequest)request).getSession().getAttribute(AFTER_LOGIN_ATTR_NAME);
    	if (afterLogin != null)
			((HttpServletResponse)response).sendRedirect(afterLogin);
		else
			request.getRequestDispatcher("/").forward(request,response);
    }
}

