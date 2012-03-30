package eionet.meta.dao.domain;

import java.util.Date;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class SchemaSet {

    /** */
    private int id;
    private String identifier;
    private int continuityId;
    private RegStatus regStatus = RegStatus.DRAFT;
    private boolean isWorkingCopy;
    private String workingUser;
    private Date date;
    private String user;
    private String comment;
    private int checkedOutCopyId;

    public SchemaSet(){
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the continuityId
     */
    public int getContinuityId() {
        return continuityId;
    }

    /**
     * @param continuityId the continuityId to set
     */
    public void setContinuityId(int continuityId) {
        this.continuityId = continuityId;
    }

    /**
     * @return the regStatus
     */
    public RegStatus getRegStatus() {
        return regStatus;
    }

    /**
     * @param regStatus the regStatus to set
     */
    public void setRegStatus(RegStatus regStatus) {
        this.regStatus = regStatus;
    }

    /**
     * @return the isWorkingCopy
     */
    public boolean isWorkingCopy() {
        return isWorkingCopy;
    }

    /**
     * @param isWorkingCopy the isWorkingCopy to set
     */
    public void setWorkingCopy(boolean isWorkingCopy) {
        this.isWorkingCopy = isWorkingCopy;
    }

    /**
     * @return the workingUser
     */
    public String getWorkingUser() {
        return workingUser;
    }

    /**
     * @param workingUser the workingUser to set
     */
    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the checkedOutCopyId
     */
    public int getCheckedOutCopyId() {
        return checkedOutCopyId;
    }

    /**
     * @param checkedOutCopyId the checkedOutCopyId to set
     */
    public void setCheckedOutCopyId(int checkedOutCopyId) {
        this.checkedOutCopyId = checkedOutCopyId;
    }

    /**
     * 
     * @author Jaanus Heinlaid
     *
     */
    public enum RegStatus {

        // TODO: Maybe this enum could be in a better place, since
        // it is not necessarily specific to SchemaSets, but should
        // also be used for Schemas.

        /** */
        DRAFT("Draft"),RELEASED("Released");

        /** */
        String s;

        /**
         * 
         * @param s
         */
        RegStatus(String s){
            this.s = s;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        public String toString(){
            return s;
        }

        /**
         * 
         * @param s
         * @return
         */
        public static RegStatus fromString(String s){
            for (RegStatus regStatus : RegStatus.values()){
                if (regStatus.toString().equals(s)){
                    return regStatus;
                }
            }
            return null;
        }

        /**
         * 
         * @return
         */
        public static RegStatus getDefault(){
            return RegStatus.DRAFT;
        }
    }

    private void test(){

    }
    public static void main(String[] args) {
        SchemaSet schema = new SchemaSet();
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }
}
