package eionet.datadict.model;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class VocabularySet {
    
    private Integer id;
    private String identifier;
    private String label;

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
    
}
