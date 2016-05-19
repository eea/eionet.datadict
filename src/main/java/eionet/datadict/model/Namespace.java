package eionet.datadict.model;

/**
 *
 * @author Aliki Kopaneli
 */
public class Namespace {
    
    private int namespaceID;
    private int parentNS;
    
    private String shortName;
    private String fullName;
    private String definition;
    private String workingUser;

    public int getNamespaceID() {
        return namespaceID;
    }

    public void setNamespaceID(int namespaceID) {
        this.namespaceID = namespaceID;
    }

    public int getParentNS() {
        return parentNS;
    }

    public void setParentNS(int parentNS) {
        this.parentNS = parentNS;
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
}
