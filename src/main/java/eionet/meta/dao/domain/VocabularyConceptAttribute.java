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
public class VocabularyConceptAttribute {

    private int id;

    private int attributeId;

    private int vocabularyConceptId;

    private String label;

    private String value;

    private String language;

    private boolean languageUsed;

    private String inputType;

    private String dataType;

    private int width;

    private int height;

    private boolean multiValue;

    private String rdfProperty;

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
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
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
     * @return the attributeId
     */
    public int getAttributeId() {
        return attributeId;
    }

    /**
     * @param attributeId
     *            the attributeId to set
     */
    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
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
     * @return the inputType
     */
    public String getInputType() {
        return inputType;
    }

    /**
     * @param inputType
     *            the inputType to set
     */
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param dataType
     *            the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the multiValue
     */
    public boolean isMultiValue() {
        return multiValue;
    }

    /**
     * @param multiValue
     *            the multiValue to set
     */
    public void setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
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

}
