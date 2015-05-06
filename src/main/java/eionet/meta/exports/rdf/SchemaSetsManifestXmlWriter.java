package eionet.meta.exports.rdf;

import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import java.io.OutputStream;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Nikolaos Nakas
 */
public class SchemaSetsManifestXmlWriter extends SchemasBaseManifestXmlWriter {

    public SchemaSetsManifestXmlWriter(OutputStream out, ISchemaService schemaService) {
        super(out, schemaService);
    }

    @Override
    protected void writeDocumentBody(XMLStreamWriter writer) throws XMLStreamException, ServiceException {
        SchemaSetFilter schemaSetFilter = new SchemaSetFilter();
        schemaSetFilter.setUsePaging(false);
        SchemaSetsResult schemaSetsResult = this.getSchemaService().searchSchemaSets(schemaSetFilter);
        
        for (SchemaSet ss : schemaSetsResult.getList()) {
            writer.writeStartElement(DD_NS_PREFIX, "SchemaSet", DD_NS);
            writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "about", "schemaset/" + ss.getIdentifier());

            writer.writeStartElement(RDFS_NS, "label");
            writer.writeCharacters(ss.getNameAttribute());
            writer.writeEndElement();

            List<Schema> schemas = this.getSchemaService().listSchemaSetSchemas(ss.getId());

            for (Schema s : schemas) {
                if (!s.isOtherDocument()) {
                    writer.writeStartElement(DD_NS_PREFIX, "hasSchema", DD_NS);
                    writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "resource", "schemas/" + ss.getIdentifier() + "/" + s.getFileName());
                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
        }
    }
    
}
