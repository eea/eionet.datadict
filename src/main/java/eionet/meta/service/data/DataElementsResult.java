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

import eionet.meta.dao.domain.DataElement;

/**
 * Data elements search result.
 *
 * @author Juhan Voolaid
 */
public class DataElementsResult {

    private List<DataElement> dataElements;

    private boolean commonElements;

    /**
     * Total number of results.
     *
     * @return
     */
    public int getTotalResults() {
        return dataElements.size();
    }

    /**
     * @return the dataElements
     */
    public List<DataElement> getDataElements() {
        return dataElements;
    }

    /**
     * @param dataElements
     *            the dataElements to set
     */
    public void setDataElements(List<DataElement> dataElements) {
        this.dataElements = dataElements;
    }

    /**
     * @return the commonElements
     */
    public boolean isCommonElements() {
        return commonElements;
    }

    /**
     * @param commonElements
     *            the commonElements to set
     */
    public void setCommonElements(boolean commonElements) {
        this.commonElements = commonElements;
    }

}
