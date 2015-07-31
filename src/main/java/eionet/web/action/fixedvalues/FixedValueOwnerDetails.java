package eionet.web.action.fixedvalues;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public final class FixedValueOwnerDetails {
    
    private int id;
    private String caption;
    private String uri;
    private String entityName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
}
