package eionet.meta;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.xmlserver.*;

import eionet.util.SecurityUtil;

/**
 * Simple servlet to handle logout form submit.
 *
 * @author  Jaanus Heinlaid
 */

public class LogoutServlet extends HttpServlet {
    
    public static final String LOGOUT_PAGE = "logout-page";

/**
 *
 */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
		req.setCharacterEncoding("UTF-8");
		
        AppUserIF user = SecurityUtil.getUser(req);
        if (user != null){
            
            SecurityUtil.freeSession(req);
        }    
            res.setContentType("text/html");
            try {
                PrintWriter out = res.getWriter();
                //out.print("<html><body><table width='100%' height='100%'><tr><td align='center'><b>Logging out...</b></td></tr></table><script>window.opener.document.location.reload(true); window.setTimeout('window.close()', 1000);</script></body></html>");
                out.print("<html><body><table width='100%' height='100%'><tr><td align='center'><b>Logging out...</b></td></tr></table><script>window.opener.document.location='index.jsp'; window.setTimeout('window.close()', 1000);</script></body></html>");
                out.close();
            } catch (IOException e) {
                Logger.log("Writing page to response stream failed", e);
            }

    }
}
