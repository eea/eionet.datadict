package eionet.datadict.model;

import eionet.datadict.util.IteratorUpcastAdapter;
import java.util.Iterator;
import java.util.List;

public class Vocabulary implements ValueList, SimpleAttributeOwner {

    private Integer id;
    private String identifier;
    
    private List<Concept> concepts;
    private List<SimpleAttribute> simpleAttributes;
    private List<SimpleAttributeValues> simpleAttributesValues;

    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.VOCABULARY;
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

    public List<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<Concept> concepts) {
        this.concepts = concepts;
    }

    @Override
    public Iterator<ValueListItem> iterator() {
        return new IteratorUpcastAdapter<ValueListItem, Concept>(this.concepts.iterator());
    }
    
    @Override
    public List<SimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

    @Override
    public void setSimpleAttributes(List<SimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

    @Override
    public List<SimpleAttributeValues> getSimpleAttributesValues() {
        return simpleAttributesValues;
    }

    @Override
    public void setSimpleAttributesValues(List<SimpleAttributeValues> simpleAttributeValues) {
        this.simpleAttributesValues = simpleAttributeValues;
    }
    
}
