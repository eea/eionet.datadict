package eionet.meta;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import eionet.util.SecurityUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class LoginLogoutServlet extends HttpServlet{

    /**
     * If needed, creates new HttpSession and adds authenticated user object to it.
     * This method will be called anly by login servlet (<CODE>eionet.meta.LoginServlet</CODE>).
     * Throws GeneralException, if the passed user object is not authenticated.
     */
     protected DDUser allocSession(HttpServletRequest servReq, DDUser user) {

         if (user.isAuthentic())
            servReq.getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
         else
             throw new DDRuntimeException("Attempted to store unauthorised user");

         return user;
     }

     /**
      * Frees current <CODE>HttpSession</CODE> object and if it had user attached to it, invalidates the user.
      */
     protected static final void freeSession(HttpServletRequest servReq) {
          HttpSession httpSession = servReq.getSession(false);
          if (httpSession != null) {
              DDUser user = (DDUser)httpSession.getAttribute(SecurityUtil.REMOTEUSER);
              if (user != null) {
                user.invalidate();
                httpSession.removeAttribute(SecurityUtil.REMOTEUSER);
              }

            httpSession.invalidate();
          }
      }
}
