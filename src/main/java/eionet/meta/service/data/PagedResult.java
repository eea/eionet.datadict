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

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

/**
 * Abstract paged result object, meant to be used in dsiplay:table tag.
 *
 * @author Juhan Voolaid
 */
public abstract class PagedResult<T> implements PaginatedList {

    private List<T> items;

    private int pageSize;

    private int pageNumber;

    private int totalItems;

    private String sortProperty;

    private SortOrderEnum sortOrder;

    public PagedResult(List<T> items, int totalItems, PagedRequest pagedRequest) {
        this.items = items;
        this.totalItems = totalItems;
        this.pageSize = pagedRequest.getPageSize();
        this.pageNumber = pagedRequest.getPageNumber();
        this.sortProperty = pagedRequest.getSortProperty();
        this.sortOrder = pagedRequest.getSortOrder();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getList()
     */
    @Override
    public List<T> getList() {
        return items;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getPageNumber()
     */
    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getObjectsPerPage()
     */
    @Override
    public int getObjectsPerPage() {
        return pageSize;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getFullListSize()
     */
    @Override
    public int getFullListSize() {
        return totalItems;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getSortCriterion()
     */
    @Override
    public String getSortCriterion() {
        return sortProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getSortDirection()
     */
    @Override
    public SortOrderEnum getSortDirection() {
        return sortOrder;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.displaytag.pagination.PaginatedList#getSearchId()
     */
    @Override
    public String getSearchId() {
        return null;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


}
