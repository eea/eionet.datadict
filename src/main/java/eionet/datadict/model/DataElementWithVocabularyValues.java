package eionet.datadict.model;

import java.util.Set;
import javax.persistence.ManyToOne;

public class DataElementWithVocabularyValues extends DataElement {

    @ManyToOne
    private Vocabulary vocabulary;

    public DataElementWithVocabularyValues() {
        super();
    }

    public DataElementWithVocabularyValues(Integer id) {
        super(id);
    }
    
    @Override
    public AttributeOwnerCategory getAttributeOwnerCategory() {
        return AttributeOwnerCategory.DATA_ELEMENT_WITH_VALUE_LIST;
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.VOCABULARY;
    }

    @Override
    public boolean supportsValueList() {
        return true;
    }

    @Override
    public Iterable<Concept> getValueList() {
        return vocabulary;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    @Override
    public Set<Attribute> getAttributes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributes(Set<Attribute> attributes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
