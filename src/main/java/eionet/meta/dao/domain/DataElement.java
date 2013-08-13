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

import org.apache.commons.lang.StringUtils;

import eionet.util.Util;

/**
 * Data element.
 *
 * @author Juhan Voolaid
 */
public class DataElement {

    private int id;

    private String identifier;

    private String shortName;

    private String type;

    private String status;

    private Date modified;

    private String tableName;

    private String dataSetName;

    private String workingUser;

    private boolean workingCopy;

    private String rdfTypeName;
    private String rdfTypeUri;
    private String rdfTypePrefix;
    private int rdfNamespaceId;

    /** Value from T_CONCEPT_ELEMENT_VALUE table. */
    private String attributeValue;

    public String getStatusImage() {
        return Util.getStatusImage(status);
    }

    public boolean isReleased() {
        return StringUtils.equalsIgnoreCase("Released", status);
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
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified
     *            the modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the dataSetName
     */
    public String getDataSetName() {
        return dataSetName;
    }

    /**
     * @param dataSetName
     *            the dataSetName to set
     */
    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
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
     * @return the rdfTypeName
     */
    public String getRdfTypeName() {
        return rdfTypeName;
    }

    /**
     * @param rdfTypeName
     *            the rdfTypeName to set
     */
    public void setRdfTypeName(String rdfTypeName) {
        this.rdfTypeName = rdfTypeName;
    }

    /**
     * @return the rdfTypeUri
     */
    public String getRdfTypeUri() {
        return rdfTypeUri;
    }

    /**
     * @param rdfTypeUri
     *            the rdfTypeUri to set
     */
    public void setRdfTypeUri(String rdfTypeUri) {
        this.rdfTypeUri = rdfTypeUri;
    }

    /**
     * @return the rdfTypePrefix
     */
    public String getRdfTypePrefix() {
        return rdfTypePrefix;
    }

    /**
     * @param rdfTypePrefix
     *            the rdfTypePrefix to set
     */
    public void setRdfTypePrefix(String rdfTypePrefix) {
        this.rdfTypePrefix = rdfTypePrefix;
    }

    /**
     * @return the rdfNamespaceId
     */
    public int getRdfNamespaceId() {
        return rdfNamespaceId;
    }

    /**
     * @param rdfNamespaceId
     *            the rdfNamespaceId to set
     */
    public void setRdfNamespaceId(int rdfNamespaceId) {
        this.rdfNamespaceId = rdfNamespaceId;
    }

    /**
     * @return the attributeValue
     */
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     * @param attributeValue
     *            the attributeValue to set
     */
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    /**
     * @return identifier of the data element
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *
     * @param identifier attribute value to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
