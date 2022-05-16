package eionet.web.action;

import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import eionet.web.DDActionBeanContext;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ActionMethodUtils {

    private static HttpSession getSession(DDActionBeanContext context) {
        HttpServletRequest request = null;
        if (context!=null) {
            request = context.getRequest();
        }
        if (request!=null) {
            HttpSession session = request.getSession();
            return session;
        }
        return null;
    }

    private static String getSessionId(HttpSession session) {
        return session != null ? session.getId().substring(0, 16) : "";
    }

    private static String getUsername(HttpSession session) {
        DDUser user = null;
        if (session!=null) {
            user = (DDUser) session.getAttribute(SecurityUtil.REMOTEUSER);
        }
        return user!=null ? user.getUserName() : "";
    }

    public static void setLogParameters(DDActionBeanContext context) {
        HttpSession session = getSession(context);
        String sessionId = getSessionId(session);
        String username = getUsername(session);
        MDC.put("sessionId", sessionId);
        MDC.put("username", username);
    }
}
