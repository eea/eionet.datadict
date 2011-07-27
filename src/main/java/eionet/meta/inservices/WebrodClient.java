package eionet.meta.inservices;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import eionet.util.Util;

public class WebrodClient extends InServiceClient{

    public static final String NAME = "webrod";
    public static final String GET_ACTIVITIES = "get_activities";
    public static final String RELOAD_ACTIVITIES = "reload_activities";

    public WebrodClient() {
        getProps(NAME);
    }

    public void execute(HttpServletRequest req) throws Exception {

        req.setAttribute(Attrs.ERR_HANDLER, "error.jsp?class=popup");

        String method = req.getParameter(Params.METHOD);
        if (Util.isEmpty(method))
            throw new Exception(Params.METHOD + " is missing!");

        if (method.equals(GET_ACTIVITIES))
            getActivities(req, false);
        else if (method.equals(RELOAD_ACTIVITIES))
            getActivities(req, true);
        else
            throw new Exception("Unknown method " + method);
    }

    public void getActivities(HttpServletRequest req, boolean reload) throws Exception {

        req.setAttribute(Attrs.DISPATCHER, "rod_activities.jsp");

        HttpSession session = req.getSession();
        if (!reload) {
            Object o = session.getAttribute(Attrs.ROD_ACTIVITIES);
            if (o!=null) return;
        }
        else
            session.removeAttribute(Attrs.ROD_ACTIVITIES);

        Vector params = new Vector();
        session.setAttribute(Attrs.ROD_ACTIVITIES, execute("getActivities", params));
    }
}
