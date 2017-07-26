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
 * Agency. Portions created by TripleDev or Zero TechnoLOGGERies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.action;


import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.dao.domain.DataSetTableSort;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import eionet.meta.service.ITableService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.TableFilter;
import eionet.meta.service.data.TableResult;
import java.util.Collections;
import java.util.List;
import net.sourceforge.stripes.action.ForwardResolution;
import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table search action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/searchtables")
public class TableSearchActionBean extends AbstractActionBean {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableSearchActionBean.class);

    /** Table search result. */
    private TableResult tableResult;

    /** Table search filter. */
    private TableFilter tableFilter;

    /** Table service. */
    @SpringBean
    private ITableService tableService;

    /**
     * Handles the searching action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution search() throws ServiceException {

        if (tableFilter == null) {
            initTableFilter();
        }
        tableFilter.setPageNumber(page);

        List<DataSetTable> dataSetTables = tableService.searchTables(tableFilter);
        int dataSetTablesSize = dataSetTables.size();

        // sorting
        DataSetTableSort dataSetTableSort = DataSetTableSort.fromString(sort);
        if (dataSetTableSort != null) {
            boolean descending = StringUtils.isNotBlank(dir) && dir.equals("desc");
            // feed sort info to display tag
            tableFilter.setSortProperty(sort);
            tableFilter.setSortOrder(descending ? SortOrderEnum.DESCENDING : SortOrderEnum.ASCENDING);
            Collections.sort(dataSetTables, dataSetTableSort.getComparator(descending));
        }

        tableResult = new TableResult(dataSetTables, dataSetTablesSize, tableFilter);

        // pagination
        if (tableFilter.isUsePaging()) {
            if (tableFilter.getOffset() > dataSetTablesSize) {
                tableResult.setList(Collections.EMPTY_LIST);
            } else {
                int paginationLimit = tableFilter.getOffset()+ tableFilter.getPageSize();
                List<DataSetTable> paginatedItems = tableResult.getList().subList(tableFilter.getOffset(),
                        dataSetTablesSize <= paginationLimit ? dataSetTablesSize : paginationLimit);
                tableResult.setList(paginatedItems);
            }
        }
        return new ForwardResolution("/pages/search_tables.jsp");
    }

    /**
     * Initializes filter.
     *
     * @throws ServiceException
     */
    public void initTableFilter() throws ServiceException {
        tableFilter = new TableFilter();
        tableFilter.setAttributes(tableService.getTableAttributes());
    }

    /**
     * @return the dataSetTables
     */
    public TableResult getTableResult() {
        return tableResult;
    }

    /**
     * @param dataSetTables
     *            the dataSetTables to set
     */
    public void setDataSetTables(TableResult tableResult) {
        this.tableResult = tableResult;
    }

    /**
     * @return the tableFilter
     */
    public TableFilter getTableFilter() {
        return tableFilter;
    }

    /**
     * @param tableFilter
     *            the tableFilter to set
     */
    public void setTableFilter(TableFilter tableFilter) {
        this.tableFilter = tableFilter;
    }

    /**
     * @return the tableService
     */
    public ITableService getTableService() {
        return tableService;
    }

    /**
     * @param tableService
     *            the tableService to set
     */
    public void setTableService(ITableService tableService) {
        this.tableService = tableService;
    }

    /** Table page number. */
    private int page = 1;

    /** Sorting property. */
    private String sort;

    /** Sorting direction. */
    private String dir;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

}