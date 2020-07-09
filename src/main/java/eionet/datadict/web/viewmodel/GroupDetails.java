package eionet.datadict.web.viewmodel;

public class GroupDetails {

    private String groupNameOptionOne;

    private String userName;

    private String groupNameOptionTwo;

    private String ldapGroupName;

    public GroupDetails() {

    }

    public String getGroupNameOptionOne() {
        return groupNameOptionOne;
    }

    public void setGroupNameOptionOne(String groupNameOptionOne) {
        this.groupNameOptionOne = groupNameOptionOne;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupNameOptionTwo() {
        return groupNameOptionTwo;
    }

    public void setGroupNameOptionTwo(String groupNameOptionTwo) {
        this.groupNameOptionTwo = groupNameOptionTwo;
    }

    public String getLdapGroupName() {
        return ldapGroupName;
    }

    public void setLdapGroupName(String ldapGroupName) {
        this.ldapGroupName = ldapGroupName;
    }
}
