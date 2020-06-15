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

package eionet.web.action;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.VersionManager;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataElementSort;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.util.SecurityUtil;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;

/**
 * Data elements search page controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/searchelements")
public class SearchDataElementsActionBean extends AbstractActionBean {

    /** Possible registration statuses. */
    @SuppressWarnings("unchecked")
    private List<String> regStatuses = new ArrayList<String>(VersionManager.REGISTRATION_STATUSES);

    /** Select option values. */
    private List<DataSet> dataSets;

    /** Search filter. */
    private DataElementsFilter filter;

    /** Search result object. */
    private DataElementsResult result;

    /** Filtering attributes that are available to add. */
    private List<Attribute> addableAttributes;

    /** Id of the attribute to add. */
    private int addAttr;

    /** Id of the attribute to delete. */
    private int delAttr;

    /** Data service. */
    @SpringBean
    private IDataService dataService;

    /**
     * Default form view action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution search() throws ServiceException {
        if (filter == null) {
            filter = new DataElementsFilter();
            filter.setElementType(DataElementsFilter.NON_COMMON_ELEMENT_TYPE);
            filter.getDefaultAttributes().add(dataService.getAttributeByName("Name"));
            filter.getDefaultAttributes().add(dataService.getAttributeByName("Definition"));
            filter.getDefaultAttributes().add(dataService.getAttributeByName("Keyword"));
        }
        filter.setPageNumber(page);
        dataSets = dataService.getDataSets();

        addableAttributes = dataService.getDataElementAttributes();
        if (addAttr != 0) {
            addSelectedAttribute();
        }

        if (delAttr != 0) {
            deleteSelectedAttribute();
        }
        filterAddableAttributes();

        List<DataElement> dataElements = dataService.searchDataElements(filter);
        int dataElementsSize = dataElements.size();

        // sorting
        DataElementSort dataElementSort = DataElementSort.fromString(sort);
        if (dataElementSort != null) {
            boolean descending = StringUtils.isNotBlank(dir) && dir.equals("desc");
            // feed sort order to display tag
            filter.setSortProperty(sort);
            filter.setSortOrder(descending ? SortOrderEnum.DESCENDING : SortOrderEnum.ASCENDING);
            Collections.sort(dataElements, dataElementSort.getComparator(descending));
        }

        result = new DataElementsResult(dataElements, dataElementsSize, filter);

        // pagination
        if (filter.isUsePaging()) {
            if (filter.getOffset() > dataElementsSize) {
                result.setList(Collections.EMPTY_LIST);
            } else {
                int paginationLimit = filter.getOffset()+ filter.getPageSize();
                List<DataElement> paginatedItems = result.getList().subList(filter.getOffset(), 
                        dataElementsSize <= paginationLimit ? dataElementsSize : paginationLimit);
                result.setList(paginatedItems);
            }
        }

        return new ForwardResolution("/pages/dataElementSearch.jsp");
    }

    /**
     * Deletes the attribute from addedAttributes collection.
     */
    private void deleteSelectedAttribute() {
        for (Attribute a : filter.getAddedAttributes()) {
            if (delAttr == a.getId()) {
                filter.getAddedAttributes().remove(a);
                break;
            }
        }
        delAttr = 0;
    }

    /**
     * Adds the attribute to addedAttributes collection.
     */
    private void addSelectedAttribute() {
        for (Attribute a : addableAttributes) {
            if (addAttr == a.getId()) {
                filter.getAddedAttributes().add(a);
                break;
            }
        }
        addAttr = 0;
    }

    /**
     * Filters out attributes that are already included in the form.
     *
     * @throws ServiceException
     */
    private void filterAddableAttributes() throws ServiceException {
        // Remove already included attributes
        for (Attribute a : filter.getAttributes()) {
            if (addableAttributes.contains(a)) {
                addableAttributes.remove(a);
            }
        }
    }

    /**
     * Returns true, when user has permission to add common element.
     *
     * @return
     */
    public boolean isPermissionToAdd() {
        try {
            return getUser() != null && SecurityUtil.hasPerm(getUser(), "/elements", "i");
        } catch (Exception e) {
            LOGGER.error("Failed to get user permission: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * @return the filter
     */
    public DataElementsFilter getFilter() {
        return filter;
    }

    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter(DataElementsFilter filter) {
        this.filter = filter;
    }

    /**
     * @return the regStatuses
     */
    public List<String> getRegStatuses() {
        return regStatuses;
    }

    /**
     * @return the dataSets
     */
    public List<DataSet> getDataSets() {
        return dataSets;
    }

    /**
     * @return the result
     */
    public DataElementsResult getResult() {
        return result;
    }

    /**
     * @return the addAttr
     */
    public int getAddAttr() {
        return addAttr;
    }

    /**
     * @param addAttr
     *            the addAttr to set
     */
    public void setAddAttr(int addAttr) {
        this.addAttr = addAttr;
    }

    /**
     * @return the addableAttributes
     */
    public List<Attribute> getAddableAttributes() {
        return addableAttributes;
    }

    /**
     * @return the delAttr
     */
    public int getDelAttr() {
        return delAttr;
    }

    /**
     * @param delAttr
     *            the delAttr to set
     */
    public void setDelAttr(int delAttr) {
        this.delAttr = delAttr;
    }

    // table page number
    private int page = 1;

    // sorting property
    private String sort;

    // sorting direction
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
