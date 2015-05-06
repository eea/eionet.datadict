package eionet.meta.exports.rdf;

import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.io.OutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A base class for schema-based RDF output.
 * 
 * @author Nikolaos Nakas
 */
public abstract class SchemasBaseManifestXmlWriter {
    
    public static final String ENCODING = "UTF-8";
    
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String DD_NS = "http://dd.eionet.europa.eu/schema.rdf#";
    public static final String CR_NS = "http://cr.eionet.europa.eu/ontologies/contreg.rdf#";
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    
    public static final String RDF_NS_PREFIX = "rdf";
    public static final String RDFS_NS_PREFIX = "rdfs";
    public static final String DD_NS_PREFIX = "dd";
    public static final String CR_NS_PREFIX = "cr";
    public static final String XML_NS_PREFIX = "xml";
    
    private final OutputStream out;
    private final ISchemaService schemaService;
    
    /**
     * 
     * @param out The destination stream. As per javax.xml.stream.XMLStreamWriter, 
     * this object is not responsible for closing the stream.
     * 
     * @param schemaService The business object that provides schema-related data.
     */
    public SchemasBaseManifestXmlWriter(OutputStream out, ISchemaService schemaService) {
        if (out == null) throw new IllegalArgumentException();
        if (schemaService == null) throw new IllegalArgumentException();
        
        this.out = out;
        this.schemaService = schemaService;
    }
    
    public void writeManifestXml() throws XMLStreamException, ServiceException {
        XMLStreamWriter writer = null;
        
        try {
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
            this.writeDocumentStart(writer);
            this.writeDocumentBody(writer);
            writer.writeEndDocument();
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    protected ISchemaService getSchemaService() {
        return this.schemaService;
    }
    
    protected abstract void writeDocumentBody(XMLStreamWriter writer) throws XMLStreamException, ServiceException;
    
    private void writeDocumentStart(XMLStreamWriter writer) throws XMLStreamException {
        String webAppUrl = Props.getRequiredProperty(PropsIF.DD_URL);

        writer.writeStartDocument(ENCODING, "1.0");

        writer.setPrefix(RDF_NS_PREFIX, RDF_NS);
        writer.setPrefix(RDFS_NS_PREFIX, RDFS_NS);
        writer.setPrefix(DD_NS_PREFIX, DD_NS);
        writer.setPrefix(CR_NS_PREFIX, CR_NS);

        writer.writeStartElement(RDF_NS_PREFIX, "RDF", RDF_NS);
        writer.writeNamespace(RDF_NS_PREFIX, RDF_NS);
        writer.writeNamespace(RDFS_NS_PREFIX, RDFS_NS);
        writer.writeNamespace(DD_NS_PREFIX, DD_NS);
        writer.writeNamespace(CR_NS_PREFIX, CR_NS);
        writer.writeAttribute(XML_NS_PREFIX, XML_NS, "base", webAppUrl);
    }
}
