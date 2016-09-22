package eionet.datadict.model;

public class Namespace {

    private Integer id;
    private String shortName;
    private String fullName;
    private String definition;
    private String workingUser;
    
    private Namespace parent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public Namespace getParent() {
        return parent;
    }

    public void setParent(Namespace parent) {
        this.parent = parent;
    }
    
}
