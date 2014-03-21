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
 * Simple attribute object.
 *
 * @author Juhan Voolaid
 */
public class
        SimpleAttribute {

    /**
     * M_ATTRIBUTE_ID.
     */
    private int attributeId;

    /**
     * DATAELEM_ID.
     */
    private int objectId;

    /**
     * M_ATTRIBUTE.SHORT_NAME.
     */
    private String identifier;

    private String label;

    private String value;

    private String inputType;

    private String dataType;

    private int width;

    private int height;

    private boolean multiValue;

    private boolean mandatory;

    private String rdfPropertyName;

    private String rdfPropertyUri;

    private String rdfPropertyPrefix;

    /**
     * @return the attributeId
     */
    public int getAttributeId() {
        return attributeId;
    }

    /**
     * @param attributeId the attributeId to set
     */
    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * @return the objectId
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * @param objectId the objectId to set
     */
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the inputType
     */
    public String getInputType() {
        return inputType;
    }

    /**
     * @param inputType the inputType to set
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
     * @param dataType the dataType to set
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
     * @param width the width to set
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
     * @param height the height to set
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
     * @param multiValue the multiValue to set
     */
    public void setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @param mandatory the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * @return the rdfPropertyName
     */
    public String getRdfPropertyName() {
        return rdfPropertyName;
    }

    /**
     * @param rdfPropertyName the rdfPropertyName to set
     */
    public void setRdfPropertyName(String rdfPropertyName) {
        this.rdfPropertyName = rdfPropertyName;
    }

    /**
     * @return the rdfPropertyUri
     */
    public String getRdfPropertyUri() {
        return rdfPropertyUri;
    }

    /**
     * @param rdfPropertyUri the rdfPropertyUri to set
     */
    public void setRdfPropertyUri(String rdfPropertyUri) {
        this.rdfPropertyUri = rdfPropertyUri;
    }

    /**
     * @return the rdfPropertyPrefix
     */
    public String getRdfPropertyPrefix() {
        return rdfPropertyPrefix;
    }

    /**
     * @param rdfPropertyPrefix the rdfPropertyPrefix to set
     */
    public void setRdfPropertyPrefix(String rdfPropertyPrefix) {
        this.rdfPropertyPrefix = rdfPropertyPrefix;
    }

}
