package eionet.datadict.model;

import java.io.Serializable;

public class User implements Serializable  {

    private Integer id;

    private String fullName;

    public User() {

    }

    public void setFullName(String fullname) {
        this.fullName = fullname;
    }

    public String getFullName() {return fullName;}
}
