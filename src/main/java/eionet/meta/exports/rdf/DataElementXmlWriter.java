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

import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eionet.meta.dao.domain.DataElement;
import eionet.util.StringEncoder;

/**
 *
 * Data Element XML writer.
 *
 * @author Kaido Laine
 */
public class DataElementXmlWriter {
    /** RDF encoding. */
    private static final String ENCODING = "UTF-8";

    /** RDF namespace prefix. */
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** RDFS namespace prefix. */
    private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";

    /** XML namespace prefix. */
    private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    /**
     * XMLWriter to write XML to.
     */
    private XMLStreamWriter writer = null;

    /**
     * Class constructor.
     *
     * @param out output stream
     * @throws XMLStreamException if writing fails
     */
    public DataElementXmlWriter(OutputStream out) throws XMLStreamException {
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
    }

    /**
     * Writes xml start.
     * @param contextRoot context root URI
     * @throws XMLStreamException if writing fails
     */
    public void writeXmlStart(String contextRoot) throws XMLStreamException {
        writer.writeStartDocument(ENCODING, "1.0");
        writer.writeCharacters("\n");

        writer.setPrefix("rdf", RDF_NS);
        writer.setPrefix("rdfs", RDFS_NS);

        writer.writeStartElement("rdf", "RDF", RDF_NS);

        writer.writeNamespace("rdf", RDF_NS);
        writer.writeNamespace("rdfs", RDFS_NS);

        writer.writeAttribute("xml", XML_NS, "base", StringEncoder.encodeToIRI(contextRoot));

    }

    /**
     * Writes closing tags of XML.
     *
     * @throws XMLStreamException if writing fails
     */
    public void writeXmlEnd() throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeEndElement(); // End rdf:RDF
        writer.writeCharacters("\n");
    }

    /**
     * composes a rdf:Property element from the dataelement.
     * @param element Data element
     * @throws XMLStreamException if fail
     */
    private void writeDataElementXml(DataElement element) throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeStartElement(RDF_NS, "Property");
        writer.writeAttribute(RDF_NS , "about", element.getIdentifier());

        writer.writeCharacters("\n");
        writer.writeStartElement(RDFS_NS, "label");
        writer.writeCharacters(element.getName());
        writer.writeEndElement(); //rdfs:label
        writer.writeCharacters("\n");
        writer.writeEndElement(); //rdf:Property
    }

    /**
     * writes common elements to RDF output.
     * @param elements list of elements
     * @throws XMLStreamException if writing fails
     */
    private void writeDataelements(List<DataElement> elements) throws XMLStreamException {
        for (DataElement element : elements) {
            writeDataElementXml(element);
        }
    }
    /**
     * Writes full RDF.
     * @param contextRoot context root URI
     * @param dataElements collection of common data elements
     * @throws XMLStreamException if writing fails
     */
    public void writeRDFXml(String contextRoot,  List<DataElement> dataElements) throws XMLStreamException {

        writeXmlStart(contextRoot);
        writeDataelements(dataElements);
        writeXmlEnd();
    }
}
