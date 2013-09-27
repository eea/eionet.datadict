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
import java.util.Iterator;
import java.util.List;

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
    private static final int CONCEPT_ENTRIES_COUNT = 6;

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
            List<String> attributesMeta) throws IOException {

        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
        addBOM(out);

        String[] entries = new String[CONCEPT_ENTRIES_COUNT + attributesMeta.size()];
        addFixedEntryHeaders(entries);

        for (int i = 0; i < attributesMeta.size(); i++) {
            entries[i + CONCEPT_ENTRIES_COUNT] = attributesMeta.get(i);
        }

        CSVWriter writer = new CSVWriter(osw, ',');
        writer.writeNext(entries);

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        for (VocabularyConcept c : concepts) {
            int elemPos = 0;
            String previousName = "";
            boolean elemChanged = false;
            List<DataElement> attributeElems = null;

            entries = new String[CONCEPT_ENTRIES_COUNT + attributesMeta.size()];
            entries[0] = uriPrefix + c.getIdentifier();
            entries[1] = c.getLabel();
            entries[2] = c.getDefinition();
            entries[3] = c.getNotation();

            entries[4] = c.getCreated() != null ? dateFormatter.format(c.getCreated()) : "";
            entries[5] = c.getObsolete() != null ? dateFormatter.format(c.getObsolete()) : "";

            Iterator<DataElement> iterator = null;
            for (String elemName : attributesMeta) {

                // new element started in the header
                elemChanged = !elemName.equals(previousName);

                if (elemChanged) {
                    attributeElems = getDataElementValuesByName(elemName, c.getElementAttributes());
                    if (attributeElems != null) {
                        iterator = attributeElems.iterator();
                    }
                }

                String value = "";
                if (iterator != null && iterator.hasNext()) {
                    DataElement e = iterator.next();
                    if (e.isRelationalElement()) {
                        value = folderContextRoot + e.getRelatedConceptIdentifier();
                    } else {
                        value = e.getAttributeValue();
                    }
                }
                // value = "\"" + value + "\"";
                entries[CONCEPT_ENTRIES_COUNT + elemPos] = value;

                elemPos++;
                previousName = elemName;
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
        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

        for (byte b : bom) {
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
    private static List<DataElement> getDataElementValuesByName(String elemName, List<List<DataElement>> elems) {
        for (List<DataElement> elem : elems) {
            DataElement elemMeta = elem.get(0);
            if (elemMeta != null && elemMeta.getIdentifier().equals(elemName)) {
                return elem;
            }
        }
        return null;
    }

    /**
     * Adds pre-defined entries to the array.
     *
     * @param entries
     *            array for CSV output
     */
    private static void addFixedEntryHeaders(String[] entries) {
        entries[0] = "URI";
        entries[1] = "Label";
        entries[2] = "Definition";
        entries[3] = "Notation";
        entries[4] = "StartDate";
        entries[5] = "EndDate";
    }
}
