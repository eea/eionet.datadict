package eionet.datadict.model;

import java.util.Iterator;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

public class Vocabulary implements Iterable<Concept>, AttributeOwner {

    @Id
    private Integer id;
    private String identifier;
    
    @ManyToOne
    private VocabularySet vocabularySet;
    @OneToMany(mappedBy = "vocabulary")
    private Set<Concept> concepts;
    private Set<Attribute> attributes;
    @OneToMany(mappedBy = "owner")
    private Set<SimpleAttributeValues> simpleAttributesValues;

    @Override
    public AttributeOwnerCategory getAttributeOwnerCategory() {
        return AttributeOwnerCategory.VOCABULARY;
    }
    
    @Override
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

    public VocabularySet getVocabularySet() {
        return vocabularySet;
    }

    public void setVocabularySet(VocabularySet vocabularySet) {
        this.vocabularySet = vocabularySet;
    }
    
    public Set<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(Set<Concept> concepts) {
        this.concepts = concepts;
    }

    @Override
    public Iterator<Concept> iterator() {
        return this.concepts.iterator();
    }
    
    @Override
    public Set<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Set<SimpleAttributeValues> getSimpleAttributesValues() {
        return simpleAttributesValues;
    }

    @Override
    public void setSimpleAttributesValues(Set<SimpleAttributeValues> simpleAttributeValues) {
        this.simpleAttributesValues = simpleAttributeValues;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof Vocabulary)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        Vocabulary other = (Vocabulary) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}
