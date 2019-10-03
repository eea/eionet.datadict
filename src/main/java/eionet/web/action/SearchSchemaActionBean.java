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
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;

import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.Schema;
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

    /** If valued, marks the schema set id to copy the selected schema to. */
    private int schemaSetId;

    /** Selected schema id to copy. */
    private int schemaId;

    /** New schema name, which is asked when during copying there is name conflict. */
    private String newSchemaName;

    /** True, when during copying, new schema name must be asked from user. */
    private boolean askNewName;

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
            searchFilter = new SchemaFilter();
            searchFilter.setAttributes(schemaService.getSchemaAttributes());
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
        schemasResult = schemaService.searchSchemas(searchFilter);

        return new ForwardResolution(SEARCH_SCHEMAS_JSP);
    }

    /**
     * Action that copies the selected schema to given schema set.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution copyToSchemaSet() throws ServiceException {
        // Validate selected schema
        if (schemaId == 0) {
            addWarningMessage("Select schema to copy.");
            return search();
        }

        SchemaSet schemaSet = schemaService.getSchemaSet(schemaSetId);
        Schema schema = schemaService.getSchema(schemaId);
        if (StringUtils.isNotEmpty(newSchemaName)) {
            schema.setFileName(newSchemaName);
        }

        // Validate naming conflict
        if (schemaService.schemaExists(schema.getFileName(), schemaSetId)) {
            askNewName = true;
            newSchemaName = schema.getFileName();
            return search();
        }

        schemaService.copySchema(schema.getId(), schemaSet.getId(), getUserName(), schema.getFileName());
        addSystemMessage("Schema successfully copied");

        return new RedirectResolution(SchemaSetActionBean.class).addParameter("schemaSet.identifier", schemaSet.getIdentifier())
        .addParameter("workingCopy", true);
    }

    /**
     * Action when schema copy is cancelled.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution cancelCopy() throws ServiceException {
        SchemaSet schemaSet = schemaService.getSchemaSet(schemaSetId);
        return new RedirectResolution(SchemaSetActionBean.class).addParameter("schemaSet.identifier", schemaSet.getIdentifier())
        .addParameter("workingCopy", true);
    }

    /**
     * Returns reg. statuses to search by.
     *
     * @return
     */
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
     * True, if user is authenticated.
     *
     * @return
     */
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

    /**
     * @return the schemaSetId
     */
    public int getSchemaSetId() {
        return schemaSetId;
    }

    /**
     * @param schemaSetId
     *            the schemaSetId to set
     */
    public void setSchemaSetId(int schemaSetId) {
        this.schemaSetId = schemaSetId;
    }

    /**
     * @return the schemaId
     */
    public int getSchemaId() {
        return schemaId;
    }

    /**
     * @param schemaId
     *            the schemaId to set
     */
    public void setSchemaId(int schemaId) {
        this.schemaId = schemaId;
    }

    /**
     * @return the newSchemaName
     */
    public String getNewSchemaName() {
        return newSchemaName;
    }

    /**
     * @param newSchemaName
     *            the newSchemaName to set
     */
    public void setNewSchemaName(String newSchemaName) {
        this.newSchemaName = newSchemaName;
    }

    /**
     * @return the askNewName
     */
    public boolean isAskNewName() {
        return askNewName;
    }

}
