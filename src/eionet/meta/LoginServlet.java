package eionet.meta;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import eionet.util.SecurityUtil;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class LoginServlet extends HttpServlet {
	
	/** */
	private static final String INITPAR_LOGIN_ERROR_PAGE = "login-error-page";

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        String username = req.getParameter("j_username");
        String password = req.getParameter("j_password");
            
        DDUser user = new DDUser();
        if (user.authenticate(username, password)==true) {
            
            SecurityUtil.allocSession(req, user);
            
            res.setContentType("text/html");
            PrintWriter out = res.getWriter();
            out.print(responseText(req));
            out.close();   
        }
        else {
            String loginErrorPage = getServletContext().getInitParameter(INITPAR_LOGIN_ERROR_PAGE);
            SecurityUtil.freeSession(req);
            res.sendRedirect(loginErrorPage);
        }
    }
    
    private String responseText(HttpServletRequest req){
    	
    	String target = req.getParameter("target");
    	StringBuffer buf = new StringBuffer("<html><script>");
    	if (target!=null && target.equals("blank"))
    		buf.append("window.opener.location.reload(true);");
		buf.append("window.close();</script></html>");
    	return buf.toString();
    }
}
