package eionet.datadict.model;

import eionet.datadict.model.enums.Enumerations.AttributeDataType;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class Attribute {

    
    public static enum TargetEntity {
        
        CH1(2, "Data elements with fixed values (code list and elements from a vocabulary)"), // Data elements with fixed values (code list and elements from a vocabulary)
        CH2(1, "Data elements with quantitative values"), // Data elements with quantitative values
        //CH3(2, "Data elements with fixed values (code list and elements from a vocabulary)"), // Data elements with fixed values (code list and elements from a vocabulary)
        DST(8, "Datasets"), // Datasets
        TBL(64, "Dataset tables"), // Dataset tables
        SCH(128, "Schemas"), // Schemas
        SCS(256, "Schema sets"), // Schema sets
        VCF(1024, "Vocabularies") // Vocabularies
        ;
        
        private final int value;
        private final String label;
        
        private TargetEntity(int value, String label) {
            this.value = value;
            this.label = label;
        }
        
        public int getValue() {
            return this.value;
        }
        
        public String getLabel() {
            return this.label;
        }
        
    }
    
    public static enum DisplayType {
        TEXT("Text box"),
        TEXTAREA("Text area"),
        SELECT("Select box"),
        IMAGE("Image"),
        VOCABULARY("Vocabulary");
        
        private final String label;
        
        private DisplayType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return this.label;
        }
        
    }
    
    public static enum ObligationType {
        MANDATORY("Mandatory"),
        OPTIONAL("Optional"),
        CONDITIONAL("Conditional");
        
        private final String label;
        
        private ObligationType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return this.label;
        }
        
    }
    
    public static enum ValueInheritanceMode {
        NONE("0", "No inheritance"),
        PARENT_WITH_EXTEND("1", "Inherit attribute values from parent level with possibilty to add new values"),
        PARENT_WITH_OVERRIDE("2", "Inherit attribute values from parent level with possibilty to overwrite them");
       
        private final String key;
        private final String label;
        
        private ValueInheritanceMode(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public String getKey() {
            return this.key;
        }

        public String getLabel() {
            return this.label;
        }
        
        public static ValueInheritanceMode getInstance(String key) {
            if (StringUtils.isNotBlank(key)) {
                for (ValueInheritanceMode inheritanceMode : ValueInheritanceMode.values()) {
                    if (inheritanceMode.getKey().equalsIgnoreCase(key)) {
                        return inheritanceMode;
                    }
                }
            }
            return NONE;  // default fall-back
        }
    }

    @Id
    private Integer id;
    private Integer displayOrder;
    private Integer displayWidth;
    private Integer displayHeight;
    private boolean languageUsed;
    private boolean displayMultiple;

    private String rdfPropertyName;
    private String name;
    private String definition;
    private String shortName;

    private Set<TargetEntity> targetEntities;
    private DisplayType displayType;
    private AttributeDataType dataType;
    private ObligationType obligationType;
    private ValueInheritanceMode valueInheritanceMode;
    
    
    @ManyToOne
    private Namespace namespace;
    private RdfNamespace rdfNamespace;
    
    private VocabularyFolder vocabulary;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(Integer displayWidth) {
        this.displayWidth = displayWidth;
    }

    public Integer getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(Integer displayHeight) {
        this.displayHeight = displayHeight;
    }

    public boolean isLanguageUsed() {
        return languageUsed;
    }

    public void setLanguageUsed(boolean languageUsed) {
        this.languageUsed = languageUsed;
    }

    public boolean isDisplayMultiple() {
        return displayMultiple;
    }

    public void setDisplayMultiple(boolean displayMultiple) {
        this.displayMultiple = displayMultiple;
    }

    public String getRdfPropertyName() {
        return rdfPropertyName;
    }

    public void setRdfPropertyName(String rdfPropertyName) {
        this.rdfPropertyName = rdfPropertyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Set<TargetEntity> getTargetEntities() {
        return targetEntities;
    }
    
    public void setTargetEntities(Set<TargetEntity> targetEntities) {
        this.targetEntities = targetEntities;
    }
    
    public DisplayType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
    }

    public AttributeDataType getDataType() {
        return dataType;
    }

    public void setDataType(AttributeDataType dataType) {
        this.dataType = dataType;
    }

    public ObligationType getObligationType() {
        return obligationType;
    }

    public void setObligationType(ObligationType obligationType) {
        this.obligationType = obligationType;
    }

    public ValueInheritanceMode getValueInheritanceMode() {
        return valueInheritanceMode;
    }

    public void setValueInheritanceMode(ValueInheritanceMode valueInheritanceMode) {
        this.valueInheritanceMode = valueInheritanceMode;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public RdfNamespace getRdfNamespace() {
        return rdfNamespace;
    }

    public void setRdfNamespace(RdfNamespace rdfNamespace) {
        this.rdfNamespace = rdfNamespace;
    }

    public VocabularyFolder getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(VocabularyFolder vocabulary) {
        this.vocabulary = vocabulary;
    }    
    
}
