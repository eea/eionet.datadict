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

package eionet.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVWriter;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;

/**
 * Vocabulary CSV output helper.
 * 
 * @author Juhan Voolaid
 */
public final class VocabularyCSVOutputHelper {
    
    /**
     * how many values are takend from vocabulary concept.
     */
    public static final int CONCEPT_ENTRIES_COUNT = 6;
    
    public static final byte[] BOM_BYTE_ARRAY = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    /**
     * Prevent public initialization.
     */
    private VocabularyCSVOutputHelper() {

    }

    /**
     * Writes CSV to output stream.
     * 
     * @param out
     *            outputstream
     * @param uriPrefix
     *            uri prefix for teh element identifiers
     * @param folderContextRoot
     *            parent vocabulary folder root for related identifiers
     * @param concepts
     *            list of vocabulary concepts
     * @param attributesMeta
     *            list of field names to the CSV header row
     * @throws IOException
     *             if error in I/O
     */
    public static void writeCSV(OutputStream out, String uriPrefix, String folderContextRoot, List<VocabularyConcept> concepts,
            List<Triple<String, String, Integer>> attributesMeta) throws IOException {

        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
        addBOM(out);

        ArrayList<String> toBeAddedToHeader = new ArrayList<String>();
        for (Triple<String, String, Integer> row : attributesMeta) {
            String lang = "";
            if (StringUtils.isNotEmpty(row.getCentral())) {
                lang = "@" + row.getCentral();
            }
            int numOfElements = row.getRight();
            for (int i = 0; i < numOfElements; i++) {
                toBeAddedToHeader.add(row.getLeft() + lang);
            }
        }

        String[] entries = new String[CONCEPT_ENTRIES_COUNT + toBeAddedToHeader.size()];
        addFixedEntryHeaders(entries);

        for (int i = 0; i < toBeAddedToHeader.size(); i++) {
            entries[i + CONCEPT_ENTRIES_COUNT] = toBeAddedToHeader.get(i);
        }

        CSVWriter writer = new CSVWriter(osw, ',');
        writer.writeNext(entries);

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        for (VocabularyConcept c : concepts) {
            int elemPos = 0;
            String value = "";
            List<DataElement> attributeElems = null;

            // add fixed entries
            entries = new String[CONCEPT_ENTRIES_COUNT + toBeAddedToHeader.size()];
            entries[0] = uriPrefix + c.getIdentifier();
            entries[1] = c.getLabel();
            entries[2] = c.getDefinition();
            entries[3] = c.getNotation();
            entries[4] = c.getCreated() != null ? dateFormatter.format(c.getCreated()) : "";
            entries[5] = c.getObsolete() != null ? dateFormatter.format(c.getObsolete()) : "";

            // add extra fields
            for (Triple<String, String, Integer> row : attributesMeta) {
                String elemName = row.getLeft();

                attributeElems = getDataElementValuesByNameAndLang(elemName, row.getCentral(), c.getElementAttributes());

                int sizeOfAttributeElems = 0;
                if (attributeElems != null) {
                    sizeOfAttributeElems = attributeElems.size();
                    for (int j = 0; j < attributeElems.size(); j++) {
                        DataElement e = attributeElems.get(j);
                        if (e.isRelationalElement()) {
                            value = folderContextRoot + e.getRelatedConceptIdentifier();
                        } else {
                            value = e.getAttributeValue();
                        }
                        // value = "\"" + value + "\"";
                        entries[CONCEPT_ENTRIES_COUNT + elemPos + j] = value;
                    }
                }

                int maximumNumberOfElements = row.getRight();
                // add missing columns
                for (int j = sizeOfAttributeElems; j < maximumNumberOfElements; j++) {
                    entries[CONCEPT_ENTRIES_COUNT + elemPos + j] = null;
                }

                elemPos += maximumNumberOfElements;
            }
            writer.writeNext(entries);
        }
        writer.close();
        osw.close();
    }

    /**
     * Writes utf-8 BOM in the given writer.
     * 
     * @param out
     *            current outputstream
     * @throws IOException
     *             if connection fails
     */
    private static void addBOM(OutputStream out) throws IOException {
        
        for (byte b : BOM_BYTE_ARRAY) {
            out.write(b);
        }

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
        for (List<DataElement> elem : elems) {
            if (elem != null && elem.size() > 0) {
                DataElement elemMeta = elem.get(0);
                if (elemMeta != null && StringUtils.equals(elemMeta.getIdentifier(),elemName)) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * finds list of data element values by name and language
     * 
     * @param elemName
     *            element name to be looked for
     * @param lang
     *            element lang to be looked for
     * @param elems
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    public static List<DataElement>
            getDataElementValuesByNameAndLang(String elemName, String lang, List<List<DataElement>> elems) {
        boolean isLangEmpty = StringUtils.isEmpty(lang);
        ArrayList<DataElement> elements = new ArrayList<DataElement>();
        for (List<DataElement> elem : elems) {
            if (elem == null || elem.size() < 1 || !StringUtils.equals(elem.get(0).getIdentifier(), elemName)) {// check first one
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
        return elements;
    }// end of method getDataElementValuesByNameAndLang

    /**
     * Adds pre-defined entries to the array.
     * If updated 
     * 
     * @param entries
     *            array for CSV output
     */
    public static void addFixedEntryHeaders(String[] entries) {
        entries[0] = "URI";
        entries[1] = "Label";
        entries[2] = "Definition";
        entries[3] = "Notation";
        entries[4] = "StartDate";
        entries[5] = "EndDate";
    }
}
