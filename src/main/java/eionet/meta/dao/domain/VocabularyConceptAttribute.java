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

/**
 * Vocabulary concept attribute.
 *
 * @author Juhan Voolaid
 */
public class VocabularyConceptAttribute extends SimpleAttribute {

    /** Attribute short names (M_ATTRIBUTE.SHORT_NAME). */
    public static final String BROADER_LOCAL_CONCEPT = "broaderLocalConcept";
    public static final String NARROWER_LOCAL_CONCEPT = "narrowerLocalConcept";
    public static final String RELATED_LOCAL_CONCEPT = "relatedLocalConcept";

    private int id;

    private int vocabularyConceptId;

    private Integer relatedId;

    private String language;

    private boolean languageUsed;

    private String rdfProperty;

    private String linkText;

    /** Related concept properties. */
    private String relatedIdentifier;

    /**
     * @return the vocabularyConceptId
     */
    public int getVocabularyConceptId() {
        return vocabularyConceptId;
    }

    /**
     * @param vocabularyConceptId
     *            the vocabularyConceptId to set
     */
    public void setVocabularyConceptId(int vocabularyConceptId) {
        this.vocabularyConceptId = vocabularyConceptId;
    }

    /**
     * @return the relatedId
     */
    public Integer getRelatedId() {
        return relatedId;
    }

    /**
     * @param relatedId
     *            the relatedId to set
     */
    public void setRelatedId(Integer relatedId) {
        this.relatedId = relatedId;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the languageUsed
     */
    public boolean isLanguageUsed() {
        return languageUsed;
    }

    /**
     * @param languageUsed
     *            the languageUsed to set
     */
    public void setLanguageUsed(boolean languageUsed) {
        this.languageUsed = languageUsed;
    }

    /**
     * @return the rdfProperty
     */
    public String getRdfProperty() {
        return rdfProperty;
    }

    /**
     * @param rdfProperty
     *            the rdfProperty to set
     */
    public void setRdfProperty(String rdfProperty) {
        this.rdfProperty = rdfProperty;
    }

    /**
     * @return the linkText
     */
    public String getLinkText() {
        return linkText;
    }

    /**
     * @param linkText
     *            the linkText to set
     */
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    /**
     * @return the relatedIdentifier
     */
    public String getRelatedIdentifier() {
        return relatedIdentifier;
    }

    /**
     * @param relatedIdentifier
     *            the relatedIdentifier to set
     */
    public void setRelatedIdentifier(String relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
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

}
