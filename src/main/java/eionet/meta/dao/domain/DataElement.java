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

import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.StringEncoder;
import eionet.util.Util;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Data element.
 *
 * @author Juhan Voolaid
 */
public class DataElement {

    /**
     * Id.
     */
    private int id;
    /**
     * Identifier.
     */
    private String identifier;
    /**
     * Shortname.
     */
    private String shortName;
    /**
     * Type.
     */
    private String type;
    /**
     * Status.
     */
    private String status;
    /**
     * Modified date.
     */
    private Date modified;
    /**
     * Table name.
     */
    private String tableName;

    /**
     * parent namespace ID.
     * NULL if common element
     */
    private Integer parentNamespace;
    /**
     * Dataset name.
     */
    private String dataSetName;
    /**
     * Working user.
     */
    private String workingUser;
    /**
     * Working copy.
     */
    private boolean workingCopy;
    //TODO - make a new DAO entity for VOCABULARY_CONCEPT_ELEMENT
    /**
     * Value from VOCABULARY_CONCEPT_ELEMENT table. Expected to be IRI encoded in DB.
     */
    private String attributeValue;
    /**
     * Language from VOCABULARY_CONCEPT_ELEMENT table.
     */
    private String attributeLanguage;
    /**
     * Related concept id.
     */
    private Integer relatedConceptId;
    /**
     * Related concept identifier.
     */
    private String relatedConceptIdentifier;
    /**
     * Related concept identifier.
     */
    private String relatedConceptLabel;
    /**
     * Related concept vocabulary identifier.
     */
    private String relatedConceptVocabulary;
    /**
     * Related concept vocabulary set identifier.
     */
    private String relatedConceptVocSet;
    /**
     * Related concept vocabulary base URI.
     */
    private String relatedConceptBaseURI;
    /**
     * attribute metadata in M_ATTRIBUTE.
     */
    private Map<String, List<String>> elemAttributeValues;
    /**
     * fixed values.
     */
    private List<FixedValue> fixedValues;
    /**
     * relation to a vocabulary if fixed values element are from a vocabulary.
     */
    private Integer vocabularyId;
    /**
     * if element gets fxv from a vocabulary shows if all concepts are valid.
     * if false only concepts released before releasing the element and not marked
     * obsolete are valid.
     */
    private Boolean allConceptsValid;
    /**
     * update date.
     */
    private String date;
    /**
     * Name attribute value is saved in this variable for better performance in search.
     */
    private String name;

    public String getStatusImage() {
        return Util.getStatusImage(status);
    }

    public boolean isReleased() {
        return StringUtils.equalsIgnoreCase("Released", status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * indicates if element is taken from an external schema.
     *
     * @return true if identifier contains colon, for example geo:lat
     */
    public boolean isExternalSchema() {
        return StringUtils.contains(identifier, ":");
    }

    /**
     * returns external namespace prefix.
     *
     * @return NS prefix. null if an internal namespace
     */
    public String getNameSpacePrefix() {
        return isExternalSchema() ? StringUtils.substringBefore(identifier, ":") : null;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }

    public boolean isFixedValuesElement() {
        return type != null && type.equalsIgnoreCase("CH1");
    }

    public Map<String, List<String>> getElemAttributeValues() {
        return elemAttributeValues;
    }

    public void setElemAttributeValues(Map<String, List<String>> elemAttributeValues) {
        this.elemAttributeValues = elemAttributeValues;
    }

    /**
     * Returns Datatype.
     *
     * @return Datatype in M_ATTRIBUTES. If not specified, "string" is returned
     */
    public String getDatatype() {
        String dataType = "string";
        List<String> elemDatatypeAttr =
                elemAttributeValues != null && elemAttributeValues.containsKey("Datatype") ? elemAttributeValues.get("Datatype")
                        : null;

        return elemDatatypeAttr != null ? elemDatatypeAttr.get(0) : dataType;
    }

    public String getAttributeLanguage() {
        return StringUtils.trimToNull(attributeLanguage);
    }

    public void setAttributeLanguage(String attributeLanguage) {
        this.attributeLanguage = StringUtils.trimToNull(attributeLanguage);
    }


    /**
     * Checks if given element is used for describing relations.
     *
     * @return true if an relation element
     */
    public boolean isRelationalElement() {
        //this DAO class is used for metadata and data element with values
        return (relatedConceptId != null && relatedConceptId != 0) || getDatatype().equals("localref");
    }

    public Integer getRelatedConceptId() {
        return relatedConceptId;
    }

    public void setRelatedConceptId(Integer relatedConceptId) {
        this.relatedConceptId = relatedConceptId;
    }

    public String getRelatedConceptIdentifier() {
        return relatedConceptIdentifier;
    }

    public void setRelatedConceptIdentifier(String relatedConceptIdentifier) {
        this.relatedConceptIdentifier = relatedConceptIdentifier;
    }

    public String getRelatedConceptLabel() {
        return relatedConceptLabel;
    }

    public void setRelatedConceptLabel(String relatedConceptLabel) {
        this.relatedConceptLabel = relatedConceptLabel;
    }

    /**
     * Generate the relative path to a concept in a different vocabulary in the same data dictionary.
     * The path looks like "common/nuts/AT111".
     *
     * @return the path
     */
    public String getRelatedConceptRelativePath() {
        return relatedConceptVocSet + "/" + relatedConceptVocabulary + "/" + relatedConceptIdentifier;
    }

    /**
     * Generate the full URI to a related concept. The concept can be specified
     * as a foreign key reference to another concept in the database or it
     * can be specified as a text string.
     *
     * @return the url - IRI encoded.
     */
    public String getRelatedConceptUri() {
        if (isRelationalElement()) {
            if (StringUtils.isNotEmpty(this.relatedConceptBaseURI)) {
                return this.relatedConceptBaseURI + this.relatedConceptIdentifier;
            }
            return StringEncoder.encodeToIRI(Props.getRequiredProperty(PropsIF.DD_URL)
                    + "/vocabulary/"
                    + getRelatedConceptRelativePath());
        } else {
            return attributeValue;
        }
    }

    public String getRelatedConceptVocabulary() {
        return relatedConceptVocabulary;
    }

    public void setRelatedConceptVocabulary(String relatedConceptVocabulary) {
        this.relatedConceptVocabulary = relatedConceptVocabulary;
    }

    public String getRelatedConceptVocSet() {
        return relatedConceptVocSet;
    }

    public void setRelatedConceptVocSet(String relatedConceptVocSet) {
        this.relatedConceptVocSet = relatedConceptVocSet;
    }

    public String getRelatedConceptBaseURI() {
        return relatedConceptBaseURI;
    }

    /**
     * Sets related base uri if input is not empty string.
     *
     * @param relatedConceptBaseURI base uri
     */
    public void setRelatedConceptBaseURI(String relatedConceptBaseURI) {
        this.relatedConceptBaseURI = StringUtils.trimToNull(relatedConceptBaseURI);
        if (StringUtils.isNotEmpty(this.relatedConceptBaseURI) && !StringUtils.endsWith(this.relatedConceptBaseURI, "/")) {
            this.relatedConceptBaseURI += "/";
        }
    }

    /**
     * returns Name attribute. Short name if data element does not have name.
     *
     * @return name in ATTRIBUTES table, default is empty string
     */
    public String getName() {

        if (name != null) {
            return name;
        }

        if (elemAttributeValues != null) {
            if (elemAttributeValues.containsKey("Name")) {
                return elemAttributeValues.get("Name").get(0);
            }
        }

        return "";
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Indicates if Element values can have values in several languages.
     *
     * @return is Language used in ATTRIBUTES table
     */
    public boolean isLanguageUsed() {
        if (elemAttributeValues != null) {
            if (elemAttributeValues.containsKey("languageUsed")) {
                String lang = elemAttributeValues.get("languageUsed").get(0);
                //TODO - change to check only one value if some solution is made for boolean attributes, see #16975
                return lang.equals("1") || lang.equalsIgnoreCase("Yes") || lang.equalsIgnoreCase("true");
            }
        }

        return false;
    }

    public Integer getParentNamespace() {
        return parentNamespace;
    }

    public void setParentNamespace(Integer parentNamespace) {
        this.parentNamespace = parentNamespace;
    }

    public boolean isCommonElement() {
        return parentNamespace == null;
    }

    @Override
    public String toString() {
        return identifier;
    }

    public Integer getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(Integer vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public Boolean getAllConceptsValid() {
        return allConceptsValid;
    }

    public void setAllConceptsValid(Boolean allConceptsValid) {
        this.allConceptsValid = allConceptsValid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    /**
     * MD5 hash of the value similar to the DB unique key.
     * @return md5 hash of element values.
     */
    public String getUniqueValueHash() {
        return Util.md5((getId() + "," + (getRelatedConceptId() != null ? getRelatedConceptId() : getAttributeValue())
                + "@" + StringUtils.defaultString(getAttributeLanguage())));
    }

    /**
     * String representation of the element attribute value.
     *
     * @return value or related concept label depending on the element type
     */
    public String getValueText() {
        return isRelationalElement()
                ? StringUtils.trimToEmpty(getRelatedConceptLabel()) : StringUtils.trimToEmpty(getAttributeValue())
                + (getAttributeLanguage() != null ? " [" + getAttributeLanguage() + "]" : "");
    }
}
