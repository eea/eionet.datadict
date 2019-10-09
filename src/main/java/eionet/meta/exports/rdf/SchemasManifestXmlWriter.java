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

package eionet.meta.exports.rdf;

import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemasResult;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Schemas RDF manifest xml writer.
 *
 * @author Juhan Voolaid
 */
public class SchemasManifestXmlWriter extends SchemasBaseManifestXmlWriter {

    public SchemasManifestXmlWriter(OutputStream out, ISchemaService schemaService) {
        super(out, schemaService);
    }

    @Override
    protected void writeDocumentBody(XMLStreamWriter writer) throws XMLStreamException, ServiceException {
        SchemaFilter schemaFilter = new SchemaFilter();
        schemaFilter.setUsePaging(false);
        SchemasResult schemaResult = this.getSchemaService().searchSchemas(schemaFilter);

        for (Schema s : schemaResult.getList()) {
            if (s.isOtherDocument()) {
                continue;
            }
            
            String schemaId = s.getSchemaSetIdentifier() == null ? SchemaSet.ROOT_IDENTIFIER : s.getSchemaSetIdentifier();
            String schemaUri = String.format("schema/%s/%s", schemaId, s.getFileName());
            
            writer.writeStartElement(CR_NS_PREFIX, "XMLSchema", CR_NS);
            writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "about", schemaUri);

            writer.writeStartElement(RDFS_NS, "label");
            writer.writeCharacters(s.getNameAttribute());
            writer.writeEndElement();

            writer.writeEndElement();
        }
    }
    
}
