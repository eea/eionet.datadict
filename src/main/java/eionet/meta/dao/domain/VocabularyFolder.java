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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Vocabulary DAO class.
 *
 * @author Juhan Voolaid
 */
public class VocabularyFolder {

    /** Base URI of all vocabularies. */
    public static final String VOCABULARY_FOLDERS_BASE_URI = Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/";

    /** URI of the folder of DD's own vocabularies. */
    public static final String OWN_VOCABULARIES_FOLDER_URI = VOCABULARY_FOLDERS_BASE_URI
            + Props.getRequiredProperty(PropsIF.DD_OWN_VOCABULARIES_FOLDER_NAME);

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
    private boolean notationsEqualIdentifiers;
    
    private VocabularySet vocabularySet;

    /**
     * Includes valid reg. status values for a vocabulary.
     */
    public static final RegStatus[] VALID_REG_STATUS = new RegStatus[] {RegStatus.DRAFT, RegStatus.PUBLIC_DRAFT, RegStatus.RELEASED};

    /**
     * Joined property - folder identifier.
     */
    private String folderName;
    private String folderLabel;

    private List<List<SimpleAttribute>> attributes;

    /**
     * All vocabulary concepts.
     */
    private List<VocabularyConcept> concepts;

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
        this.baseUri = StringUtils.trimToNull(baseUri);
        if (StringUtils.isNotBlank(this.baseUri) && !StringUtils.endsWith(this.baseUri, "/") && !StringUtils.endsWith(this.baseUri, ":")
                && !StringUtils.endsWith(this.baseUri, "#")) {
            this.baseUri = this.baseUri + "/";
        }
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

    /**
     * @return the attributes
     */
    public List<List<SimpleAttribute>> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(List<List<SimpleAttribute>> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the folderId
     */
    public int getFolderId() {
        VocabularySet vocabularySet = this.getVocabularySet();
        
        return vocabularySet == null ? 0 : vocabularySet.getId();
    }

    /**
     * @param folderId
     *            the folderId to set
     */
    public void setFolderId(int folderId) {
        VocabularySet vocabularySet = this.getVocabularySet();
        
        if (vocabularySet == null) {
            vocabularySet = new VocabularySet();
            this.setVocabularySet(vocabularySet);
        }
        
        vocabularySet.setId(folderId);
    }

    public VocabularySet getVocabularySet() {
        return this.vocabularySet;
    }
    
    public void setVocabularySet(VocabularySet vocabularySet) {
        this.vocabularySet = vocabularySet;
    }
    
    /**
     * @return the folderLabel
     */
    public String getFolderLabel() {
        return folderLabel;
    }

    public void setFolderLabel(String folderLabel) {
        this.folderLabel = folderLabel;
    }

    /**
     * @return the notationsEqualIdentifiers
     */
    public boolean isNotationsEqualIdentifiers() {
        return notationsEqualIdentifiers;
    }

    /**
     * @param enforceNotationToId
     *            the notationsEqualIdentifiers to set
     */
    public void setNotationsEqualIdentifiers(boolean enforceNotationToId) {
        this.notationsEqualIdentifiers = enforceNotationToId;
    }

    public List<VocabularyConcept> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<VocabularyConcept> concepts) {
        this.concepts = concepts;
    }

    /**
     * Utility method to return baseUri for folder context.
     *
     * @param vf Vocabulary folder.
     * @return Base URI.
     */
    public static String getBaseUri(VocabularyFolder vf) {

        String result = vf.getBaseUri();
        if (StringUtils.isBlank(result)) {
            result = VOCABULARY_FOLDERS_BASE_URI + vf.getFolderName() + "/" + vf.getIdentifier() + "/";
        }
        return result;
    }

    /**
     * Returns valid reg status enum values for a vocabulary.
     *
     * @return array of RegStatus enum
     */
    public RegStatus[] getValidRegStatusForVocabulary() {
        return VALID_REG_STATUS;
    }

}
