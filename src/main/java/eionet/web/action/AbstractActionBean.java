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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.SimpleMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.util.SecurityUtil;
import eionet.web.DDActionBeanContext;

/**
 * Root class for all DD ActionBeans.
 *
 * @author Juhan Voolaid
 */
public abstract class AbstractActionBean implements ActionBean {

    /** Logger. */
    protected static Log LOGGER = LogFactory.getLog(AbstractActionBean.class);

    private static final String SYSTEM_MESSAGES = "systemMessages";
    private static final String CAUTION_MESSAGES = "cautionMessages";
    private static final String WARNING_MESSAGES = "warningMessages";

    /** DD ActionBeanContext extension. */
    private DDActionBeanContext context;

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
}
