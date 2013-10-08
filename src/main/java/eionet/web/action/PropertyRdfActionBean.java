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
 *        Jaanus Heinlaid
 */

package eionet.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;

/**
 * Redirecting bean for common elements.
 * Regular web/browser is redirected to Data Element page
 * RDF browser is redirected to the list of common elements in RDF format
 *
 * @author Kaido Laine
 */
@UrlBinding("/property/{identifier}")
public class PropertyRdfActionBean extends AbstractActionBean {

    //private DataElement dataElement;

    /**
     * local dataservice instance.
     */
    @SpringBean
    private IDataService dataService;

    /**
     * common data element identifier.
     */
    private String identifier;


    /**
     * Redirects as specified in the class declaration.
     * @return resolution to be forwarded to
     * @throws ServiceException if DB query does not succeed
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        Resolution r;
        if (isWebBrowser()) {
            String elemId = String.valueOf(dataService.getCommonElementIdByIdentifier(identifier));
            if (!elemId.equals("0")) {
                r = new RedirectResolution("/dataelements/" + elemId);
            } else {
                r = new RedirectResolution("/error.action")
                    .addParameter("message", "No common element with identifier " + identifier);
            }
        } else {
            r = new RedirectResolution("/properties/rdf");
        }

        return r;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
