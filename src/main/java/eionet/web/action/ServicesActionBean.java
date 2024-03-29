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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.meta.service.ServiceException;

/**
 * Services action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/services/list")
public class ServicesActionBean extends AbstractActionBean {

    /** Page. */
    private static final String VIEW_SERVICES_JSP = "/pages/services/services.jsp";

    /**
     * View services action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        return new ForwardResolution(VIEW_SERVICES_JSP);
    }
}
