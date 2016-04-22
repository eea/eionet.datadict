package eionet.datadict.resources;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class VocabularySetIdInfo implements ResourceIdInfo {

    private final String identifier;
    
    public VocabularySetIdInfo(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
    
}
