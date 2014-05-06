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
 * The Original Code is Data Dictionary.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.web.action;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.service.INamespaceService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.NamespaceFilter;
import eionet.meta.service.data.NamespaceResult;
import eionet.meta.service.data.PagedRequest;
import eionet.meta.service.data.RdfNamespaceResult;

/**
 * Action bean for listing namespaces.
 *
 * @author enver
 */
@UrlBinding("/namespaces")
public class NamespacesActionBean extends AbstractActionBean {
    /**
     * Page path.
     */
    private static final String LIST_NAMESPACES_JSP = "/pages/namespaces/listNamespaces.jsp";
    /**
     * Namespace page size.
     */
    private static final int NAMESPACE_PAGE_SIZE = 40;
    /**
     * Vocabulary service.
     */
    @SpringBean
    private INamespaceService namespaceService;
    /**
     * RDF Namespaces display list.
     */
    private RdfNamespaceResult rdfNamespaceResult;
    /**
     * Namespaces display list.
     */
    private NamespaceResult namespaceResult;
    /**
     * Namespace current page.
     */
    private int page = 1;

    /**
     * View namespaces action.
     *
     * @return Default Resolution.
     * @throws eionet.meta.service.ServiceException
     *             if anything goes wrong.
     */
    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        List<RdfNamespace> rdfNamespaceList = this.namespaceService.getRdfNamespaces();
        if (rdfNamespaceList != null) {
            PagedRequest rdfPagedRequest = new PagedRequest();
            rdfPagedRequest.setUsePaging(false);
            this.rdfNamespaceResult = new RdfNamespaceResult(rdfNamespaceList, rdfNamespaceList.size(), rdfPagedRequest);
        }

        NamespaceFilter filter = new NamespaceFilter();
        filter.setPageNumber(this.page);
        filter.setPageSize(NAMESPACE_PAGE_SIZE);
        this.namespaceResult = this.namespaceService.getNamespaces(filter);
        return new ForwardResolution(NamespacesActionBean.LIST_NAMESPACES_JSP);
    } // end of method viewList

    public NamespaceResult getNamespaceResult() {
        return namespaceResult;
    }

    public RdfNamespaceResult getRdfNamespaceResult() {
        return rdfNamespaceResult;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
} // end of class NamespacesActionBean
