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

import org.displaytag.properties.SortOrderEnum;

/**
 * Request object for paged search.
 *
 * @author Juhan Voolaid
 */
public class PagedRequest {

    private int pageSize;

    private int pageNumber;

    private String sortProperty;

    private SortOrderEnum sortOrder;

    /**
     * Class constructor.
     *
     * @param pageSize
     * @param pageNumber
     */
    public PagedRequest(int pageNumber, int pageSize) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    /**
     * Returns the offset of the first row for the search result to return.
     */
    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return the sortProperty
     */
    public String getSortProperty() {
        return sortProperty;
    }

    /**
     * @param sortProperty
     *            the sortProperty to set
     */
    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

    /**
     * @return the sortOrder
     */
    public SortOrderEnum getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder
     *            the sortOrder to set
     */
    public void setSortOrder(SortOrderEnum sortOrder) {
        this.sortOrder = sortOrder;
    }

}
