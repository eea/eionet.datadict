package eionet.datadict.services.acl;

public enum Permission {

    VIEW("v"),
    INSERT("i"),
    UPDATE("u"),
    DELETE("d");
    
    private final String value;
    
    private Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
}
