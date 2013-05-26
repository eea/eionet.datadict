package eionet.util;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RequestMessages {

    /** */
    public static final String system = RequestMessages.class.getName() + "system";

    /** */
    public static final String htmlLineBreak = "<br/>";

    /**
     *
     * @param request
     * @param attrName
     */
    public static void add(HttpServletRequest request, String attrName, String message) {

        if (request != null && attrName != null && attrName.length()>0 && message != null && message.length()>0) {
            Object o = request.getAttribute(attrName);
            if (o == null || !(o instanceof ArrayList)) {
                o = new ArrayList();
                request.setAttribute(attrName, o);
            }

            ((ArrayList) o).add(message);
        }
    }

    /**
     *
     * @param request
     * @param attrName
     * @param delimiter
     * @return
     */
    public static String get(HttpServletRequest request, String attrName, String delimiter) {

        StringBuffer buf = new StringBuffer();
        if (request != null && attrName != null && attrName.length()>0) {

            if (delimiter == null || delimiter.length() == 0)
                delimiter = System.getProperty("line.separator");

            Object o = request.getAttribute(attrName);
            if (o != null && o instanceof ArrayList) {
                ArrayList list = (ArrayList) o;
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0)
                        buf.append(delimiter);
                    buf.append(list.get(i));
                }
            }
        }

        return buf.toString();
    }
}
