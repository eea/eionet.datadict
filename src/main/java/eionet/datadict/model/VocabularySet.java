package eionet.datadict.model;

import java.util.Set;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class VocabularySet {
    
    @Id
    private Integer id;
    private String identifier;
    private String label;

    @OneToMany(mappedBy = "vocabularySet")
    private Set<Vocabulary> vocabularies;
    
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<Vocabulary> getVocabularies() {
        return vocabularies;
    }

    public void setVocabularies(Set<Vocabulary> vocabularies) {
        this.vocabularies = vocabularies;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof VocabularySet)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        VocabularySet other = (VocabularySet) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}
