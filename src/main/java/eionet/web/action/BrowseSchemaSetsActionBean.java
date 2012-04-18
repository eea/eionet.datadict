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

import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.ValidationException;
import eionet.util.SecurityUtil;

/**
 * Action bean for browsing schema sets.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schemaSets.action")
public class BrowseSchemaSetsActionBean extends AbstractActionBean {

    private static final String BROWSE_SCHEMA_SETS_JSP = "/pages/schemaSets/browseSchemaSets.jsp";

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** All schema sets for viewing. */
    private List<SchemaSet> schemaSets;

    /** Selected ids. */
    private List<Integer> selected;

    /** Ids of schema sets that the current user is allowed to delete. */
    private Set<Integer> deletable;

    /** If true, only working copies must be listed. If false, only non-working copies must be listed.*/
    private boolean workingCopy;

    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        boolean listReleasedOnly = getUser() == null;
        schemaSets = schemaService.getSchemaSets(listReleasedOnly, workingCopy);
        return new ForwardResolution(BROWSE_SCHEMA_SETS_JSP);
    }

    /**
     * Deletes schema sets.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution delete() throws ServiceException {
        if (isDeletePermission()) {
            try {
                schemaService.deleteSchemaSets(selected, getUserName());
            } catch (ValidationException e) {
                LOGGER.info(e.getMessage());
                addGlobalValidationError(e.getMessage());
                return viewList();
            }
        } else {
            addGlobalValidationError("Cannot delete. No permission.");
            return viewList();
        }

        return new RedirectResolution(BrowseSchemaSetsActionBean.class);
    }

    /**
     * 
     * @return
     */
    public boolean isDeletePermission() {
        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUserName(), "/schemasets", "d") || SecurityUtil.hasPerm(getUserName(), "/schemasets", "er");
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

    /**
     * @return the deletable
     * @throws Exception
     */
    public Set<Integer> getDeletable() throws Exception {

        if (deletable==null){
            deletable = new HashSet<Integer>();
            String userName = getUserName();
            if (!StringUtils.isBlank(userName)){
                for (SchemaSet schemaSet : schemaSets) {
                    // Must not be a working copy, nor must it be checked out
                    if (!schemaSet.isWorkingCopy() && StringUtils.isBlank(schemaSet.getWorkingUser())){
                        String permission = schemaSet.getRegStatus().equals(SchemaSet.RegStatus.RELEASED) ? "er" : "d";
                        if (SecurityUtil.hasPerm(userName, "/schemasets", permission)){
                            deletable.add(schemaSet.getId());
                        }
                    }
                }
            }
        }
        return deletable;
    }

    /**
     * @param workingCopy the workingCopy to set
     */
    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

}
