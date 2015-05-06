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

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import eionet.meta.exports.rdf.SchemasManifestXmlWriter;
import eionet.meta.service.ISchemaService;

/**
 * Action bean that serves RDF output of schemas.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schemas/rdf")
public class SchemasRdfActionBean extends AbstractActionBean {

    @SpringBean
    private ISchemaService schemaService;

    /**
     * Handles the request for viewing VoID.
     *
     * @return
     */
    @DefaultHandler
    public Resolution getRdf() {
        StreamingResolution result = new StreamingResolution("application/xml") {
            
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                SchemasManifestXmlWriter xmlWriter = new SchemasManifestXmlWriter(response.getOutputStream(), schemaService);
                xmlWriter.writeManifestXml();
            }
        };
        
        return result;
    }
}
