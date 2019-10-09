package eionet.meta.dao.domain;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class SchemaSet {

    public static final String ROOT_IDENTIFIER = "root";

    /** */
    private int id;
    private String identifier;
    private String continuityId;
    private RegStatus regStatus = RegStatus.DRAFT;
    private boolean isWorkingCopy;
    private String workingUser;
    private Date dateModified;
    private String userModified;
    private String comment;
    private int checkedOutCopyId;
    /**
     * Status modification date.
     */
    private Date statusModified;
    /**
     * Status modification flag. Set true when status is changed.
     */
    private boolean isStatusModified = false;

    /** Name attribute value. */
    private String nameAttribute;

    /** Relational properties. */
    private Map<String, List<String>> attributeValues;

    /**
     * Helper method for checking, if SchemaSet is in "DRAFT" status.
     *
     * @return boolean
     */
    public boolean isDraftStatus() {
        return RegStatus.DRAFT.equals(regStatus);
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
     */
    public SchemaSet() {
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
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
     * Set the registration status. If status changed, it updates modified date as well.
     *
     * @param regStatus
     *            the regStatus to set
     */
    public void setRegStatus(RegStatus regStatus) {
        if (this.regStatus != null && !this.regStatus.equals(regStatus)) {
            setStatusModified(new Timestamp(System.currentTimeMillis()));
        }
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
     * @return the date
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     * @param dateModified
     *            the date to set
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
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
     * @return the user
     */
    public String getUserModified() {
        return userModified;
    }

    /**
     * @param userModified
     *            the user to set
     */
    public void setUserModified(String userModified) {
        this.userModified = userModified;
    }

    /**
     *
     * @return boolean
     */
    public boolean isCheckedOut() {
        return !isWorkingCopy && (workingUser != null && !workingUser.isEmpty());
    }

    /**
     *
     * @param userName
     *            user name
     * @return boolean
     */
    public boolean isWorkingCopyOf(String userName) {
        return isWorkingCopy && workingUser != null && workingUser.equals(userName);
    }

    /**
     *
     * @param userName
     *            user name
     * @return boolean
     */
    public boolean isCheckedOutBy(String userName) {
        return !isWorkingCopy && workingUser != null && workingUser.equals(userName);
    }

    /**
     *
     * @return boolean
     */
    public boolean isReleased() {
        return regStatus != null && regStatus.equals(RegStatus.RELEASED);
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
     * Helper method for checking, if SchemaSet is in "Deprecated" status.
     *
     * @return boolean
     */
    public boolean isDeprecatedStatus() {
        return RegStatus.DEPRECATED.equals(regStatus);
    }

    public Date getStatusModified() {
        return statusModified;
    }

    public void setStatusModified(Date statusModified) {
        this.statusModified = statusModified;
    }
}
