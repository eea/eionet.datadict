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
 *        TripleDev
 */

package eionet.meta.exports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.DataElement;
import eionet.util.StringEncoder;

/**
 * Vocabulary common output helper.
 *
 * @author enver
 */
public final class VocabularyOutputHelper {
    /**
     * BOM byte array length.
     */
    public static final int BOM_BYTE_ARRAY_LENGTH = 3;

    /**
     * Prevent public initialization.
     */
    private VocabularyOutputHelper() {
    }

    /**
     * finds list of data element values by name.
     *
     * @param elemName
     *            element name to be looked for
     * @param elems
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    public static List<DataElement> getDataElementValuesByName(String elemName, List<List<DataElement>> elems) {
        if (elems != null) {
            for (List<DataElement> elem : elems) {
                if (elem != null && elem.size() > 0) {
                    DataElement elemMeta = elem.get(0);
                    if (elemMeta != null && StringUtils.equals(elemMeta.getIdentifier(), elemName)) {
                        return elem;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds list of data element values by name and language.
     *
     * @param elemName
     *            element name to be looked for
     * @param lang
     *            element lang to be looked for
     * @param elems
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    public static List<DataElement> getDataElementValuesByNameAndLang(String elemName, String lang, List<List<DataElement>> elems) {
        boolean isLangEmpty = StringUtils.isEmpty(lang);
        ArrayList<DataElement> elements = new ArrayList<DataElement>();
        if (elems != null) {
            for (List<DataElement> elem : elems) {
                if (elem == null || elem.size() < 1 || !StringUtils.equals(elem.get(0).getIdentifier(), elemName)) { // check first
                                                                                                                     // one
                    continue;
                }
                for (DataElement elemMeta : elem) {
                    String elemLang = elemMeta.getAttributeLanguage();
                    if ((isLangEmpty && StringUtils.isEmpty(elemLang)) || StringUtils.equals(lang, elemLang)) {
                        elements.add(elemMeta);
                    } else if (elements.size() > 0) {
                        break;
                    }
                }
                // return elements;
            }
        }
        return elements;
    } // end of method getDataElementValuesByNameAndLang

    /**
     * Returns bom byte array.
     *
     * @return bom byte array
     */
    public static byte[] getBomByteArray() {
        return new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    } // end of method getBomByteArray

    /**
     * Escapes IRI's reserved characters in the given URL string.
     *
     * @param url
     *            is a string.
     * @return escaped URI
     */
    public static String escapeIRI(String url) {
        return StringEncoder.encodeToIRI(url);
    }

    /**
     * Inner class to hold Linked Data related definitions.
     */
    public static final class LinkedDataNamespaces {

        /**
         * RDF namespace uri.
         */
        public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        /**
         * RDFS namespace uri.
         */
        public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
        /**
         * SKOS namespace uri.
         */
        public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
        /**
         * XML namespace uri.
         */
        public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
        /**
         * OWL namespace uri.
         */
        public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
        /**
         * DCTYPE namespace prefix.
         */
        public static final String DCTYPE_NS = "http://purl.org/dc/dcmitype/";
        /**
         * DCTERMS namespace uri.
         */
        public static final String DCTERMS_NS = "http://purl.org/dc/terms/";
        /**
         * DD namespace uri.
         */
        public static final String DD_SCHEMA_NS = "http://dd.eionet.europa.eu/schema.rdf#";
        /**
         * RDF namespace prefix.
         */
        public static final String RDF = "rdf";
        /**
         * RDFS namespace prefix.
         */
        public static final String RDFS = "rdfs";
        /**
         * SKOS namespace prefix.
         */
        public static final String SKOS = "skos";
        /**
         * OWL namespace prefix.
         */
        public static final String OWL = "owl";
        /**
         * DCTYPE namespace prefix.
         */
        public static final String DCTYPE = "dctype";
        /**
         * DCTERMS namespace prefix.
         */
        public static final String DCTERMS = "dcterms";
        /**
         * default namespaces that are present in all vocabulary RDFs.
         */
        public static final HashMap<String, String> DEFAULT_NAMESPACES = new HashMap<String, String>();
        /**
         * inits default namespaces container.
         */
        static {
            DEFAULT_NAMESPACES.put(RDF, RDF_NS);
            DEFAULT_NAMESPACES.put(RDFS, RDFS_NS);
            DEFAULT_NAMESPACES.put(SKOS, SKOS_NS);
            DEFAULT_NAMESPACES.put(OWL, OWL_NS);
            DEFAULT_NAMESPACES.put(DCTYPE, DCTYPE_NS);
            DEFAULT_NAMESPACES.put(DCTERMS, DCTERMS_NS);
        }
    } // end of inner class LinkedDataNamespaces

} // end of class VocabularyOutputHelper
