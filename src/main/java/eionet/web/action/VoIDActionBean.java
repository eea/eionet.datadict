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

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.exports.rdf.VoIDXmlWriter;
import eionet.meta.service.IDataService;
import eionet.util.sql.ConnectionUtil;

/**
 * The Vocabulary of data elements with fixed values (VoID) action bean. http://www.w3.org/TR/void/
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/void.rdf")
public class VoIDActionBean extends AbstractActionBean {

    private String contextRoot;

    @SpringBean
    private IDataService dataService;

    /**
     * Handles the request for viewing VoID.
     *
     * @return
     */
    @DefaultHandler
    public Resolution getVoID() {
        contextRoot = StringUtils.substringBeforeLast(getContext().getRequest().getRequestURL().toString(), "/");

        StreamingResolution result = new StreamingResolution("application/xml") {
            public void stream(HttpServletResponse response) throws Exception {
                List<DataElement> dataElements = dataService.getDataElementsWithFixedValues();

                HashSet<String> datasetStatuses = new HashSet<String>();
                datasetStatuses.add("Released");
                datasetStatuses.add("Recorded");
                DDSearchEngine searchEngine = new DDSearchEngine(ConnectionUtil.getConnection());
                Vector tables = searchEngine.getDatasetTables(null, null, null, null, null, null, datasetStatuses, false);


                VoIDXmlWriter xmlWriter = new VoIDXmlWriter(response.getOutputStream(), contextRoot);
                xmlWriter.writeVoIDXml(dataElements, tables);
            }
        };
        return result;
    }

}
