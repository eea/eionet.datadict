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
import java.util.Calendar;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.VocabularyOutputHelper;
import eionet.meta.service.data.SiteCode;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.StringEncoder;

/**
 * Vocabulary RDF-XML writer.
 *
 * @author Juhan Voolaid
 */
public class VocabularyXmlWriter {

    /** */
    public static final String OWN_STATUS_VOCABULARY_URI = VocabularyFolder.OWN_VOCABULARIES_FOLDER_URI + "/"
            + Props.getRequiredProperty(PropsIF.DD_OWN_STATUS_VOCABULARY_IDENTIFIER);

    /** Encoding. */
    private static final String ENCODING = "UTF-8";

    /** XMLWriter to write XML to. */
    private XMLStreamWriter writer;

    /**
     * Default constructor.
     *
     * @param out Output stream.
     * @throws XMLStreamException If writing fails.
     */
    public VocabularyXmlWriter(OutputStream out) throws XMLStreamException {

        if (out == null) {
            throw new IllegalArgumentException("Output stream must not be null!");
        }

        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
    }

    /**
     * Writes rdf output to stream for one vocabulary folder.
     *
     * @param commonElemsUri
     *            uri for common elements
     * @param folderContextRoot
     *            IRI for folder
     * @param contextRoot
     *            folder context IRI
     * @param vocabularyFolder
     *            vocabulary
     * @param vocabularyConcepts
     *            concepts in the vocabulary
     * @param rdfNamespaces
     *            Namespaces that are used in the RDF entities
     * @throws XMLStreamException
     *             if streaming fails
     */
    public void writeRDFXml(String commonElemsUri, String folderContextRoot, String contextRoot, VocabularyFolder vocabularyFolder,
            List<? extends VocabularyConcept> vocabularyConcepts, List<RdfNamespace> rdfNamespaces) throws XMLStreamException {

        writeXmlStart(vocabularyFolder.isSiteCodeType(), commonElemsUri, contextRoot, rdfNamespaces);

        writeVocabularyFolderXml(folderContextRoot, contextRoot, vocabularyFolder, vocabularyConcepts);

        writeXmlEnd();
    }

    /**
     * Writes start of XML.
     *
     * @param siteCodeType
     *            if true it is a site code vocabulary
     * @param commonElemsUri
     *            uri for common elements
     * @param contextRoot
     *            IRI for context
     * @param nameSpaces
     *            namespaces to be written to the header
     * @throws XMLStreamException
     *             if writing does not succeed
     */
    public void writeXmlStart(boolean siteCodeType, String commonElemsUri, String contextRoot, List<RdfNamespace> nameSpaces)
            throws XMLStreamException {
        writer.writeStartDocument(ENCODING, "1.0");
        writer.writeCharacters("\n");

        // default namespaces
        for (String prefix : VocabularyOutputHelper.LinkedDataNamespaces.DEFAULT_NAMESPACES.keySet()) {
            writer.setPrefix(prefix, VocabularyOutputHelper.LinkedDataNamespaces.DEFAULT_NAMESPACES.get(prefix));
        }

        if (siteCodeType) {
            writer.setPrefix("dd", VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS);
        }

        writer.writeStartElement("rdf", "RDF", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS);
        for (String prefix : VocabularyOutputHelper.LinkedDataNamespaces.DEFAULT_NAMESPACES.keySet()) {
            writer.writeNamespace(prefix, VocabularyOutputHelper.LinkedDataNamespaces.DEFAULT_NAMESPACES.get(prefix));
        }

        for (RdfNamespace ns : nameSpaces) {
            if (!VocabularyOutputHelper.LinkedDataNamespaces.DEFAULT_NAMESPACES.keySet().contains(ns.getPrefix())) {
                writer.writeNamespace(ns.getPrefix(), ns.getUri());
            }
        }

        if (siteCodeType) {
            writer.writeNamespace("dd", VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS);
        }
        writer.writeDefaultNamespace(commonElemsUri);
        writer.writeAttribute("xml", VocabularyOutputHelper.LinkedDataNamespaces.XML_NS, "base", VocabularyOutputHelper.escapeIRI(contextRoot));
    }

    /**
     * Writes dcterms:Collection resource of the folder.
     *
     * @param folderContext
     *            Folder context IRI
     * @param folder
     *            Folder for the vocabularies
     * @param vocabularies
     *            concepts collection in the vocabulary
     * @throws XMLStreamException
     *             if rdf output fails
     */
    public void writeFolderXml(String folderContext, Folder folder, List<? extends VocabularyFolder> vocabularies) throws XMLStreamException {
        // dctype:Collection for Folder:
        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DCTYPE_NS, "Collection");
        writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "about", StringEncoder.encodeToIRI(folderContext));

        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.RDFS_NS, "label");
        writer.writeCharacters(folder.getLabel());
        writer.writeEndElement();

        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "notation");
        writer.writeCharacters(folder.getIdentifier());
        writer.writeEndElement();

        // hasPart relations of concepts:
        for (VocabularyFolder v : vocabularies) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement(VocabularyOutputHelper.LinkedDataNamespaces.DCTERMS_NS, "hasPart");
            writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource",
                    StringEncoder.encodeToIRI(folderContext + v.getIdentifier() + "/"));
        }
        writer.writeCharacters("\n");
        writer.writeEndElement(); // End Collection

    }

    /**
     * Writes closing tags of XML.
     *
     * @throws XMLStreamException
     *             if writing fails
     */
    public void writeXmlEnd() throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeEndElement(); // End rdf:RDF
        writer.writeCharacters("\n");
        writer.flush();
    }

    /**
     * Writes vocabulary folder XML.
     *
     * @param folderContextRoot
     *            vocabulary base uri
     * @param vocabularyContextRoot
     *            vocabulary set base uri
     * @param vocabularyFolder
     *            vocabulary
     * @param vocabularyConcepts
     *            concepts of vocabulary
     * @throws XMLStreamException
     *             when an error occurs during write operation
     */
    public void writeVocabularyFolderXml(String folderContextRoot, String vocabularyContextRoot, VocabularyFolder vocabularyFolder,
            List<? extends VocabularyConcept> vocabularyConcepts) throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "ConceptScheme");
        writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "about", StringEncoder.encodeToIRI(vocabularyContextRoot));

        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.RDFS_NS, "label");
        writer.writeCharacters(vocabularyFolder.getLabel());
        writer.writeEndElement();

        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "notation");
        writer.writeCharacters(vocabularyFolder.getIdentifier());
        writer.writeEndElement();

        writer.writeCharacters("\n");
        writer.writeEmptyElement(VocabularyOutputHelper.LinkedDataNamespaces.DCTERMS_NS, "isPartOf");
        writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource", StringEncoder.encodeToIRI(folderContextRoot));

        writer.writeCharacters("\n");
        writer.writeEndElement(); // End ConceptScheme

        for (VocabularyConcept vc : vocabularyConcepts) {

            writer.writeCharacters("\n");
            writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "Concept");
            writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "about",
                    StringEncoder.encodeToIRI(vocabularyContextRoot + vc.getIdentifier()));

            if (StringUtils.isNotEmpty(vc.getNotation())) {
                writer.writeCharacters("\n");
                writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "notation");
                writer.writeCharacters(vc.getNotation());
                writer.writeEndElement();
            }

            writer.writeCharacters("\n");
            writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "prefLabel");
            writer.writeCharacters(vc.getLabel());
            writer.writeEndElement();

            if (StringUtils.isNotEmpty(vc.getDefinition())) {
                writer.writeCharacters("\n");
                writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "definition");
                writer.writeCharacters(vc.getDefinition());
                writer.writeEndElement();
            }

            // Write concept status if it's not null;
            StandardGenericStatus conceptStatus = vc.getStatus();
            if (conceptStatus != null) {
                writer.writeCharacters("\n");
                writer.writeEmptyElement(VocabularyOutputHelper.LinkedDataNamespaces.ADMS_NS, "status");
                writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource",
                        StringEncoder.encodeToIRI(OWN_STATUS_VOCABULARY_URI + "/" + conceptStatus.getIdentifier()));
            }

            writer.writeCharacters("\n");
            writer.writeEmptyElement(VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS, "inScheme");
            writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource",
                    StringEncoder.encodeToIRI(vocabularyContextRoot));

            writeBoundElements(vocabularyContextRoot, vc.getElementAttributes());

            writer.writeCharacters("\n");
            writer.writeEndElement();
        }
    }

    /**
     * Write bound elements to RDF.
     *
     * @param contextRoot
     *            contex root
     * @param elements
     *            elements list
     * @throws XMLStreamException
     *             if writing fails
     */
    private void writeBoundElements(String contextRoot, List<List<DataElement>> elements) throws XMLStreamException {
        if (elements != null) {
            for (List<DataElement> elems : elements) {
                if (elems != null) {
                    for (DataElement elem : elems) {
                        writer.writeCharacters("\n");
                        if (elem.isRelationalElement()) {
                            writer.writeEmptyElement(elem.getIdentifier());
                            writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource",
                                    StringEncoder.encodeToIRI(elem.getRelatedConceptUri()));
                        } else if (StringUtils.isNotEmpty(elem.getAttributeValue())) {
                            if (StringUtils.isNotEmpty(elem.getRelatedConceptUri()) && StringUtils.isNotEmpty(elem.getDatatype())
                                    && elem.getDatatype().equalsIgnoreCase("reference")) {
                                writer.writeEmptyElement(elem.getIdentifier());
                                writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource",
                                        elem.getRelatedConceptUri());
                            } else {
                                writer.writeStartElement(elem.getIdentifier());
                                if (StringUtils.isNotEmpty(elem.getAttributeLanguage())) {
                                    writer.writeAttribute("xml", VocabularyOutputHelper.LinkedDataNamespaces.XML_NS, "lang",
                                            elem.getAttributeLanguage());
                                }
                                if (StringUtils.isNotEmpty(elem.getDatatype()) && !elem.getDatatype().equalsIgnoreCase("string")) {
                                    writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "datatype",
                                            Rdf.getXmlType(elem.getDatatype()));
                                }
                                writer.writeCharacters(elem.getAttributeValue());
                                writer.writeEndElement();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes site code specific properties to RDF output.
     *
     * @param sc
     *            sitecode
     * @throws XMLStreamException
     *             if export fails
     */
    private void writeSiteCodeData(SiteCode sc) throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "siteCode");
        writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "datatype", "http://www.w3.org/2001/XMLSchema#int");
        writer.writeCharacters(sc.getIdentifier());
        writer.writeEndElement();

        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "siteName");
        writer.writeCharacters(sc.getLabel());
        writer.writeEndElement();

        writer.writeCharacters("\n");
        writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "status");
        writer.writeCharacters(sc.getSiteCodeStatus().name());
        writer.writeEndElement();

        if (StringUtils.isNotEmpty(sc.getCountryCode())) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "countryAllocated");
            writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource",
                    "http://rdfdata.eionet.europa.eu/eea/countries/" + sc.getCountryCode());
        }

        if (sc.getDateCreated() != null) {
            Calendar created = Calendar.getInstance();
            created.setTime(sc.getDateCreated());
            writer.writeCharacters("\n");
            writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "yearCreated");
            writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "datatype", "http://www.w3.org/2001/XMLSchema#gYear");
            writer.writeCharacters(Integer.toString(created.get(Calendar.YEAR)));
            writer.writeEndElement();
        }

        if (StringUtils.isNotEmpty(sc.getYearsDeleted())) {
            writer.writeCharacters("\n");
            writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "yearsDeleted");
            writer.writeCharacters(sc.getYearsDeleted());
            writer.writeEndElement();
        }

        if (StringUtils.isNotEmpty(sc.getYearsDisappeared())) {
            writer.writeCharacters("\n");
            writer.writeStartElement(VocabularyOutputHelper.LinkedDataNamespaces.DD_SCHEMA_NS, "yearsDisappeared");
            writer.writeCharacters(sc.getYearsDisappeared());
            writer.writeEndElement();
        }

        writer.writeCharacters("\n");
        writer.writeEmptyElement(VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "type");
        writer.writeAttribute("rdf", VocabularyOutputHelper.LinkedDataNamespaces.RDF_NS, "resource", "http://dd.eionet.europa.eu/schema.rdf#SiteCode");

    }
}
