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

package eionet.meta.service.data;

import java.util.List;

import eionet.meta.dao.domain.StandardGenericStatus;

/**
 * Vocabulary concept search filter.
 *
 * @author Juhan Voolaid
 */
public class VocabularyConceptFilter extends PagedRequest {

    /** Vocabulary folder id. */
    private int vocabularyFolderId;

    /** Text search value. */
    private String text;

    /** Identifier exact search value. */
    private String identifier;

    /** Definition exact search value. */
    private String definition;

    /** Label exact search value. */
    private String label;

    /** If true, the identifier sorting is numeric. */
    private boolean numericIdentifierSorting;

    /**
     * If true, sorting will be on concept id.
     */
    private boolean orderByConceptId;

    /** Concept id's that don't get returned. */
    private List<Integer> excludedIds;

    /** Concept id's that get returned. */
    private List<Integer> includedIds;
    
    /**
     * if true only exact match is searched in textual fields.
     */
    private boolean exactMatch = false;
    /**
     * if true results are search where one or more fields contain the text as a separate word.
     */
    private boolean wordMatch = false;
    /**
     * search from vocabulary label or identifier.
     */
    private String vocabularyText;
    /** vocabulary sets to not search from. */
    private List<Integer> excludedVocabularySetIds;

    /**
     * Status of concept.
     */
    private StandardGenericStatus conceptStatus;
    /**
     * Status exact match.
     */
    private boolean statusExactMatch = false;

    /**
     * @return the vocabularyFolderId
     */
    public int getVocabularyFolderId() {
        return vocabularyFolderId;
    }

    /**
     * @param vocabularyFolderId
     *            the vocabularyFolderId to set
     */
    public void setVocabularyFolderId(int vocabularyFolderId) {
        this.vocabularyFolderId = vocabularyFolderId;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = text;
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
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition
     *            the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
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
     * @return the numericIdentifierSorting
     */
    public boolean isNumericIdentifierSorting() {
        return numericIdentifierSorting;
    }

    /**
     * @param numericIdentifierSorting
     *            the numericIdentifierSorting to set
     */
    public void setNumericIdentifierSorting(boolean numericIdentifierSorting) {
        this.numericIdentifierSorting = numericIdentifierSorting;
    }

    /**
     * @return the excludedIds
     */
    public List<Integer> getExcludedIds() {
        return excludedIds;
    }

    /**
     * @param excludedIds
     *            the excludedIds to set
     */
    public void setExcludedIds(List<Integer> excludedIds) {
        this.excludedIds = excludedIds;
    }

    /**
     * @return the includedIds
     */
    public List<Integer> getIncludedIds() {
        return includedIds;
    }

    /**
     * @param includedIds
     *            the includedIds to set
     */
    public void setIncludedIds(List<Integer> includedIds) {
        this.includedIds = includedIds;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public String getVocabularyText() {
        return vocabularyText;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public void setVocabularyText(String vocabularyText) {
        this.vocabularyText = vocabularyText;
    }

    public boolean isWordMatch() {
        return wordMatch;
    }

    public void setWordMatch(boolean wordMatch) {
        this.wordMatch = wordMatch;
    }

    public void setExcludedVocabularySetIds(List<Integer> excludedVocabularySetIds) {
        this.excludedVocabularySetIds = excludedVocabularySetIds;
    }

    public List<Integer> getExcludedVocabularySetIds() {
        return excludedVocabularySetIds;
    }

    public boolean isOrderByConceptId() {
        return orderByConceptId;
    }

    public void setOrderByConceptId(boolean orderByConceptId) {
        this.orderByConceptId = orderByConceptId;
    }
	
    public StandardGenericStatus getConceptStatus() {
        return conceptStatus;
    }

    public void setConceptStatus(StandardGenericStatus conceptStatus) {
        this.conceptStatus = conceptStatus;
    }

    public int getConceptStatusInt() {
        return conceptStatus != null ? conceptStatus.getValue() : StandardGenericStatus.ALL_MASK;
    }

    /**
     * Setter method to set conceptStatus from int value
     * @param conceptStatus int value. value should be in range, otherwise it will be null
     */
    public void setConceptStatusInt(int conceptStatus) {
        //if we receive all mask, just set it to null
        if (conceptStatus == StandardGenericStatus.ALL_MASK){
            this.conceptStatus = null;
        }
        else {
            this.conceptStatus = StandardGenericStatus.fromValue(conceptStatus);
        }
    }

    public boolean isStatusExactMatch() {
        return statusExactMatch;
    }

    public void setStatusExactMatch(boolean statusExactMatch) {
        this.statusExactMatch = statusExactMatch;
    }
}
