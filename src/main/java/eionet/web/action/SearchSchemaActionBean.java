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

import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;

import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemasResult;

/**
 * Schema search action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schema/search/{$event}")
public class SearchSchemaActionBean extends AbstractActionBean {

    private static final String SEARCH_SCHEMAS_JSP = "/pages/schemaSets/searchSchemas.jsp";

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Schema search filter. */
    private SchemaFilter searchFilter;

    /** Schema search result. */
    private SchemasResult schemasResult;

    /** Table page number. */
    private int page = 1;

    /** Sorting property. */
    private String sort;

    /** Sorting direction. */
    private String dir;

    @DefaultHandler
    public Resolution search() throws ServiceException {
        LOGGER.debug("Search schemas");
        if (searchFilter == null) {
            searchFilter = new SchemaFilter();
            searchFilter.setAttributes(schemaService.getSchemaAttributes());
        }
        searchFilter.setPageNumber(page);

        if (!isAuthenticated()) {
            searchFilter.setRegStatus(SchemaSet.RegStatus.RELEASED.toString());
        }

        if (StringUtils.isNotEmpty(sort)) {
            searchFilter.setSortProperty(sort);
            if (dir.equals("asc")) {
                searchFilter.setSortOrder(SortOrderEnum.ASCENDING);
            } else {
                searchFilter.setSortOrder(SortOrderEnum.DESCENDING);
            }
        }

        searchFilter.setSearchingUser(getUserName());
        schemasResult = schemaService.searchSchemas(searchFilter);
        return new ForwardResolution(SEARCH_SCHEMAS_JSP);
    }

    public List<String> getRegStatuses() {
        List<String> result = new ArrayList<String>();
        result.add("");
        for (SchemaSet.RegStatus rs : SchemaSet.RegStatus.values()) {
            result.add(rs.toString());
        }

        return result;
    }

    public boolean isAuthenticated() {
        return getUser() != null;
    }

    /**
     * @return the searchFilter
     */
    public SchemaFilter getSearchFilter() {
        return searchFilter;
    }

    /**
     * @param searchFilter
     *            the searchFilter to set
     */
    public void setSearchFilter(SchemaFilter searchFilter) {
        this.searchFilter = searchFilter;
    }

    /**
     * @return the schemasResult
     */
    public SchemasResult getSchemasResult() {
        return schemasResult;
    }

    /**
     * @param schemasResult
     *            the schemasResult to set
     */
    public void setSchemasResult(SchemasResult schemasResult) {
        this.schemasResult = schemasResult;
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     *            the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the sort
     */
    public String getSort() {
        return sort;
    }

    /**
     * @param sort
     *            the sort to set
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return the dir
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dir
     *            the dir to set
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * @param schemaService
     *            the schemaService to set
     */
    public void setSchemaService(ISchemaService schemaService) {
        this.schemaService = schemaService;
    }

}
