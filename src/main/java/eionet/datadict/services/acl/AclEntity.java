package eionet.datadict.services.acl;

public enum AclEntity {
    
    ATTRIBUTE("/attributes");
    
    private final String value;
    
    private AclEntity(String value) {
        this.value = value;
    }

    public String getPath() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }
    
}
