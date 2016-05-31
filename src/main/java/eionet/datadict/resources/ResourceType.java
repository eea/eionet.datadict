package eionet.datadict.resources;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public enum ResourceType {

    VOCABULARY_SET("Vocabulary set"),
    VOCABULARY("Vocabulary"),
    ATTRIBUTE("Attribute"),
    DATASET("Dataset");
    
    private final String label;
    
    private ResourceType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
    
}
