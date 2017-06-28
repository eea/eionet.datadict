package eionet.datadict.model;

import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang.StringUtils;

@Table(name="VOCABULARY_CONCEPT")
public class Concept implements ValueListItem {

    @ManyToOne
    private Vocabulary vocabulary;
    
    @Id
    private Integer id;
    private String identifier;
    private String notation;
    private String label;
    private String definition;

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public String getCode() {
        return StringUtils.isBlank(this.notation) ? this.identifier : this.notation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof Concept)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        Concept other = (Concept) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}
