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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import eionet.meta.DsTable;
import eionet.meta.dao.domain.DataElement;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * VoID xml writer.
 *
 * @author Juhan Voolaid
 */
public class VoIDXmlWriter {

    private static final String ENCODING = "UTF-8";
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDF_NS_PREFIX = "rdf";
    private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    private static final String RDFS_NS_PREFIX = "rdfs";
    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final String OWL_NS_PREFIX = "owl";
    private static final String DCT_NS = "http://purl.org/dc/terms/";
    private static final String DCT_NS_PREFIX = "dct";
    private static final String VOID_NS = "http://rdfs.org/ns/void#";
    private static final String VOID_NS_PREFIX = "void";
    private static final String ROOT_ELEMENT = "RDF";

    private String contextRoot;

    /**
     * XMLWriter to write XML to.
     */
    private XMLStreamWriter writer = null;

    /**
     *
     * Class constructor.
     *
     * @param out
     * @param contextRoot
     * @throws XMLStreamException
     */
    public VoIDXmlWriter(OutputStream out, String contextRoot) throws XMLStreamException {
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
        this.contextRoot = contextRoot;
    }

    /**
     * Writes data element's VoID xml.
     *
     * @param dataElements
     * @throws XMLStreamException
     */
    public void writeVoIDXml(List<DataElement> dataElements, Vector tables) throws XMLStreamException {
        String dataElementsBaseUri = Props.getRequiredProperty(PropsIF.RDF_DATAELEMENTS_BASE_URI);
        String tablesBaseUri = Props.getRequiredProperty(PropsIF.RDF_TABLES_BASE_URI);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        writer.writeStartDocument(ENCODING, "1.0");

        writer.writeStartElement(RDF_NS_PREFIX, ROOT_ELEMENT, RDF_NS);
        writer.writeNamespace(RDF_NS_PREFIX, RDF_NS);
        writer.writeNamespace(RDFS_NS_PREFIX, RDFS_NS);
        writer.writeNamespace(OWL_NS_PREFIX, OWL_NS);
        writer.writeNamespace(DCT_NS_PREFIX, DCT_NS);
        writer.writeNamespace(VOID_NS_PREFIX, VOID_NS);

        for (int i = 0; tables != null && i < tables.size(); i++) {

            DsTable table = (DsTable) tables.get(i);
            String tableId = table.getID();
            String tableRdfUrl = MessageFormat.format(tablesBaseUri, Integer.parseInt(tableId));

            writer.writeStartElement(VOID_NS_PREFIX, "Dataset", VOID_NS);
            writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "ID", "TBL" + tableId);

            writer.writeStartElement(RDFS_NS_PREFIX, "label", RDFS_NS);
            writer.writeCharacters(table.getName());
            writer.writeEndElement();

            writer.writeStartElement(VOID_NS_PREFIX, "dataDump", VOID_NS);
            writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "resource", tableRdfUrl);
            writer.writeEndElement();

            if (StringUtils.isNotEmpty(table.getDstDate())) {
                Long milliseconds = Long.parseLong(table.getDstDate());
                writer.writeStartElement(DCT_NS_PREFIX, "modified", DCT_NS);
                writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "datatype", "http://www.w3.org/2001/XMLSchema#dateTime");
                writer.writeCharacters(dateFormat.format(new Date(milliseconds)));
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }

        for (DataElement de : dataElements) {
            String dataelementUri = MessageFormat.format(dataElementsBaseUri, de.getId());

            writer.writeStartElement(VOID_NS_PREFIX, "Dataset", VOID_NS);
            writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "about", StringUtils.substringBeforeLast(dataelementUri, "/rdf"));

            writer.writeStartElement(RDFS_NS_PREFIX, "label", RDFS_NS);
            writer.writeCharacters(de.getShortName());
            writer.writeEndElement();

            writer.writeStartElement(VOID_NS_PREFIX, "dataDump", VOID_NS);
            writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "resource", dataelementUri);
            writer.writeEndElement();

            if (de.getModified() != null) {
                writer.writeStartElement(DCT_NS_PREFIX, "modified", DCT_NS);
                writer.writeAttribute(RDF_NS_PREFIX, RDF_NS, "datatype", "http://www.w3.org/2001/XMLSchema#dateTime");
                writer.writeCharacters(dateFormat.format(de.getModified()));
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }

        writer.writeEndDocument();
    }
}
