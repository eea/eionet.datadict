/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.dao.domain;

import java.util.Date;

/**
 * Vocabulary folder.
 *
 * @author Juhan Voolaid
 */
public class VocabularyFolder {

    /** Properties. */
    private int id;
    private String identifier;
    private String continuityId;
    private String label;
    private RegStatus regStatus = RegStatus.DRAFT;
    private boolean workingCopy;
    private String workingUser;
    private Date dateModified;
    private String userModified;
    private int checkedOutCopyId;
    private boolean numericConceptIdentifiers;
    private String baseUri;
    private VocabularyType type;
    private String folderName;

    /**
     * True, if status is "Draft".
     *
     * @return
     */
    public boolean isDraftStatus() {
        if (RegStatus.DRAFT.equals(regStatus)) {
            return true;
        }
        return false;
    }

    /**
     * True, if type is "COMMON".
     *
     * @return
     */
    public boolean isCommonType() {
        if (VocabularyType.COMMON.equals(type)) {
            return true;
        }
        return false;
    }

    /**
     * True, if type is "SITE_CODE".
     *
     * @return
     */
    public boolean isSiteCodeType() {
        if (VocabularyType.SITE_CODE.equals(type)) {
            return true;
        }
        return false;
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
     * @return the workingCopy
     */
    public boolean isWorkingCopy() {
        return workingCopy;
    }

    /**
     * @param workingCopy
     *            the workingCopy to set
     */
    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
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
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
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
     * @return the numericConceptIdentifiers
     */
    public boolean isNumericConceptIdentifiers() {
        return numericConceptIdentifiers;
    }

    /**
     * @param numericConceptIdentifiers
     *            the numericConceptIdentifiers to set
     */
    public void setNumericConceptIdentifiers(boolean numericConceptIdentifiers) {
        this.numericConceptIdentifiers = numericConceptIdentifiers;
    }

    /**
     * @return the baseUri
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * @param baseUri
     *            the baseUri to set
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    /**
     * @return the type
     */
    public VocabularyType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(VocabularyType type) {
        this.type = type;
    }

    /**
     * @return the folderName
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * @param folderName
     *            the folderName to set
     */
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

}
