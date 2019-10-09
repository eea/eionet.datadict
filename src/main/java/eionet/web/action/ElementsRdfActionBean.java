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

import java.net.HttpURLConnection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.exports.rdf.DataElementXmlWriter;
import eionet.meta.service.IDataService;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Action bean that serves RDF output of common data elements.
 *
 * @author Kaido Laine
 */
@UrlBinding("/properties/rdf")
public class ElementsRdfActionBean extends AbstractActionBean {

    /**
     * dataservice instance.
     */
    @SpringBean
    private IDataService dataService;

    /**
     * default RDF output.
     *
     * @return Stripes resolution
     */
    @DefaultHandler
    public Resolution getRdf() {
        try {
            final String contextRoot = Props.getRequiredProperty(PropsIF.DD_URL) + "/property/";
            // final List<DataE>
            final List<DataElement> commonElements = dataService.getReleasedCommonDataElements();
            StreamingResolution result = new StreamingResolution("application/xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    DataElementXmlWriter xmlWriter = new DataElementXmlWriter(response.getOutputStream());
                    xmlWriter.writeRDFXml(contextRoot, commonElements);
                }
            };
            return result;

        } catch (Exception e) {
            LOGGER.error("Failed to output common elements RDF data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }
}
