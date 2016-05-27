package eionet.datadict.resources;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class VocabularyIdInfo implements ResourceIdInfo {

    private final String vocabularySetIdentifier;
    private final String vocabularyIdentifier;

    public VocabularyIdInfo(String vocabularySetIdentifier, String vocabularyIdentifier) {
        this.vocabularySetIdentifier = vocabularySetIdentifier;
        this.vocabularyIdentifier = vocabularyIdentifier;
    }

    public String getVocabularySetIdentifier() {
        return vocabularySetIdentifier;
    }

    public String getVocabularyIdentifier() {
        return vocabularyIdentifier;
    }
    
}
