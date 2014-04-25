package eionet.meta.service.data;

import eionet.meta.dao.domain.RegStatus;

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
 *        Kaido Laine
 */

/**
 * Helper DAO object for presenting search results.
 * besides Vocabulary Concept fields shows also required fields from parent objects:
 * Vocabulary and Vocabulary Set
 *
 * @author Kaido Laine
 */
public class VocabularyConceptData {
    /**
     * concept id.
     */
    private int id;
    /**
     * concept label.
     */
    private String label;

    /**
     * concept identifier.
     */
    private String identifier;

    /**
     * Vocabulary label.
     */
    private String vocabularyLabel;

    /**
     * Vocabulary identifier.
     */
    private String vocabularyIdentifier;

    /**
     * True if Vocabulary is working copy.
     */
    private boolean workingCopy;

    /**
     * user who has checked out the vocabulary.
     */
    private String userName;

    /**
     * Vocabulary Set label.
     */
    private String vocabularySetLabel;

    /**
     * Vocabulary Set identifier.
     */
    private String vocabularySetIdentifier;

    /**
     * status of vocabulary.
     */
    private RegStatus vocabularyStatus;

    public String getLabel() {
        return label;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getVocabularyLabel() {
        return vocabularyLabel;
    }

    public String getVocabularyIdentifier() {
        return vocabularyIdentifier;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public String getUserName() {
        return userName;
    }

    public String getVocabularySetLabel() {
        return vocabularySetLabel;
    }

    public String getVocabularySetIdentifier() {
        return vocabularySetIdentifier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setVocabularyLabel(String vocabularyLabel) {
        this.vocabularyLabel = vocabularyLabel;
    }

    public void setVocabularyIdentifier(String vocabularyIdentifier) {
        this.vocabularyIdentifier = vocabularyIdentifier;
    }

    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setVocabularySetLabel(String vocabularySetLabel) {
        this.vocabularySetLabel = vocabularySetLabel;
    }

    public void setVocabularySetIdentifier(String vocabularySetIdentifier) {
        this.vocabularySetIdentifier = vocabularySetIdentifier;
    }

    public RegStatus getVocabularyStatus() {
        return vocabularyStatus;
    }

    public void setVocabularyStatus(RegStatus vocabularyStatus) {
        this.vocabularyStatus = vocabularyStatus;
    }

}
