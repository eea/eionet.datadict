/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.action;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.validation.SimpleError;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;

import eionet.meta.DDUser;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.web.DDActionBeanContext;

/**
 * Root class for all DD ActionBeans.
 *
 * @author Juhan Voolaid
 */
public abstract class AbstractActionBean implements ActionBean {

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(AbstractActionBean.class);

    private static final String SYSTEM_MESSAGES = "systemMessages";
    private static final String CAUTION_MESSAGES = "cautionMessages";
    private static final String WARNING_MESSAGES = "warningMessages";

    /** DD ActionBeanContext extension. */
    private DDActionBeanContext context;

    /** */
    private String urlBinding;

    /** */
    private String contextPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public DDActionBeanContext getContext() {
        return this.context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContext(final ActionBeanContext context) {
        this.context = (DDActionBeanContext) context;
    }

    /**
     * Adds system message. The message will be shown in a simple rectangle and is to provide information on <i>successful</i>
     * actions.
     *
     * @param message
     *            Message text in HTML format.
     */
    protected void addSystemMessage(final String message) {
        getContext().getMessages(SYSTEM_MESSAGES).add(new SimpleMessage(message));
    }

    /**
     * Adds caution message. The message will be shown wrapped in the &lt;div class="caution-msg"&lt; element. A caution is less
     * severe than a warning. It can e.g. be used when the application has to say to the user that it has ignored some input.
     *
     * @param message
     *            Message text in HTML format.
     */
    protected void addCautionMessage(final String message) {
        getContext().getMessages(CAUTION_MESSAGES).add(new SimpleMessage(message));
    }

    /**
     * Adds warning message. The message will be shown wrapped in the &lt;div class="warning-msg"&lt; element.
     *
     * @param message
     *            Message text in HTML format.
     */
    protected void addWarningMessage(final String message) {
        getContext().getMessages(WARNING_MESSAGES).add(new SimpleMessage(message));
    }

    /**
     *
     * @param simpleErrorMessage
     */
    protected void addGlobalValidationError(String simpleErrorMessage) {
        context.getValidationErrors().addGlobalError(new SimpleError(simpleErrorMessage));
    }

    /**
     * Returns site URL beginning part. It is configured in the datadict.properties file.
     *
     * @return
     */
    public String getSitePrefix() {
        return Props.getProperty(PropsIF.JSP_URL_PREFIX);
    }

    /**
     * Method returns {@link DDUser} from session.
     *
     * @return {@link DDUser} from session or null if user is not logged in.
     */
    public DDUser getUser() {
        return SecurityUtil.getUser(getContext().getRequest());
    }

    /**
     *
     * @return
     */
    public String getUserName() {
        DDUser user = getUser();
        return user == null ? null : user.getUserName();
    }

    /**
     * Method returns login url.
     *
     * @return String.
     */
    public String getLoginUrl() {
        return SecurityUtil.getLoginURL(getContext().getRequest());
    }

    /**
     * Method returns logout url.
     *
     * @return String.
     */
    public String getLogoutUrl() {
        return SecurityUtil.getLogoutURL(getContext().getRequest());
    }

    /**
     * Method checks whether user is logged in or not.
     *
     * @return true if user is logged in.
     */
    public final boolean isUserLoggedIn() {
        return getUser() != null;
    }

    /**
     *
     * @return
     */
    public boolean isPostRequest() {
        return getContext().getRequest().getMethod().equalsIgnoreCase("POST");
    }

    /**
     *
     * @return
     */
    public boolean isGetOrHeadRequest() {
        String method = getContext().getRequest().getMethod().toUpperCase();
        return method.equals("GET") || method.equals("HEAD");
    }

    /**
     *
     * @return
     */
    public String getUrlBinding() {
        if (urlBinding == null) {
            urlBinding = new AnnotatedClassActionResolver().getUrlBinding(this.getClass());
            if (urlBinding == null) {
                urlBinding = "";
            }
        }
        return urlBinding;
    }

    /**
     *
     * @return
     */
    public String getContextPath() {
        if (contextPath == null) {
            contextPath = getContext().getRequest().getContextPath();
        }
        return contextPath;
    }

    /**
     *
     * @return
     */
    public boolean isValidationErrors(){
        return MapUtils.isNotEmpty(getContext().getValidationErrors());
    }

    /**
     *
     */
    protected void dumpRequestParameters() {

        HttpServletRequest request = getContext().getRequest();
        Enumeration paramNames = request.getParameterNames();

        // System.out.println(">>>>>>>>>> start request parameters dump");
        // if (paramNames != null && paramNames.hasMoreElements()) {
        // do {
        // String paramName = paramNames.nextElement().toString();
        // String[] paramValues = request.getParameterValues(paramName);
        // System.out.print(paramName + " = [");
        // for (int i = 0; i < paramValues.length; i++) {
        // if (i > 0) {
        // System.out.print(", ");
        // }
        // System.out.print(paramValues[i]);
        // }
        // System.out.println("]");
        // } while (paramNames.hasMoreElements());
        // } else {
        // System.out.println("No request parameters found");
        // }
        // System.out.println("<<<<<<<<<< end request parameters dump");
    }
}
