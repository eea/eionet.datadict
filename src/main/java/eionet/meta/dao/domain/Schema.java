package eionet.meta.dao.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private RegStatus regStatus;
    private RegStatus schemaSetRegStatus;
    private boolean isWorkingCopy;
    private String workingUser;
    private Date dateModified;
    private String userModified;
    private String comment;
    private int checkedOutCopyId;
    private boolean otherDocument;

    /** Relational properties. */
    private String schemaSetIdentifier;
    private boolean schemaSetWorkingCopy;
    private String schemaSetWorkingUser;
    private String nameAttribute;
    private String schemaSetNameAttribute;

    /** */
    private Map<String, List<String>> attributeValues;

    /**
     * Helper method for checking, if SchemaSet is in "DRAFT" status.
     *
     * @return
     */
    public boolean isDraftStatus() {
        return RegStatus.DRAFT.equals(regStatus);
    }

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

    /**
     *
     * @param userName
     * @return
     */
    public boolean isCheckedOutBy(String userName) {
        return isWorkingCopy == false && workingUser != null && workingUser.equals(userName);
    }

    /**
     * @return the attributeValues
     */
    public Map<String, List<String>> getAttributeValues() {
        return attributeValues;
    }

    /**
     * @param attributeValues
     *            the attributeValues to set
     */
    public void setAttributeValues(Map<String, List<String>> attributeValues) {
        this.attributeValues = attributeValues;
    }

    /**
     *
     * @return
     */
    public boolean isReleased() {
        return regStatus != null && regStatus.equals(RegStatus.RELEASED);
    }

    /**
     * @return the schemaSetWorkingCopy
     */
    public boolean isSchemaSetWorkingCopy() {
        return schemaSetWorkingCopy;
    }

    /**
     * @param schemaSetWorkingCopy
     *            the schemaSetWorkingCopy to set
     */
    public void setSchemaSetWorkingCopy(boolean schemaSetWorkingCopy) {
        this.schemaSetWorkingCopy = schemaSetWorkingCopy;
    }

    /**
     * @return the schemaSetWorkingUser
     */
    public String getSchemaSetWorkingUser() {
        return schemaSetWorkingUser;
    }

    /**
     * @param schemaSetWorkingUser
     *            the schemaSetWorkingUser to set
     */
    public void setSchemaSetWorkingUser(String schemaSetWorkingUser) {
        this.schemaSetWorkingUser = schemaSetWorkingUser;
    }

    /**
     * @return the nameAttribute
     */
    public String getNameAttribute() {
        return nameAttribute;
    }

    /**
     * @param nameAttribute
     *            the nameAttribute to set
     */
    public void setNameAttribute(String nameAttribute) {
        this.nameAttribute = nameAttribute;
    }

    /**
     * @return the schemaSetNameAttribute
     */
    public String getSchemaSetNameAttribute() {
        return schemaSetNameAttribute;
    }

    /**
     * @param schemaSetNameAttribute
     *            the schemaSetNameAttribute to set
     */
    public void setSchemaSetNameAttribute(String schemaSetNameAttribute) {
        this.schemaSetNameAttribute = schemaSetNameAttribute;
    }

    /**
     * @return the schemaSetRegStatus
     */
    public RegStatus getSchemaSetRegStatus() {
        return schemaSetRegStatus;
    }

    /**
     * @param schemaSetRegStatus
     *            the schemaSetRegStatus to set
     */
    public void setSchemaSetRegStatus(RegStatus schemaSetRegStatus) {
        this.schemaSetRegStatus = schemaSetRegStatus;
    }

    /**
     * @return the otherDocument
     */
    public boolean isOtherDocument() {
        return otherDocument;
    }

    /**
     * @param otherDocument
     *            the otherDocument to set
     */
    public void setOtherDocument(boolean otherDocument) {
        this.otherDocument = otherDocument;
    }

}
