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
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import eionet.meta.dao.domain.VocabularyConcept;

/**
 * Vocabulary CSV output helper.
 *
 * @author Juhan Voolaid
 */
public final class VocabularyCSVOutputHelper {

    /**
     * Prevent public initialization.
     */
     private VocabularyCSVOutputHelper() {

     }

   /**
     * Writes CSV to output stream.
     *
     * @param out outputstream
     * @param uriPrefix uri prefix for teh element identifiers
     * @param concepts list of vocabulary concepts
     * @throws IOException if error in I/O
     */
    public static void writeCSV(OutputStream out, String uriPrefix, List<VocabularyConcept> concepts) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
        addBOM(out);

        CSVWriter writer = new CSVWriter(osw, ',');
        String[] entries = {"URI", "Label", "Definition", "Notation"};
        writer.writeNext(entries);
        for (VocabularyConcept c : concepts) {
            entries = new String[4];
            entries[0] = uriPrefix + c.getIdentifier();
            entries[1] = c.getLabel();
            entries[2] = c.getDefinition();
            entries[3] = c.getNotation();
            writer.writeNext(entries);
        }
        writer.close();
        osw.close();
    }

    /**
     * Writes utf-8 BOM in the given writer.
     *
     * @param out current outputstream
     * @throws IOException
     *             if connection fails
     */
    private static void addBOM(OutputStream out) throws IOException {
        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

        for (byte b : bom) {
            out.write(b);
        }

    }
}
