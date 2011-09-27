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

package eionet.dao.domain;

/**
 * DataSetTable entity.
 *
 * @author Juhan Voolaid
 */
public class DataSetTable {

    private String id;

    private String dataSetId;

    private String shortName;

    private String name;

    private String dataSetName;

    private String dataSetStatus;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the dataSetId
     */
    public String getDataSetId() {
        return dataSetId;
    }

    /**
     * @param dataSetId
     *            the dataSetId to set
     */
    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the dataSetStatus
     */
    public String getDataSetStatus() {
        return dataSetStatus;
    }

    /**
     * @param dataSetStatus
     *            the dataSetStatus to set
     */
    public void setDataSetStatus(String dataSetStatus) {
        this.dataSetStatus = dataSetStatus;
    }

}
