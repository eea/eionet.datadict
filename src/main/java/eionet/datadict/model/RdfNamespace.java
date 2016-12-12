package eionet.datadict.model;

/**
 *
 * @author Aliki Kopaneli
 */
public class RdfNamespace {
    
    private Integer id;
    private String uri;
    private String prefix;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
}
