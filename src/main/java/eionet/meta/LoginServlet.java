package eionet.meta;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class LoginServlet extends LoginLogoutServlet {

    /** Serial ID. */
    private static final long serialVersionUID = 1L;

    /** The path to local login page */
    public static final String LOGIN_JSP = "login.jsp";

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String username = req.getParameter("j_username");
        String password = req.getParameter("j_password");

        DDUser user = new DDUser();
        if (user.authenticate(username, password) == true) {

            allocSession(req, user);

            String afterLogin = (String) req.getSession().getAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME);
            if (afterLogin != null) {
                res.sendRedirect(afterLogin);
            } else {
                req.getRequestDispatcher("/").forward(req, res);
            }

        } else {
            freeSession(req);
            res.sendRedirect(req.getContextPath() + "/" + LOGIN_JSP + "?err=");
        }
    }
}
