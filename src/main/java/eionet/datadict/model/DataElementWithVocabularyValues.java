package eionet.datadict.model;

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
    public AttributeOwnerType getAttributeOwnerType() {
        return new AttributeOwnerType(super.getId(), AttributeOwnerType.Type.DATA_ELEMENT_WITH_VALUE_LIST);
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

}
