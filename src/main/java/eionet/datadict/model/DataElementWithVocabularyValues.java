package eionet.datadict.model;

public class DataElementWithVocabularyValues extends DataElement {

    private Vocabulary vocabulary;
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_ELEMENT_WITH_VALUE_LIST;
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
    public ValueList getValueList() {
        return vocabulary;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }
    
}
