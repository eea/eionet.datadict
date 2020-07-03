package eionet.datadict.model;

import java.io.Serializable;

public class LdapRole implements Serializable {

    private String name;

    private String fullDn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullDn() {
        return fullDn;
    }

    public void setFullDn(String fullDn) {
        this.fullDn = fullDn;
    }
}
