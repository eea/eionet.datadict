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
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.util.SecurityUtil;

/**
 * Action bean for browsing schema sets.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schemaSets.action")
public class BrowseSchemaSetsActionBean extends AbstractActionBean {

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** All schema sets for viewing. */
    private List<SchemaSet> schemaSets;

    /** Selected ids. */
    private List<Integer> selected;

    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        boolean limited = getUser() == null;
        schemaSets = schemaService.getSchemaSets(limited);
        return new ForwardResolution("/pages/schemaSets/browseSchemaSets.jsp");
    }

    /**
     * Deletes schema sets.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution delete() throws ServiceException {
        if (isDeletePermission()) {
            schemaService.deleteSchemaSets(selected);
        } else {
            addSystemMessage("Cannot delete. No permission.");
        }
        return new RedirectResolution(BrowseSchemaSetsActionBean.class);
    }

    public boolean isDeletePermission() {
        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUserName(), "/schemasets", "u");
            } catch (Exception e) {
                LOGGER.error("Failed to read user permission", e);
            }
        }
        return false;
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
     * @return the schemaSets
     */
    public List<SchemaSet> getSchemaSets() {
        return schemaSets;
    }

    /**
     * @param schemaService
     *            the schemaService to set
     */
    public void setSchemaService(ISchemaService schemaService) {
        this.schemaService = schemaService;
    }

}
