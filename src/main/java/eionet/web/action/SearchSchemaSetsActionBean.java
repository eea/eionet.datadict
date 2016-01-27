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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.displaytag.properties.SortOrderEnum;

import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.ValidationException;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import eionet.util.SecurityUtil;

/**
 * Schema sets action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schemasets/search/{$event}")
public class SearchSchemaSetsActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SearchSchemaSetsActionBean.class);

    /** */
    private static final String SEARCH_SCHEMA_SETS_JSP = "/pages/schemaSets/searchSchemaSets.jsp";

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Schemasets search result. */
    private SchemaSetsResult schemaSetsResult;

    /** Schema search filter. */
    private SchemaSetFilter searchFilter;

    /** Selected ids. */
    @Deprecated
    private List<Integer> selected;

    /** Table page number. */
    private int page = 1;

    /** Sorting property. */
    private String sort;

    /** Sorting direction. */
    private String dir;

    /** Ids of search result schema sets that the current user is allowed to delete. */
    @Deprecated
    private Set<Integer> deletable;

    /** Possible registration statuses. */
    private List<String> regStatuses;

    /**
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution search() throws ServiceException {
        if (searchFilter == null) {
            searchFilter = new SchemaSetFilter();
            searchFilter.setAttributes(schemaService.getSchemaSetAttributes());
        }
        searchFilter.setPageNumber(page);

        if (!isAuthenticated()) {
            searchFilter.setRegStatuses(RegStatus.getPublicStatuses());
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
        schemaSetsResult = schemaService.searchSchemaSets(searchFilter);
        return new ForwardResolution(SEARCH_SCHEMA_SETS_JSP);
    }

    /**
     * Deletes schema sets.
     *
     * @return
     * @throws ServiceException
     */
    @Deprecated
    public Resolution delete() throws ServiceException {
        if (isDeletePermission()) {
            try {
                schemaService.deleteSchemaSets(selected, getUserName(), true);
            } catch (ValidationException e) {
                LOGGER.info(e.getMessage());
                addGlobalValidationError(e.getMessage());
                return search();
            }
        } else {
            addGlobalValidationError("Cannot delete. No permission.");
            return search();
        }

        if (selected.size() == 1) {
            addSystemMessage("Schema set succesfully deleted.");
        } else if (selected.size() > 1) {
            addSystemMessage("Schema sets succesfully deleted.");
        }
        return new RedirectResolution(SearchSchemaSetsActionBean.class);
    }

    @Deprecated
    public boolean isDeletePermission() {
        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUserName(), "/schemasets", "d")
                || SecurityUtil.hasPerm(getUserName(), "/schemasets", "er");
            } catch (Exception e) {
                LOGGER.error("Failed to read user permission", e);
            }
        }
        return false;
    }

    public boolean isAuthenticated() {
        return getUser() != null;
    }

    /**
     * @param schemaService
     *            the schemaService to set
     */
    public void setSchemaService(ISchemaService schemaService) {
        this.schemaService = schemaService;
    }

    public List<String> getRegStatuses() {

        if (regStatuses == null) {
            regStatuses = new ArrayList<String>();
            regStatuses.add("");
            for (RegStatus rs : RegStatus.values()) {
                regStatuses.add(rs.toString());
            }
        }

        return regStatuses;
    }

    /**
     * @return the schemaSetsResult
     */
    public SchemaSetsResult getSchemaSetsResult() {
        return schemaSetsResult;
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
     * @return the selected
     */
    public List<Integer> getSelected() {
        return selected;
    }

    /**
     * @param selected
     *            the selected to set
     */
    public void setSelected(List<Integer> selected) {
        this.selected = selected;
    }

    /**
     * @return the searchFilter
     */
    public SchemaSetFilter getSearchFilter() {
        return searchFilter;
    }

    /**
     * @param searchFilter
     *            the searchFilter to set
     */
    public void setSearchFilter(SchemaSetFilter searchFilter) {
        this.searchFilter = searchFilter;
    }

    /**
     * @return the deletable
     * @throws Exception
     */
    @Deprecated
    public Set<Integer> getDeletable() throws Exception {

        if (deletable == null) {
            deletable = new HashSet<Integer>();
            String userName = getUserName();
            if (!StringUtils.isBlank(userName)) {
                List<SchemaSet> schemaSets = schemaSetsResult.getList();
                for (SchemaSet schemaSet : schemaSets) {
                    // Must not be a working copy, nor must it be checked out
                    if (!schemaSet.isWorkingCopy() && StringUtils.isBlank(schemaSet.getWorkingUser())) {
                        String permission = schemaSet.getRegStatus().equals(RegStatus.RELEASED) ? "er" : "d";
                        if (SecurityUtil.hasPerm(userName, "/schemasets", permission)) {
                            deletable.add(schemaSet.getId());
                        }
                    }
                }
            }
        }
        return deletable;
    }

}
