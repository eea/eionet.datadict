package eionet.datadict.model;

import java.io.Serializable;

public class LdapUser implements Serializable  {

    private Integer id;

    private String uid;

    private String fullName;

    public LdapUser() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setFullName(String fullname) {
        this.fullName = fullname;
    }

    public String getFullName() {return fullName;}
}
