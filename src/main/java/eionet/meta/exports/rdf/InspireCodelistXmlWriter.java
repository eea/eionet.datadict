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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Kaido Laine
 */

package eionet.meta.exports.rdf;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.DAOUtils;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 * Vocabulary XML Writer in INSPIRE codelist format.
 *
 * @author Kaido Laine
 */
public class InspireCodelistXmlWriter {

    /**
     * Vocabulary the INSPIRE output is created from.
     */
    private VocabularyFolder vocabulary;

    /**
     * vocabulary cont4xt Root.
     */
    private String contextRoot;

    /**
     * XML encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * XMLWriter to write XML to.
     */
    private XMLStreamWriter writer = null;

    /**
     * inspire no extensibility.
     */
    private static final String INSPIRE_EXTENSIBILITY_NONE = "http://inspire.ec.europa.eu/registry/extensibility/none";

    /**
     * valid status in inspire.
     */
    private static final String INSPIRE_STATUS_VALID = "http://inspire.ec.europa.eu/registry/status/valid";

    /**
     * Obsolete status in inspire.
     */
    private static final String INSPIRE_STATUS_OBSOLETE = "http://inspire.ec.europa.eu/registry/status/retired";

    /**
     * not released status in inspire.
     */
    private static final String INSPIRE_STATUS_PUBLICDRAFT = "http://inspire.ec.europa.eu/registry/status/submitted";

    /**
     * INSPIRE XSD location.
     */
    private static final String INSPIRE_XSD_LOCATION = "http://inspire.ec.europa.eu/draft-schemas/registry/0.3/codelist.xsd";

    /**
     * XSI Namespace.
     */
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * default namespaces that are present in INSPIRE.
     */
    private static final HashMap<String, String> INSIPIRE_NAMESPACES = new HashMap<String, String>();

    /** init namespaces hash. */
    static {
        INSIPIRE_NAMESPACES.put("dc", "http://purl.org/dc/elements/1.1/");
        INSIPIRE_NAMESPACES.put("xlink", "http://www.w3.org/1999/xlink");
        INSIPIRE_NAMESPACES.put("xs", "http://www.w3.org/2001/XMLSchema");
        INSIPIRE_NAMESPACES.put("xsi", XSI_NAMESPACE);
        INSIPIRE_NAMESPACES.put("xsd", "http://www.w3.org/2001/XMLSchema");
    }

    /**
     * Inits INSPIRE XML writer.
     *
     * @param out
     *            output stream
     * @param voc
     *            Vocabulary
     * @param ctx
     *            context URL for DD
     * @throws XMLStreamException
     *             if streaming fails
     */
    public InspireCodelistXmlWriter(OutputStream out, VocabularyFolder voc, String ctx) throws XMLStreamException {
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
        vocabulary = voc;
        contextRoot = ctx;
    }

    /**
     * Writes xml output.
     *
     * @throws XMLStreamException
     *             if writing does not succeed
     */
    public void writeXml() throws XMLStreamException {
        writeXmlStart();
        writeVocabularyXml();
        writeXmlEnd();
    }

    /**
     * Writes vocabulary in xml format.
     *
     * @throws XMLStreamException
     *             if wirting fails
     */
    public void writeVocabularyXml() throws XMLStreamException {

        writeLabelEN(vocabulary.getLabel());

        // definition not required
        String definition = DAOUtils.getVocabularyAttributeByName(vocabulary, "definition");
        if (StringUtils.isNotBlank(definition)) {
            writer.writeCharacters("\n");
            writer.writeStartElement("definition");
            writer.writeAttribute("xml:lang", "en");

            writer.writeCharacters(definition);
            writer.writeEndElement();
        }

        // description
        RegStatus status = vocabulary.getRegStatus();
        String statusLabel = "";
        writer.writeCharacters("\n");
        writer.writeStartElement("status");
        if (status.equals(RegStatus.PUBLIC_DRAFT)) {
            writer.writeAttribute("id", INSPIRE_STATUS_PUBLICDRAFT);
            statusLabel = "Submitted";
        } else if (status.equals(RegStatus.RELEASED)) {
            // if it has all concepts obsolete then it is not valid:
            if (!DAOUtils.anyConceptValid(vocabulary)) {
                writer.writeAttribute("id", INSPIRE_STATUS_OBSOLETE);
                statusLabel = "Retired";
            } else {
                writer.writeAttribute("id", INSPIRE_STATUS_VALID);
                statusLabel = "Valid";
            }
        }

        writeLabelEN(statusLabel);
        writer.writeEndElement(); // status

        // extensiblity (default NONE)
        writer.writeCharacters("\n");
        writer.writeStartElement("extensibility");
        writer.writeAttribute("id", INSPIRE_EXTENSIBILITY_NONE);
        writeLabelEN("None");
        writer.writeEndElement(); // extensibility

        writeRegisterElement();

        // TODO - add theme and applicationschema
        /*
         * //theme writer.writeCharacters("\n"); writer.writeStartElement("theme"); writer.writeAttribute("id",
         * generateVocabularyID()); writeLabelEN(vocabulary.getLabel()); writer.writeCharacters("\n"); writer.writeEndElement();
         * //theme //applicationschema writer.writeCharacters("\n"); writer.writeStartElement("applicationschema");
         * writer.writeAttribute("id", generateVocabularySetID()); writeLabelEN(vocabulary.getFolderLabel());
         * writer.writeCharacters("\n"); writer.writeEndElement(); //applicationschema
         */
        writeConcepts();
    }

    /**
     * writes XML start element.
     *
     * @throws XMLStreamException
     *             if writing fails
     */
    public void writeXmlStart() throws XMLStreamException {
        writer.writeStartDocument(ENCODING, "1.0");
        writer.writeCharacters("\n");

        writer.writeStartElement("codelist");

        for (String prefix : INSIPIRE_NAMESPACES.keySet()) {
            String uri = INSIPIRE_NAMESPACES.get(prefix);
            writer.writeNamespace(prefix, uri);
        }

        writer.writeAttribute("xsi", XSI_NAMESPACE, "noNamespaceSchemaLocation", INSPIRE_XSD_LOCATION);
        writer.writeAttribute("id", generateVocabularyID());

    }

    /**
     * Writes closing tags of XML.
     *
     * @throws XMLStreamException
     *             if writing fails
     */
    public void writeXmlEnd() throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    /**
     * ID attribute value.
     *
     * @return vocabulary URI
     */
    private String generateVocabularyID() {
        return contextRoot + "/vocabulary/" + vocabulary.getFolderName() + "/" + vocabulary.getIdentifier();
    }

    /**
     * vocabulary SET value for an ID.
     *
     * @return vocabulary set URI
     */
    private String generateVocabularySetID() {
        return contextRoot + "/vocabulary/" + vocabulary.getFolderName();
    }

    /**
     * Writes label element in xml:lang = "EN".
     *
     * @param labelText
     *            text to write
     * @throws javax.xml.stream.XMLStreamException
     *             if writing fails
     */
    private void writeLabelEN(String labelText) throws XMLStreamException {
        writer.writeCharacters("\n");
        writer.writeStartElement("label");
        writer.writeAttribute("xml:lang", "en");
        writer.writeCharacters(labelText);
        writer.writeEndElement();
    }

    /**
     * Writes concepts array.
     *
     * @throws XMLStreamException
     *             if writing fails
     */
    private void writeConcepts() throws XMLStreamException {
        List<VocabularyConcept> concepts = vocabulary.getConcepts();
        boolean hasConcepts = concepts != null && concepts.size() > 0;
        if (hasConcepts) {
            writer.writeCharacters("\n");
            writer.writeStartElement("containeditems");

            for (VocabularyConcept concept : concepts) {
                writer.writeCharacters("\n");
                writer.writeStartElement("value");
                writer.writeAttribute("id", generateVocabularyID() + "/" + concept.getIdentifier());
                writeLabelEN(concept.getLabel());

                if (StringUtils.isNotBlank(concept.getDefinition())) {
                    writer.writeCharacters("\n");
                    writer.writeStartElement("definition");
                    writer.writeAttribute("xml:lang", "en");
                    writer.writeCharacters(concept.getDefinition());
                    writer.writeEndElement(); // definition
                }

                writer.writeCharacters("\n");
                writer.writeStartElement("status");

                if (concept.getStatus().isValid()) {
                    writer.writeAttribute("id", INSPIRE_STATUS_VALID);
                    writeLabelEN("Valid");
                } else {
                    writer.writeAttribute("id", INSPIRE_STATUS_OBSOLETE);
                    writeLabelEN("Invalid");
                }
                writer.writeCharacters("\n");
                writer.writeEndElement(); // status

                writeRegisterElement();

                // TODO theme and applicationschema if complex attrs are implemented
                /*
                 * //theme writer.writeCharacters("\n"); writer.writeStartElement("theme"); writer.writeAttribute("id",
                 * generateVocabularyID()); writeLabelEN(vocabulary.getLabel()); writer.writeCharacters("\n");
                 * writer.writeEndElement(); //theme
                 *
                 * //applicationschema writer.writeCharacters("\n"); writer.writeStartElement("applicationschema");
                 * writer.writeAttribute("id", generateVocabularySetID()); writeLabelEN(vocabulary.getFolderLabel());
                 * writer.writeCharacters("\n"); writer.writeEndElement(); //applicationschema
                 */
                writer.writeCharacters("\n");
                writer.writeStartElement("codelist");
                writer.writeAttribute("id", generateVocabularyID());
                writeLabelEN(vocabulary.getLabel());
                writer.writeCharacters("\n");
                writer.writeEndElement(); // codelist

                writer.writeCharacters("\n");
                writer.writeEndElement();
            }

            writer.writeCharacters("\n");
            writer.writeEndElement(); // containeditems
        }
    }

    /**
     * Register element is same for all entities vocabulary and concepts.
     *
     * @throws XMLStreamException
     *             if streaming fails
     */
    private void writeRegisterElement() throws XMLStreamException {
        // register
        writer.writeCharacters("\n");
        writer.writeStartElement("register");
        writer.writeAttribute("id", this.contextRoot + "/vocabularies");
        writeLabelEN("Data Dictionary Vocabularies");
        writer.writeCharacters("\n");
        writer.writeStartElement("registry");
        writer.writeAttribute("id", this.contextRoot);
        writeLabelEN("Eionet Data Dictionary");

        writer.writeCharacters("\n");
        writer.writeEndElement(); // registry

        writer.writeCharacters("\n");
        writer.writeEndElement(); // register
    }
}
