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

import java.util.ArrayList;
import java.util.List;

import eionet.meta.dao.domain.Attribute;

/**
 * Data elements search filter.
 *
 * @author Juhan Voolaid
 */
public class DataElementsFilter {

    public static final String NON_COMMON_ELEMENT_TYPE = "nonCommon";

    public static final String COMMON_ELEMENT_TYPE = "common";

    private String regStatus;

    private String dataSet;

    private String type;

    private String shortName;

    private String identifier;

    private String keyword;

    private String elementType;

    private boolean includeHistoricVersions;

    /** For searching user's working copy common elements. */
    private String userName;

    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * Returns NON_COMMON_ELEMENT_TYPE constant value.
     *
     * @return
     */
    public String getNonCommonElementType() {
        return NON_COMMON_ELEMENT_TYPE;
    }

    /**
     * Returns COMMON_ELEMENT_TYPE constant value.
     *
     * @return
     */
    public String getCommonElementType() {
        return COMMON_ELEMENT_TYPE;
    }

    /**
     * @return the regStatus
     */
    public String getRegStatus() {
        return regStatus;
    }

    /**
     * @param regStatus
     *            the regStatus to set
     */
    public void setRegStatus(String regStatus) {
        this.regStatus = regStatus;
    }

    /**
     * @return the dataSet
     */
    public String getDataSet() {
        return dataSet;
    }

    /**
     * @param dataSet
     *            the dataSet to set
     */
    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName
     *            the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
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
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword
     *            the keyword to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return the attributes
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the elementType
     */
    public String getElementType() {
        return elementType;
    }

    /**
     * @param elementType
     *            the elementType to set
     */
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    /**
     * @return the includeHistoricVersions
     */
    public boolean isIncludeHistoricVersions() {
        return includeHistoricVersions;
    }

    /**
     * @param includeHistoricVersions
     *            the includeHistoricVersions to set
     */
    public void setIncludeHistoricVersions(boolean includeHistoricVersions) {
        this.includeHistoricVersions = includeHistoricVersions;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}
