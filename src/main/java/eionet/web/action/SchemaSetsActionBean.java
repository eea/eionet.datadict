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

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;

import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.PagedRequest;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * Schema sets action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schemaSets.action")
public class SchemaSetsActionBean extends AbstractActionBean {

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Schemasets result. */
    private SchemaSetsResult schemaSetsResult;

    /** Selected ids. */
    private List<Integer> selected;

    /** Table page number. */
    private int page = 1;

    /** Sorting property. */
    private String sort;

    /** Sorting direction. */
    private String dir;

    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        PagedRequest pagedRequest = new PagedRequest(page, PagedRequest.DEFAULT_PAGE_SIZE);
        pagedRequest.setSortProperty(sort);
        if (StringUtils.isNotEmpty(sort)) {
            if (dir.equals("asc")) {
                pagedRequest.setSortOrder(SortOrderEnum.ASCENDING);
            } else {
                pagedRequest.setSortOrder(SortOrderEnum.DESCENDING);
            }
        }

        schemaSetsResult = schemaService.getSchemaSets(pagedRequest);
        return new ForwardResolution("/pages/schemaSets/viewSchemaSets.jsp");
    }

    /**
     * Deletes schema sets.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution delete() throws ServiceException {
        schemaService.deleteSchemaSets(selected);
        return new RedirectResolution(SchemaSetsActionBean.class);
    }

    /**
     * @param schemaService
     *            the schemaService to set
     */
    public void setSchemaService(ISchemaService schemaService) {
        this.schemaService = schemaService;
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

}
