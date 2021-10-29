
package eionet.datadict.model.enums;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Enumerations {

    public enum VocabularyRdfPurgeOption {

        DELETE_VOCABULARY_DATA(4),
        PURGE_VOCABULARY_DATA(3),
        PURGE_PREDICATE_BASIS(2),
        DONT_PURGE(1);
        private final int rdfPurgeOption;

        private VocabularyRdfPurgeOption(int rdfPurgeOption) {
            this.rdfPurgeOption = rdfPurgeOption;
        }

        public int getRdfPurgeOption() {
            return rdfPurgeOption;
        }

        public static String translateRDFPurgeOptionNumberToEnum(int rdfPurgeOption){
            switch(rdfPurgeOption){
                case 1:
                    return DONT_PURGE.toString();
                case 2:
                    return PURGE_PREDICATE_BASIS.toString();
                case 3: 
                    return PURGE_VOCABULARY_DATA.toString();
                case 4:
                    return DELETE_VOCABULARY_DATA.toString();
            }
            throw new IllegalArgumentException("Integer:"+rdfPurgeOption+" doesn't exist as an RdfPurgeOption");
        }
    }

    /**
     * Used for the eionet.datadict.model.AttributeDefinition
     */
    public enum Obligation {
        M("Mandatory"), 
        O("Optional"), 
        C("Conditional");
        
        private final String label;
        
        private Obligation (String label) {
            this.label = label;
        }
        
        public String getLabel(){
            return this.label;
        }
    };

    /**
     * Used for the eionet.datadict.model.AttributeDefinition
     */
    public enum AttributeDisplayType {
        TEXT("text", "Text box"),
        TEXTAREA("textarea", "Text area"),
        SELECT("select", "Select box"),
        IMAGE("image", "Image"),
        VOCABULARY("vocabulary", "Vocabulary");

        private final String value;
        private final String displayLabel;
        
        private AttributeDisplayType(String value, String displayLabel) {
            this.value = value;
            this.displayLabel = displayLabel;
        }
        
        public String getValue() {
            return value;
        }

        public static AttributeDisplayType getEnum(String s) {
            for (AttributeDisplayType element : AttributeDisplayType.values()) {
                if (element.value.equals(s)){
                    return element;
                }
            }
            return null;
        }
        
        public String getDisplayLabel(){
            return this.displayLabel;
        }
    };

    /**
     * Used for the eionet.datadict.model.AttributeDefinition
     */
    public enum Inherit {
        ZERO("0", "No inheritance"),
        ONE("1", "Inherit attribute values from parent level with possibilty to add new values"),
        TWO("2", "Inherit attribute values from parent level with possibilty to overwrite them");

        private final String value;
        private final String label;

        private Inherit(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public static List<Inherit> getAllEnums(){
            List<Inherit> elements = new ArrayList<Inherit>();
            elements.addAll(Arrays.asList(Inherit.values()));
            return elements;
        }
        
        public static Inherit getEnum(String s) {
            for (Inherit element : Inherit.values()) {
                if (element.value.equals(s)){
                    return element;
                }
            }
            return null;
        }
        
        public String getValue() {
            return this.value;
        }
        
        public String getLabel (){
            return this.label;
        }
    };

    /**
     * Used for the eionet.datadict.model.AttributeDefinition
     */
    public enum AttributeDataType {
        REFERENCE("reference"),
        STRING("string"),
        INTEGER("integer"),
        DECIMAL("decimal"),
        BOOLEAN("boolean"),
        DATETIME("dateTime"),
        DATE("date");
        private final String value;

        private AttributeDataType(String value) {
            this.value = value;
        }

        public static AttributeDataType getEnum(String s) {
            for (AttributeDataType element : AttributeDataType.values()) {
                if (element.value.equals(s)){
                    return element;
                }
            }
            return null;
        }
    };
    
    /**
     * Used for the eionet.datadict.model.Attribute
     */
     public enum DDEntitiyType {
        ELEMENT("E"), 
        TABLE("T"), 
        DATASET("DS"), 
        SCHEMA ("SCH"),
        SCHEMASET ("SCS"),
        VOCABULARYFOLDER("VCF");
        
        private final String value;
        
        private DDEntitiyType(String value){
            this.value = value;
        }
        
        public static DDEntitiyType getEnum(String s) {
            for (DDEntitiyType element : DDEntitiyType.values()) {
                if (element.value.equals(s)){
                    return element;
                }
            }
            return null;
        }
    }
     
    /**
     * Used for the AttributeDefinition
     * is related to the displayWhen property of the AttributeDefinition
     */
    public enum DisplayForType {
        // Commented exist in the DElemAttribute but are not used in the delem_attriubte.jsp
        //VCO(512),
        //AGG(4),
        //CH3(2),
        //DCL(16),
        //FXV(32, "Fixed values (code list and vocabulary"),
        CH1(2, "Data elements with fixed values (code list and elements from a vocabulary)"),
        CH2(1, "Data elements with quanitative values"),
        DST(8, "Datasets"),
        TBL(64, "Dataset tables"),
        SCH(128, "Schema"),
        SCS(256, "Schema sets"),
        VCF(1024, "Vocabulary folders");
        
        private final int value;
        private final String label;
        
        private DisplayForType(int value, String label){
            this.value = value;
            this.label = label;
        }
        
        public int getValue(){
            return this.value;
        }
        public String getLabel(){
            return this.label;
        }

        public static List<DisplayForType> getDisplayForTypes(int displayWhen){
            List<DisplayForType> displayForTypes = new ArrayList<DisplayForType>();
            for (DisplayForType type : DisplayForType.values()){
                if ((displayWhen / type.value) % 2 == 1) {
                    displayForTypes.add(type);
                }
            }
            return displayForTypes;
        } 
        
        public static List<DisplayForType> getAllEnums(){
            List<DisplayForType> elements = new ArrayList<DisplayForType>();
            elements.addAll(Arrays.asList(DisplayForType.values()));
            return elements;
        }
        
    }

    public enum SchedulingIntervalUnit {
        MINUTE(1, "minute(s)"), 
        HOUR(60, "hour(s)"), 
        DAY(24 * 60, "day(s)"),
        WEEK(7 * 24 * 60, "week(s)");

        private final int minutes;
        private final String label;

        SchedulingIntervalUnit(int minutes, String label) {
            this.minutes = minutes;
            this.label = label;
        }

        public int getMinutes() {
            return minutes;
        }

        public String getLabel() {
            return this.label;
        }

    }

    public enum SiteCodeBoundElementIdentifiers {
        COUNTRY_CODE("sitecodes_CC_ISO2"),
        DATE_ALLOCATED("sitecodes_DATE_ALLOCATED"),
        DATE_CREATED("sitecodes_DATE_CREATED"),
        DATE_DELETED("sitecodes_DATE_DELETED"),
        INITIAL_SITE_NAME("sitecodes_INITIAL_SITE_NAME"),
        SITE_CODE_NAT("sitecodes_SITE_CODE_NAT"),
        STATUS("sitecodes_STATUS"),
        USER_ALLOCATED("sitecodes_USER_ALLOCATED"),
        USER_CREATED("sitecodes_USER_CREATED"),
        YEARS_DELETED("sitecodes_YEARS_DELETED"),
        YEARS_DISAPPEARED("sitecodes_YEARS_DISAPPEARED");

        private final String identifier;

        SiteCodeBoundElementIdentifiers(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public static List<String> getEnumValuesAsList() {
            List<String> identifiers = new ArrayList();
            for (SiteCodeBoundElementIdentifiers element : SiteCodeBoundElementIdentifiers.values()) {
                identifiers.add(element.identifier);
            }
            return identifiers;
        }

    }

    public enum StatusesForAcceptedDate {
        EXPERIMENTAL("experimental"),
        DEPRECATED("deprecated"),
        RETIRED("retired"),
        STABLE("stable"),
        SUPERSEDED("superseded"),
        VALID("valid");

        private final String value;

        private StatusesForAcceptedDate(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static List<String> getEnumValues() {
            List<String> values = new ArrayList();
            for (StatusesForAcceptedDate element : StatusesForAcceptedDate.values()) {
                values.add(element.value);
            }
            return values;
        }
    }

    public enum StatusesForNotAcceptedDate {
        INVALID("invalid"),
        RESERVED("reserved"),
        SUBMITTED("submitted");

        private final String value;

        private StatusesForNotAcceptedDate(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static List<String> getEnumValues() {
            List<String> values = new ArrayList();
            for (StatusesForNotAcceptedDate element : StatusesForNotAcceptedDate.values()) {
                values.add(element.value);
            }
            return values;
        }
    }

}
