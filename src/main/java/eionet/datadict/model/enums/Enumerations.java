
package eionet.datadict.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eworx-alk
 */
public class Enumerations {
    
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

    public enum AttributeDisplayType {
        TEXT("text", "Text box"),
        TEXTAREA("textarea", "Text area"),
        SELECT("select", "Select box"),
        IMAGE("image", "Image");

        private final String value;
        private final String displayLabel;
        
        private AttributeDisplayType(String value, String displayLabel) {
            this.value = value;
            this.displayLabel = displayLabel;
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

    public enum AttributeDisplayMultiple {
        ZERO("0"),
        ONE("1");

        private final String value;

        private AttributeDisplayMultiple(String value) {
            this.value = value;
        }

       public static AttributeDisplayMultiple getEnum(String s) {
            for (AttributeDisplayMultiple element : AttributeDisplayMultiple.values()) {
                if (element.value.equals(s)){
                    return element;
                }
            }
            return null;
        }

    };

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

        public static Inherit getEnum(String s) {
            for (Inherit element : Inherit.values()) {
                if (element.value.equals(s)){
                    return element;
                }
            }
            return null;
        }
        
        public String getLabel (){
            return this.label;
        }
    };

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
     
    //It is use in order to show for who to display an attribute
    public enum DisplayForType {
        // Commented exist in the DElemAttribute but are not used in the delem_attriubte.jsp
        //VCO(512),
        //AGG(4),
        //CH3(2),
        //DCL(16),
        VCF(1024, "Vocabulary folders"),
        SCS(256, "Schema sets"),
        SCH(128, "Schema"),
        TBL(64, "Dataset tables"),
        FXV(32, "Fixed values (code list and vocabulary"),
        DST(8, "Datasets"),
        CH1(2, "Data elements with fixed values (code list and elements from a vocabulary)"),
        CH2(1, "Data elements with quanitative values");
        
        private final int value;
        private final String label;
        
        private DisplayForType(int value, String label){
            this.value = value;
            this.label = label;
        }
        
        public static Map<String, String> getDisplayForTypes(int displayWhen){
            Map<String, String> displayForTypes = new HashMap<String, String>();
            for (DisplayForType type : DisplayForType.values()){
                if ((displayWhen / type.value) % 2 == 1) {
                    displayForTypes.put(type.name(), type.label);
                }
            }
            return displayForTypes;
        }        
        
    }
     
}
