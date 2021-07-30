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

package eionet.meta.exports.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import eionet.datadict.model.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.VocabularyOutputHelper;
import eionet.util.Props;
import eionet.util.PropsIF;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vocabulary JSON output helper.
 *
 * @author enver
 */
public final class VocabularyJSONOutputHelper {
    /**
     * Context keyword.
     */
    public static final String JSON_LD_CONTEXT = "@context";
    /**
     * Base keyword.
     */
    public static final String JSON_LD_BASE = "@base";
    /**
     * Id keyword.
     */
    public static final String JSON_LD_ID = "@id";
    /**
     * Language keyword.
     */
    public static final String JSON_LD_LANGUAGE = "@language";
    /**
     * Type keyword.
     */
    public static final String JSON_LD_TYPE = "@type";
    /**
     * Value keyword.
     */
    public static final String JSON_LD_VALUE = "@value";
    /**
     * Concepts keyword.
     */
    public static final String JSON_LD_CONCEPTS = "concepts";
    /**
     * Messages keyword.
     */
    public static final String MESSAGES = "messages";
    /**
     * Skos concept keyword.
     */
    public static final String SKOS_CONCEPT = "skos:Concept";
    /**
     * Pref label keyword.
     */
    public static final String PREF_LABEL = "prefLabel";
    /**
     * Skos pref label keyword.
     */
    public static final String SKOS_PREF_LABEL = "skos:prefLabel";
    /**
     * Broader keyword.
     */
    public static final String BROADER = "broader";
    /**
     * Skos broader keyword.
     */
    public static final String SKOS_BROADER = "skos:broader";
    /**
     * Narrower keyword.
     */
    public static final String NARROWER = "narrower";
    /**
     * Skos narrower keyword.
     */
    public static final String SKOS_NARROWER = "skos:narrower";
    /**
     * Default language.
     */
    public static final String DEFAULT_LANGUAGE = Props.getProperty(PropsIF.DD_WORKING_LANGUAGE_KEY);
    /**
     * Short data elem names to data elem identifer map.
     */
    private static final Map<String, String> DATA_ELEM_MAP = new HashMap<String, String>();

    static {
        DATA_ELEM_MAP.put(JSON_LD_CONCEPTS, SKOS_CONCEPT);
        DATA_ELEM_MAP.put(PREF_LABEL, SKOS_PREF_LABEL);
        DATA_ELEM_MAP.put(BROADER, SKOS_BROADER);
        DATA_ELEM_MAP.put(NARROWER, SKOS_NARROWER);
    }

    /**
     * Prevent public initialization.
     */
    private VocabularyJSONOutputHelper() {
    } // end of default constructor

    /**
     * Writes JSON to output stream.
     * <p>
     * NOTE: For readability purposes, nested blocks are used in this method while generating json contents.
     * </p>
     *
     * @param out
     *            output stream
     * @param vocabulary
     *            vocabulary base uri
     * @param concepts
     *            list of vocabulary concepts
     * @param language
     *            language for the preferred label
     * @throws java.io.IOException
     *             if error in I/O
     */
    public static void writeJSON(OutputStream out, VocabularyFolder vocabulary, List<VocabularyConcept> concepts, String language)
            throws IOException {

        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

        JsonFactory f = new JsonFactory();
        JsonGenerator generator = f.createGenerator(out);
        generator.useDefaultPrettyPrinter();

        language = StringUtils.trimToNull(language);
        boolean checkLanguage = StringUtils.isNotBlank(language);

        List<String> relationalDataElemIdentifiers = new ArrayList<String>();
        relationalDataElemIdentifiers.add(BROADER);
        relationalDataElemIdentifiers.add(NARROWER);

        // start json object
        generator.writeStartObject();
        // add context
        generator.writeObjectFieldStart(JSON_LD_CONTEXT);
        {
            generator.writeStringField(JSON_LD_BASE, VocabularyFolder.getBaseUri(vocabulary));
            generator.writeStringField(VocabularyOutputHelper.LinkedDataNamespaces.SKOS,
                    VocabularyOutputHelper.LinkedDataNamespaces.SKOS_NS);
            generator.writeStringField(JSON_LD_CONCEPTS, SKOS_CONCEPT);
            generator.writeStringField(PREF_LABEL, SKOS_PREF_LABEL);
            for (String dataElemShortIdentifier : relationalDataElemIdentifiers) {
                generator.writeStringField(dataElemShortIdentifier, DATA_ELEM_MAP.get(dataElemShortIdentifier));
            }
            generator.writeStringField(JSON_LD_LANGUAGE, StringUtils.isNotBlank(language) ? language : DEFAULT_LANGUAGE);

            // Write VOCABULARY columns values
            generator.writeStringField("VocabularyId", String.valueOf(vocabulary.getId()));
            generator.writeStringField("ContinuityId", vocabulary.getContinuityId());
            generator.writeStringField("Identifier", vocabulary.getIdentifier());
            generator.writeStringField("Label", vocabulary.getLabel());
            if(vocabulary.getRegStatus() != null){
                generator.writeStringField("RegistrationStatus", vocabulary.getRegStatus().getLabel());
            }
            generator.writeStringField("IsWorkingCopy", String.valueOf(vocabulary.isWorkingCopy()));
            generator.writeStringField("CheckedOutCopyId", String.valueOf(vocabulary.getCheckedOutCopyId()));
            generator.writeStringField("WorkingUser", vocabulary.getWorkingUser());
            if(vocabulary.getDateModified() != null){
                generator.writeStringField("DateModified", vocabulary.getDateModified().toString());
            }
            generator.writeStringField("UserModified", vocabulary.getUserModified());
            generator.writeStringField("IsNumericConceptIdentifiers", String.valueOf(vocabulary.isNumericConceptIdentifiers()));
            if(vocabulary.getType() != null){
                generator.writeStringField("VocabularyType", vocabulary.getType().getLabel());
            }
            generator.writeStringField("FolderId", String.valueOf(vocabulary.getFolderId()));
            generator.writeStringField("FolderName", vocabulary.getFolderName());
            generator.writeStringField("FolderLabel", vocabulary.getFolderLabel());
            generator.writeStringField("IsNotationsEqualIdentifiers", String.valueOf(vocabulary.isNotationsEqualIdentifiers()));

            if(vocabulary.getAttributes() != null) {
                for (List<SimpleAttribute> attributeList : vocabulary.getAttributes()) {
                    for (SimpleAttribute attribute : attributeList) {
                        if (attribute.getIdentifier().equals("Definition")) {
                            generator.writeStringField("Definition", String.valueOf(attribute.getValue()));
                        } else if (attribute.getIdentifier().equals("Version")) {
                            generator.writeStringField("Version", String.valueOf(attribute.getValue()));
                        }
                    }
                }
            }
        }
        generator.writeEndObject();
        // start writing concepts...
        generator.writeArrayFieldStart(JSON_LD_CONCEPTS);
        // iterate on concepts
        for (VocabularyConcept concept : concepts) {
            generator.writeStartObject();
            {
                generator.writeStringField(JSON_LD_ID, concept.getIdentifier());
                generator.writeStringField(JSON_LD_TYPE, SKOS_CONCEPT);
                // start writing prefLabels
                generator.writeArrayFieldStart(PREF_LABEL);
                {
                    String label;
                    String labelLang;
                    if (checkLanguage) {
                        List<DataElement> dataElementValuesByNameAndLang =
                                VocabularyOutputHelper.getDataElementValuesByNameAndLang(SKOS_PREF_LABEL, language,
                                        concept.getElementAttributes());
                        if (dataElementValuesByNameAndLang != null && dataElementValuesByNameAndLang.size() > 0) {
                            label = dataElementValuesByNameAndLang.get(0).getAttributeValue();
                            labelLang = language;
                        } else {
                            dataElementValuesByNameAndLang =
                                    VocabularyOutputHelper.getDataElementValuesByNameAndLang(SKOS_PREF_LABEL, DEFAULT_LANGUAGE,
                                            concept.getElementAttributes());
                            if (dataElementValuesByNameAndLang != null && dataElementValuesByNameAndLang.size() > 0) {
                                label = dataElementValuesByNameAndLang.get(0).getAttributeValue();
                            } else {
                                label = concept.getLabel();
                            }
                            labelLang = DEFAULT_LANGUAGE;
                        }
                        generator.writeStartObject();
                        {
                            generator.writeStringField(JSON_LD_VALUE, label);
                            generator.writeStringField(JSON_LD_LANGUAGE, labelLang);
                        }
                        generator.writeEndObject();
                    } else {
                        generator.writeStartObject();
                        {
                            generator.writeStringField(JSON_LD_VALUE, concept.getLabel());
                            generator.writeStringField(JSON_LD_LANGUAGE, DEFAULT_LANGUAGE);
                        }
                        generator.writeEndObject();
                        List<DataElement> dataElementValuesByName =
                                VocabularyOutputHelper.getDataElementValuesByName(SKOS_PREF_LABEL, concept.getElementAttributes());
                        if (dataElementValuesByName != null && dataElementValuesByName.size() > 0) {
                            for (DataElement elem : dataElementValuesByName) {
                                generator.writeStartObject();
                                {
                                    generator.writeStringField(JSON_LD_VALUE, elem.getAttributeValue());
                                    generator.writeStringField(JSON_LD_LANGUAGE, elem.getAttributeLanguage());
                                }
                                generator.writeEndObject();
                            }
                        }
                    }
                }
                // end writing prefLabels
                generator.writeEndArray();

                // Write VOCABULARY_CONCEPT columns values
                generator.writeStringField("VocabularyConceptId", String.valueOf(concept.getId()));
                generator.writeStringField("VocabularyId", String.valueOf(vocabulary.getId()));
                generator.writeStringField("Identifier", concept.getIdentifier());
                generator.writeStringField("Label", concept.getLabel());
                generator.writeStringField("Definition", concept.getDefinition());
                generator.writeStringField("Notation", concept.getNotation());
                if(concept.getStatus() != null){
                    generator.writeStringField("Status", concept.getStatus().getLabel());
                }
                if(concept.getStatusModified() != null){
                    generator.writeStringField("StatusModifiedDate", concept.getStatusModified().toString());
                }
                if(concept.getAcceptedDate() != null){
                    generator.writeStringField("AcceptedDate", concept.getAcceptedDate().toString());
                }
                if(concept.getNotAcceptedDate() != null){
                    generator.writeStringField("NotAcceptedDate", concept.getNotAcceptedDate().toString());
                }

                // write data elements
                for (String shortDataElemIdentifier : relationalDataElemIdentifiers) {
                    // check if it has this element
                    List<DataElement> dataElementValuesByName =
                            VocabularyOutputHelper.getDataElementValuesByName(DATA_ELEM_MAP.get(shortDataElemIdentifier),
                                    concept.getElementAttributes());
                    if (dataElementValuesByName != null && dataElementValuesByName.size() > 0) {
                        // start writing element values
                        generator.writeArrayFieldStart(shortDataElemIdentifier);
                        for (DataElement elem : dataElementValuesByName) {
                            generator.writeStartObject();
                            {
                                generator.writeStringField(JSON_LD_ID, elem.getRelatedConceptIdentifier());
                            }
                            generator.writeEndObject();
                        }
                        // end writing element values
                        generator.writeEndArray();
                    }
                }

                //write attribute elements values
                for(List<DataElement> elementAttributeList: concept.getElementAttributes()){
                    if(elementAttributeList.size() == 0){
                        continue;
                    }
                    generator.writeArrayFieldStart(elementAttributeList.get(0).getIdentifier());
                    for(DataElement elementAttribute: elementAttributeList){
                        if(elementAttribute.getAttributeValue() != null){
                            generator.writeString(elementAttribute.getAttributeValue());
                        }
                        else{
                            if(elementAttribute.getRelatedConceptBaseURI() != null && elementAttribute.getRelatedConceptIdentifier() != null ){
                                String url = elementAttribute.getRelatedConceptBaseURI() + elementAttribute.getRelatedConceptIdentifier();
                                generator.writeString(url);
                            }
                        }
                    }
                    generator.writeEndArray();
                }
            }
            // end writing concept
            generator.writeEndObject();
        } // end of iteration on concepts
        generator.writeEndArray();
        // end of vocabulary name
        generator.writeEndObject();

        // close writer and stream
        generator.close();
        osw.close();
    } // end of static method writeJSON

    /**
     * Writes given list of messages as JSON array.
     *
     * @param out      output stream
     * @param messages message list
     * @throws java.io.IOException if error in I/O
     */
    public static void writeJSON(OutputStream out, List<String> messages)
            throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

        JsonFactory f = new JsonFactory();
        JsonGenerator generator = f.createGenerator(out);
        generator.useDefaultPrettyPrinter();

        // start json object
        generator.writeStartObject();
        generator.writeArrayFieldStart(MESSAGES);
        if (messages != null) {
            for (String message : messages) {
                generator.writeString(message);
            }
        }
        generator.writeEndArray();
        // end json object
        generator.writeEndObject();

        // close writer and stream
        generator.close();
        osw.close();
    } // end of static method writeJSON

} // end of class VocabularyJSONOutputHelper
