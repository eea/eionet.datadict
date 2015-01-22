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
import eionet.meta.exports.VocabularyOutputHelper;

/**
 * Vocabulary CSV output helper.
 *
 * @author Juhan Voolaid
 */
public final class VocabularyCSVOutputHelper {

    /**
     * how many values are taken from vocabulary concept.
     */
    public static final int CONCEPT_ENTRIES_COUNT = 6;
    /**
     * index of URI in header list.
     */
    public static final int URI_INDEX = 0;
    /**
     * index of label in header list.
     */
    public static final int LABEL_INDEX = 1;
    /**
     * index of definition in header list.
     */
    public static final int DEFINITION_INDEX = 2;
    /**
     * index of notation in header list.
     */
    public static final int NOTATION_INDEX = 3;
    /**
     * index of start date in header list.
     */
    public static final int STATUS_INDEX = 4;
    /**
     * index of end date in header list.
     */
    public static final int ACCEPTED_DATE_INDEX = 5;
    
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

        List<String> toBeAddedToHeader = new ArrayList<String>();
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
            entries[URI_INDEX] = uriPrefix + c.getIdentifier();
            entries[LABEL_INDEX] = c.getLabel();
            entries[DEFINITION_INDEX] = c.getDefinition();
            entries[NOTATION_INDEX] = c.getNotation();
            entries[STATUS_INDEX] = c.getStatus().getLabel();
            entries[ACCEPTED_DATE_INDEX] = c.getAcceptedDate() != null ? dateFormatter.format(c.getAcceptedDate()) : "";

            // add extra fields
            for (Triple<String, String, Integer> row : attributesMeta) {
                String elemName = row.getLeft();

                attributeElems = getDataElementValuesByNameAndLang(elemName, row.getCentral(), c.getElementAttributes());

                int sizeOfAttributeElems = 0;
                if (attributeElems != null) {
                    sizeOfAttributeElems = attributeElems.size();
                    for (int j = 0; j < sizeOfAttributeElems; j++) {
                        DataElement e = attributeElems.get(j);
                        if (e.isRelationalElement()) {
                            value = e.getRelatedConceptUri();
                        } else if (StringUtils.isNotEmpty(e.getRelatedConceptIdentifier())
                                && StringUtils.isNotEmpty(e.getDatatype()) && e.getDatatype().equalsIgnoreCase("reference")) {
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
        byte[] bomByteArray = VocabularyOutputHelper.getBomByteArray();
        for (byte b : bomByteArray) {
            out.write(b);
        }
    }

    /**
     * Adds pre-defined entries to the array.
     *
     * @param entries
     *            array for CSV output
     */
    public static void addFixedEntryHeaders(String[] entries) {
        entries[URI_INDEX] = "URI";
        entries[LABEL_INDEX] = "Label";
        entries[DEFINITION_INDEX] = "Definition";
        entries[NOTATION_INDEX] = "Notation";
        entries[STATUS_INDEX] = "Status";
        entries[ACCEPTED_DATE_INDEX] = "AcceptedDate";
    }

}
