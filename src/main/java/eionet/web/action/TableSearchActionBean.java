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

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.log4j.Logger;

import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.service.ITableService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.TableFilter;

/**
 * Table search action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/tableSearch.action")
public class TableSearchActionBean extends AbstractActionBean {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(TableSearchActionBean.class);

    /** Table search result. */
    private List<DataSetTable> dataSetTables;

    /** Table search filter. */
    private TableFilter tableFilter;

    /** Table service. */
    @SpringBean
    private ITableService tableService;

    /**
     * Handles the form page view.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution form() throws ServiceException {
        initTableFilter();
        return new ForwardResolution("/pages/tableSearch.jsp");
    }

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
        dataSetTables = tableService.searchTables(tableFilter);
        return new ForwardResolution("/pages/tableResult.jsp");
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
    public List<DataSetTable> getDataSetTables() {
        return dataSetTables;
    }

    /**
     * @param dataSetTables
     *            the dataSetTables to set
     */
    public void setDataSetTables(List<DataSetTable> dataSetTables) {
        this.dataSetTables = dataSetTables;
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

}
