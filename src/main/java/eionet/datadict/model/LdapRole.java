package eionet.datadict.model;

import java.io.Serializable;

public class LdapRole implements Serializable {

    private Integer id;

    private String name;

    private String fullDn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
