package eionet.meta.dao.domain;

import java.util.Date;

import eionet.meta.dao.domain.SchemaSet.RegStatus;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class Schema {

    /** */
    private int id;
    private String fileName;
    private int schemaSetId;
    private String continuityId;
    private RegStatus regStatus = RegStatus.DRAFT;
    private boolean isWorkingCopy;
    private String workingUser;
    private Date dateModified;
    private String userModified;
    private String comment;
    private int checkedOutCopyId;

    /** Relational properties. */
    private String schemaSetIdentifier;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the schemaSetId
     */
    public int getSchemaSetId() {
        return schemaSetId;
    }

    /**
     * @param schemaSetId
     *            the schemaSetId to set
     */
    public void setSchemaSetId(int schemaSetId) {
        this.schemaSetId = schemaSetId;
    }

    /**
     * @return the continuityId
     */
    public String getContinuityId() {
        return continuityId;
    }

    /**
     * @param continuityId
     *            the continuityId to set
     */
    public void setContinuityId(String continuityId) {
        this.continuityId = continuityId;
    }

    /**
     * @return the regStatus
     */
    public RegStatus getRegStatus() {
        return regStatus;
    }

    /**
     * @param regStatus
     *            the regStatus to set
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
     * @param isWorkingCopy
     *            the isWorkingCopy to set
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
     * @param workingUser
     *            the workingUser to set
     */
    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    /**
     * @return the dateModified
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     * @param dateModified
     *            the dateModified to set
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * @return the userModified
     */
    public String getUserModified() {
        return userModified;
    }

    /**
     * @param userModified
     *            the userModified to set
     */
    public void setUserModified(String userModified) {
        this.userModified = userModified;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
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
     * @param checkedOutCopyId
     *            the checkedOutCopyId to set
     */
    public void setCheckedOutCopyId(int checkedOutCopyId) {
        this.checkedOutCopyId = checkedOutCopyId;
    }

    /**
     * @return the schemaSetIdentifier
     */
    public String getSchemaSetIdentifier() {
        return schemaSetIdentifier;
    }

    /**
     * @param schemaSetIdentifier
     *            the schemaSetIdentifier to set
     */
    public void setSchemaSetIdentifier(String schemaSetIdentifier) {
        this.schemaSetIdentifier = schemaSetIdentifier;
    }

    /**
     *
     * @param userName
     * @return
     */
    public boolean isWorkingCopyOf(String userName) {
        return isWorkingCopy && workingUser != null && workingUser.equals(userName);
    }
}
