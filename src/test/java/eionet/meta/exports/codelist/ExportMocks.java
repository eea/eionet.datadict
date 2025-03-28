/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;


import eionet.meta.Namespace;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class ExportMocks {

    static String XML_EXPORT_ROOT_ELEMENT_OPEN = "<value-lists xmlns=\"http://dd.eionet.europa.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
    static String XML_EXPORT_ROOT_ELEMENT_CLOSE = "</value-lists>";

    static List<eionet.meta.DataElement> quantitativeDataElement() {
        // ID, Short name, Type
        // Type Fixed: CH1, Quantitative: CH2, Vocabulary: CH3
        eionet.meta.DataElement element = new eionet.meta.DataElement("123", "test", "CH2");

        // Vocabulary ID
        element.setVocabularyId("456");

        // common element - null namespace
        element.setNamespace( null );

        element.setIdentifier("test-quantitative");

        // Table identifier
        element.setTblIdentifier("national_overview");

        // Dataset identifier
        element.setDstIdentifier("CDDA");

        List<eionet.meta.DataElement> elements = new ArrayList<eionet.meta.DataElement>();
        elements.add(element);
        return elements;
    }

    static List<eionet.meta.DataElement> vocabularyCommonDataElement() {
        // ID, Short name, Type
        // Type Fixed: CH1, Quantitative: CH2, Vocabulary: CH3
        eionet.meta.DataElement element = new eionet.meta.DataElement("123", "Language Code", "CH3");

        //Vocabulary ID
        element.setVocabularyId("456");

        //common element - null namespace
        element.setNamespace(null);

        element.setIdentifier("languageCode");

        //Table identifier
        element.setTblIdentifier(null);

        //Dataset identifier
        element.setDstIdentifier(null);

        List<eionet.meta.DataElement> elements = new ArrayList<eionet.meta.DataElement>();
        elements.add(element);
        return elements;
    }

    static List<eionet.meta.DataElement> vocabularyDataElement() {
        List<eionet.meta.DataElement> vocabularyCommonDataElement = vocabularyCommonDataElement();
        for (eionet.meta.DataElement el : vocabularyCommonDataElement) {
            // Table identifier
            el.setTblIdentifier("Languages");
            // Dataset identifier
            el.setDstIdentifier("EU taxonomies");
            el.setNamespace(new Namespace("123", "ParentNS", "Parent Namespace", "http://dd.test", "The parent namespace"));
        }
        return vocabularyCommonDataElement;
    }

    static List<CodeItem> vocabularyConceptsWithRelationships() {
        List<CodeItem> values = new ArrayList<CodeItem>();

        CodeItem item = new CodeItem("el", "Greek", "Greek \"language\"");
        List<RelationshipInfo> relInfo = new ArrayList<RelationshipInfo>();
        // Rel to country
        List<CodeItem> relsCountry = new ArrayList<CodeItem>();
        relsCountry.add(new CodeItem("GR", "Greece", "Greece/Hellas Country"));
        RelationshipInfo relCountry = new RelationshipInfo("language of country", "Countries", "EU taxonomies", relsCountry);

        //Rel to other language
        List<CodeItem> relsLang = new ArrayList<CodeItem>();
        relsLang.add(new CodeItem("el/cy", "Cypriot Greek", "Dialect of Greek spoken in Cyprus"));
        relsLang.add(new CodeItem("el/it", "Southern Italy Greek", "Dialect of Greek spoken in southern Italy"));
        RelationshipInfo relLang = new RelationshipInfo("similar language", "Languages", "EU taxonomies", relsLang);

        relInfo.add(relLang);
        relInfo.add(relCountry);
        item.setRelationships(relInfo);
        values.add(item);

        CodeItem en = new CodeItem("en", "English", "English language");
        List<RelationshipInfo> enRelInfo = new ArrayList<RelationshipInfo>();
        // Rel to country
        List<CodeItem> enRels = new ArrayList<CodeItem>();
        enRels.add(new CodeItem("US", "USA", "United States of America"));
        enRels.add(new CodeItem("UK", "UK", "United Kingdom"));
        RelationshipInfo enRel = new RelationshipInfo("language of country", "Countries", "EU taxonomies", enRels);

        enRelInfo.add(enRel);
        en.setRelationships(enRelInfo);
        values.add(en);

        return values;
    }

    static List<String> vocabularyConceptRelationshipNames() {
        List<String> relationshipNames = new ArrayList<String>();
        relationshipNames.add("similar language");
        relationshipNames.add("language of country");
        return relationshipNames;
    }

    static String commonDataElementWithVocabularyValuesWithRelationshipsExportXML() {
        return 
            " <value-list element=\"languageCode\" type=\"vocabulary\">" +
            "  <value code=\"el\">" +
            "   <label>Greek</label>" +
            "   <definition>Greek &quot;language&quot;</definition>" +
            "   <relationship-list>" +
            "    <relationship attribute=\"similar language\" vocabulary=\"Languages\" vocabularySet=\"EU taxonomies\">" +
            "      <value code=\"el/cy\">" +
            "        <label>Cypriot Greek</label>" +
            "        <definition>Dialect of Greek spoken in Cyprus</definition>" +
            "      </value>" +
            "      <value code=\"el/it\">" +
            "        <label>Southern Italy Greek</label>" +
            "        <definition>Dialect of Greek spoken in southern Italy</definition>" +
            "      </value>" +
            "     </relationship>" +
            "     <relationship attribute=\"language of country\" vocabulary=\"Countries\" vocabularySet=\"EU taxonomies\">" +
            "      <value code=\"GR\">" +
            "        <label>Greece</label>" +
            "        <definition>Greece/Hellas Country</definition>" +
            "      </value>" +
            "     </relationship>" +
            "   </relationship-list>" +
            "  </value>" +
            "  <value code=\"en\">" +
            "   <label>English</label>" +
            "   <definition>English language</definition>" +
            "   <relationship-list>" +
            "    <relationship attribute=\"language of country\" vocabulary=\"Countries\" vocabularySet=\"EU taxonomies\">" +
            "      <value code=\"US\">" +
            "        <label>USA</label>" +
            "        <definition>United States of America</definition>" +
            "      </value>" +
            "      <value code=\"UK\">" +
            "        <label>UK</label>" +
            "        <definition>United Kingdom</definition>" +
            "      </value>" +
            "    </relationship>" +
            "   </relationship-list>" +
            "  </value>" +
            " </value-list>";
    }

    static String commonDataElementWithVocabularyValuesWithRelationshipsExportCSV() {
        return 
            "Element:languageCode Type:vocabulary\r\n" +
            "\"Code\",\"Label\",\"Definition\",\"similar language\",\"language of country\"\r\n" +
            "\"el\",\"Greek\",\"Greek \"\"language\"\"\",\"EU taxonomies::Languages::el/cy,el/it\",\"EU taxonomies::Countries::GR\"\r\n" +
            "\"en\",\"English\",\"English language\",\"\",\"EU taxonomies::Countries::US,UK\"\r\n\r\n";
    }

    static String uncommonDataElementWithVocabularyValuesWithRelationshipsExportXML() {
        return 
            " <value-list element=\"languageCode\" table=\"Languages\" dataset=\"EU taxonomies\" type=\"vocabulary\">" +
            "  <value code=\"el\">" +
            "   <label>Greek</label>" +
            "   <definition>Greek &quot;language&quot;</definition>" +
            "   <relationship-list>" +
            "    <relationship attribute=\"similar language\" vocabulary=\"Languages\" vocabularySet=\"EU taxonomies\">" +
            "      <value code=\"el/cy\">" +
            "        <label>Cypriot Greek</label>" +
            "        <definition>Dialect of Greek spoken in Cyprus</definition>" +
            "      </value>" +
            "      <value code=\"el/it\">" +
            "        <label>Southern Italy Greek</label>" +
            "        <definition>Dialect of Greek spoken in southern Italy</definition>" +
            "      </value>" +
            "     </relationship>" +
            "     <relationship attribute=\"language of country\" vocabulary=\"Countries\" vocabularySet=\"EU taxonomies\">" +
            "      <value code=\"GR\">" +
            "        <label>Greece</label>" +
            "        <definition>Greece/Hellas Country</definition>" +
            "      </value>" +
            "     </relationship>" +
            "   </relationship-list>" +
            "  </value>" +
            "  <value code=\"en\">" +
            "   <label>English</label>" +
            "   <definition>English language</definition>" +
            "   <relationship-list>" +
            "    <relationship attribute=\"language of country\" vocabulary=\"Countries\" vocabularySet=\"EU taxonomies\">" +
            "      <value code=\"US\">" +
            "        <label>USA</label>" +
            "        <definition>United States of America</definition>" +
            "      </value>" +
            "      <value code=\"UK\">" +
            "        <label>UK</label>" +
            "        <definition>United Kingdom</definition>" +
            "      </value>" +
            "    </relationship>" +
            "   </relationship-list>" +
            "  </value>" +
            " </value-list>";
    }

    static String uncommonDataElementWithVocabularyValuesWithRelationshipsExportCSV() {
        return "Dataset:EU taxonomies Table:Languages " + commonDataElementWithVocabularyValuesWithRelationshipsExportCSV();
    }

    static List<eionet.meta.DataElement> vocabularyCommonDataElementSimple() {
        // ID, Short name, Type
        // Type Fixed: CH1, Quantitative: CH2, Vocabulary: CH3
        eionet.meta.DataElement element = new eionet.meta.DataElement("987", "In Country", "CH3");

        // Vocabulary ID
        element.setVocabularyId("456");

        // common element - null namespace
        element.setNamespace( null );

        element.setIdentifier("inCountry");

        // Table identifier
        element.setTblIdentifier(null);

        // Dataset identifier
        element.setDstIdentifier(null);

        List<eionet.meta.DataElement> elements = new ArrayList<eionet.meta.DataElement>();
        elements.add(element);

        return elements;
    }

    static List<CodeItem> vocabularyConcepts(){       
        List<CodeItem> values = new ArrayList<CodeItem>();
        values.add(new CodeItem("TJ", "Tajikistan", ""));
        values.add(new CodeItem("TK", "Tokelau", ""));
        return values;
    }

    static String commonDataElementWithVocabularyValuesExportXML() {
        return 
            " <value-list element=\"inCountry\" type=\"vocabulary\">" +
            "  <value code=\"TJ\">" +
            "   <label>Tajikistan</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"TK\">" +
            "   <label>Tokelau</label>" +
            "   <definition/>" +
            "  </value>" +
            " </value-list>";
    }

    static String commonDataElementWithVocabularyValuesExportCSV() {
        return 
            "Element:inCountry Type:vocabulary\r\n" +
            "\"Code\",\"Label\",\"Definition\"\r\n" +
            "\"TJ\",\"Tajikistan\",\"\"\r\n" +
            "\"TK\",\"Tokelau\",\"\"\r\n\r\n";
    }

    static List<eionet.meta.DataElement> commonFixedValueDataElement() {
        // ID, Short name, Type
        // Type Fixed: CH1, Quantitative: CH2, Vocabulary: CH3
        eionet.meta.DataElement element = new eionet.meta.DataElement("123", "Age groups", "CH1");

        // Vocabulary ID
        element.setVocabularyId("456");

        // common element - null namespace
        element.setNamespace( null );

        element.setIdentifier("ageGroup");

        // Table identifier
        element.setTblIdentifier(null);

        //Dataset identifier
        element.setDstIdentifier(null);

        List<eionet.meta.DataElement> elements = new ArrayList<eionet.meta.DataElement>();
        elements.add(element);

        return elements;
    }

    static List<eionet.meta.DataElement> uncommonFixedValueDataElement() {
        List<eionet.meta.DataElement> commonFixedValueDataElement = commonFixedValueDataElement();
        for (eionet.meta.DataElement el : commonFixedValueDataElement) {
            // Table identifier
            el.setTblIdentifier("Person");
            // Dataset identifier
            el.setDstIdentifier("Misc taxonomies");
            el.setNamespace( new Namespace("123", "ParentNS", "Parent Namespace", "http://dd.test", "The parent namespace") );
        }
        return commonFixedValueDataElement;
    }

    static List<CodeItem> fixedValues() {       
        List<CodeItem> values = new ArrayList<CodeItem>();
        values.add(new CodeItem("00", "up to 15", ""));
        values.add(new CodeItem("01", "15-20", ""));
        values.add(new CodeItem("02", "20-25", ""));
        values.add(new CodeItem("03", "25-30", ""));
        values.add(new CodeItem("04", "30-40", ""));
        values.add(new CodeItem("05", "40-50", ""));
        values.add(new CodeItem("06", "60+", ""));
        return values;
    }

    static List<CodeItem> quantitativeValues() {
        List<CodeItem> values = new ArrayList<CodeItem>();
        values.add(new CodeItem("a", "aa", "aaa"));
        values.add(new CodeItem("b", "bb", "bbb"));
        values.add(new CodeItem("c", "cc", "ccc"));
        return values;
    }

    static String commonDataElementWithFixedValuesExportXML() {
        return 
            " <value-list element=\"ageGroup\" type=\"fixed\">" +
            "  <value code=\"00\">" +
            "   <label>up to 15</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"01\">" +
            "   <label>15-20</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"02\">" +
            "   <label>20-25</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"03\">" +
            "   <label>25-30</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"04\">" +
            "   <label>30-40</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"05\">" +
            "   <label>40-50</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"06\">" +
            "   <label>60+</label>" +
            "   <definition/>" +
            "  </value>" +
            " </value-list>";
    }

    static String commonDataElementWithFixedValuesExportCSV() {
        return 
            "Element:ageGroup Type:fixed\r\n" +
            "\"Code\",\"Label\",\"Definition\"\r\n" +
            "\"00\",\"up to 15\",\"\"\r\n" +
            "\"01\",\"15-20\",\"\"\r\n" +
            "\"02\",\"20-25\",\"\"\r\n" +
            "\"03\",\"25-30\",\"\"\r\n" +
            "\"04\",\"30-40\",\"\"\r\n" +
            "\"05\",\"40-50\",\"\"\r\n" +
            "\"06\",\"60+\",\"\"\r\n\r\n";
    }

    static String quantitativeValuesExportXML() {
        return 
            " <value-list element=\"test-quantitative\" type=\"quantitative\">" +
            "  <value code=\"a\">" +
            "   <label>aa</label>" +
            "   <definition>aaa</definition>" +
            "  </value>" +
            "  <value code=\"b\">" +
            "   <label>bb</label>" +
            "   <definition>bbb</definition>" +
            "  </value>" +
            "  <value code=\"c\">" +
            "   <label>cc</label>" +
            "   <definition>ccc</definition>" +
            "  </value>" +
            " </value-list>";
    }

    static String quantitativeValuesLegacyExportXML() {
        return 
            " <value-list element=\"test-quantitative\" type=\"quantitative\">" +
            "  <value value=\"a\">" +
            "   <shortDescription>aa</shortDescription>" +
            "   <definition>aaa</definition>" +
            "  </value>" +
            "  <value value=\"b\">" +
            "   <shortDescription>bb</shortDescription>" +
            "   <definition>bbb</definition>" +
            "  </value>" +
            "  <value value=\"c\">" +
            "   <shortDescription>cc</shortDescription>" +
            "   <definition>ccc</definition>" +
            "  </value>" +
            " </value-list>";
    }

    static String quantitativeValuesExportCSV() {
        return 
            "Element:test-quantitative Type:quantitative\r\n" +
            "\"Code\",\"Label\",\"Definition\"\r\n" +
            "\"a\",\"aa\",\"aaa\"\r\n" +
            "\"b\",\"bb\",\"bbb\"\r\n" +
            "\"c\",\"cc\",\"ccc\"\r\n\r\n";
    }

    static String uncommonDataElementWithFixedValuesExportXML() {
        return 
            " <value-list element=\"ageGroup\" type=\"fixed\" table=\"Person\" dataset=\"Misc taxonomies\">" +
            "  <value code=\"00\">" +
            "   <label>up to 15</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"01\">" +
            "   <label>15-20</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"02\">" +
            "   <label>20-25</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"03\">" +
            "   <label>25-30</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"04\">" +
            "   <label>30-40</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"05\">" +
            "   <label>40-50</label>" +
            "   <definition/>" +
            "  </value>" +
            "  <value code=\"06\">" +
            "   <label>60+</label>" +
            "   <definition/>" +
            "  </value>" +
            " </value-list>";
    }

    static String uncommonDataElementWithFixedValuesExportCSV() {
        return "Dataset:Misc taxonomies Table:Person " + commonDataElementWithFixedValuesExportCSV();
    }

    static List<eionet.meta.DataElement> dataset() {
        List<eionet.meta.DataElement> elements = new ArrayList<eionet.meta.DataElement>();
        elements.addAll(uncommonFixedValueDataElement());
        elements.addAll(vocabularyDataElement());        
        return elements;
    }

    static String datasetExportXML() {
        return uncommonDataElementWithFixedValuesExportXML() + uncommonDataElementWithVocabularyValuesWithRelationshipsExportXML();
    }

    static String datasetExportCSV() {
        return uncommonDataElementWithFixedValuesExportCSV() + uncommonDataElementWithVocabularyValuesWithRelationshipsExportCSV();
    }

    static String emptyDatasetExportCSV() {
        return "";
    }
    
    static String emptyDatasetExportXML() {
        String root = XML_EXPORT_ROOT_ELEMENT_OPEN;
        return root.substring(0, (root.length()-1) ) + "/>";
    }

    static String wrapXML(String xml) {
        return XML_EXPORT_ROOT_ELEMENT_OPEN + xml + XML_EXPORT_ROOT_ELEMENT_CLOSE;
    }

}