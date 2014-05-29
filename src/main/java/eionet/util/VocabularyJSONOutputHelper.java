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

package eionet.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;

/**
 * Vocabulary JSON output helper.
 *
 * @author enver
 */
public final class VocabularyJSONOutputHelper {

    /**
     * Prevent public initialization.
     */
    private VocabularyJSONOutputHelper() {
    } // end of default constructor

    /**
     * Writes JSON to output stream.
     *
     * @param out
     *            output stream
     * @param vocabularyFolderIdentifier
     *            vocabulary folder identifier
     * @param concepts
     *            list of vocabulary concepts
     * @param language
     *            language for the preferred label
     * @throws java.io.IOException
     *             if error in I/O
     */
    public static void writeJSON(OutputStream out, String vocabularyFolderIdentifier, List<VocabularyConcept> concepts,
            String language) throws IOException {

        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

        JsonFactory f = new JsonFactory();
        JsonGenerator generator = f.createJsonGenerator(osw);
        generator.useDefaultPrettyPrinter();

        language = StringUtils.trimToNull(language);
        boolean checkLanguage = StringUtils.isNotBlank(language);

        // TODO for performance tuning check bounded elements and languages, so no need to query every time!!
        // TODO or maybe a special query on database level for languages

        // begin with vocabulary name
        generator.writeStartObject();
        // now fill vocabulary contents (concepts actually)
        generator.writeObjectFieldStart(vocabularyFolderIdentifier);
        // generator.writeStartObject();
        generator.writeArrayFieldStart("item");
        // iterate on concepts
        for (VocabularyConcept concept : concepts) {
            generator.writeStartObject();
            generator.writeStringField("code", concept.getIdentifier());

            String label;
            if (checkLanguage) {
                List<DataElement> dataElementValuesByNameAndLang =
                        VocabularyOutputHelper.getDataElementValuesByNameAndLang("skos:prefLabel", language,
                                concept.getElementAttributes());
                if (dataElementValuesByNameAndLang != null && dataElementValuesByNameAndLang.size() > 0) {
                    label = dataElementValuesByNameAndLang.get(0).getAttributeValue();
                } else {
                    label = concept.getLabel();
                }
            } else {
                label = concept.getLabel();
            }
            generator.writeStringField("label", label);
            generator.writeEndObject();
        }
        // end of iteration on concepts
        generator.writeEndArray();
        // end of concept start
        generator.writeEndObject();
        // end of vocabulary name
        generator.writeEndObject();

        // close writer and stream
        generator.close();
        osw.close();
    } // end of static method writeJSON

} // end of class VocabularyJSONOutputHelper
