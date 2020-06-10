package eionet.meta;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.util.SecurityUtil;

/**
 * Simple servlet to handle logout form submit.
 *
 * @author  Jaanus Heinlaid
 */

public class LogoutServlet extends LoginLogoutServlet {

    /** */
    public static final String LOGOUT_PAGE = "logout-page";

    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
                                            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        DDUser user = SecurityUtil.getUser(req);
        if (user != null)
            freeSession(req);

        if(user.isLocalUser()){
            res.sendRedirect(SecurityUtil.getLogoutURLForLocalUserAccount(req));
        }else {
            res.sendRedirect(SecurityUtil.getLogoutURL(req));
        }
    }
}
