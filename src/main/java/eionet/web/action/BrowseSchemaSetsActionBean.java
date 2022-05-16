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

import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.ValidationException;
import eionet.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Action bean for browsing schema sets.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/schemasets/browse/{$event}")
public class BrowseSchemaSetsActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseSchemaSetsActionBean.class);

    /** */
    private static final String BROWSE_SCHEMA_SETS_JSP = "/pages/schemaSets/browseSchemaSets.jsp";

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Listed schema sets that are valid for viewing. */
    private List<SchemaSet> schemaSets;

    /** Listed root-level schemas that are valid for viewing. */
    private List<Schema> schemas;

    /** Ids of selected schema sets. */
    private List<Integer> selectedSchemaSets;

    /** Ids of selected schemas. */
    private List<Integer> selectedSchemas;

    /** Ids of schema sets that the current user is allowed to delete. */
    private Set<Integer> deletableSchemaSets;

    /** Ids of root-level schemas that the current user is allowed to delete. */
    private Set<Integer> deletableSchemas;

    /**
     * List of schema status texts to be displayed in the list of schemas after schema name. Released status is the normal, and show
     * the status only when it is different from the normal.
     */
    private RegStatus[] statusTextsToDisplay = {RegStatus.DRAFT, RegStatus.PUBLIC_DRAFT, RegStatus.DEPRECATED};

    /**
     * View list action.
     *
     * @return resolution
     * @throws ServiceException if operation fails
     */
    @DefaultHandler
    public Resolution viewList() throws ServiceException {
        schemaSets = schemaService.getSchemaSets(getUserName());
        schemas = schemaService.getRootLevelSchemas(getUserName());
        return new ForwardResolution(BROWSE_SCHEMA_SETS_JSP);
    }

    /**
     * View working copies action.
     *
     * @return resolution
     * @throws ServiceException if operation fails
     */
    public Resolution workingCopies() throws ServiceException {
        if (isUserLoggedIn()) {
            schemaSets = schemaService.getSchemaSetWorkingCopiesOf(getUserName());
            schemas = schemaService.getSchemaWorkingCopiesOf(getUserName());
        } else {
            addGlobalValidationError("Un-authenticated users cannot have working copies!");
        }
        return new ForwardResolution(BROWSE_SCHEMA_SETS_JSP);
    }

    /**
     * Deletes schema sets.
     *
     * @return resolution
     * @throws ServiceException if operation fails
     */
    public Resolution delete() throws ServiceException {
        Thread.currentThread().setName("DELETE-SCHEMA-SETS");
        MDC.put("sessionId", getContext().getRequest().getSession().getId().substring(0,16));
        if (!isDeletePermission()) {
            addGlobalValidationError("Cannot delete. No permission.");
            return viewList();
        }

        if (selectedSchemaSets != null && !selectedSchemaSets.isEmpty()) {
            try {
                schemaService.deleteSchemaSets(selectedSchemaSets, getUser(), true);
            } catch (ValidationException e) {
                LOGGER.info(e.getMessage());
                addGlobalValidationError(e.getMessage());
                return viewList();
            }
        }

        if (selectedSchemas != null && !selectedSchemas.isEmpty()) {
            try {
                schemaService.deleteSchemas(selectedSchemas, getUserName(), true);
            } catch (ValidationException e) {
                LOGGER.info(e.getMessage());
                addGlobalValidationError(e.getMessage());
                return viewList();
            }
        }

        addSystemMessage("Deletion successful!");
        return new RedirectResolution(BrowseSchemaSetsActionBean.class);
    }

    /**
     *
     * @return permission
     */
    public boolean isDeletePermission() {
        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUser(), "/schemasets", "d")
                        || SecurityUtil.hasPerm(getUser(), "/schemasets", "er");
            } catch (Exception e) {
                LOGGER.error("Failed to read user permission", e);
            }
        }
        return false;
    }

    /**
     * Sets the ids of selected schema sets.
     *
     * @param selectedSchemaSets
     *            list of ids in question.
     */
    public void setSelectedSchemaSets(List<Integer> selectedSchemaSets) {
        this.selectedSchemaSets = selectedSchemaSets;
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
     * Returns the set of ids of schema sets that the current user can delete.
     *
     * @return The set of ids.
     * @throws Exception if operation fails
     */
    public Set<Integer> getDeletableSchemaSets() throws Exception {
        if (deletableSchemaSets == null) {
            deletableSchemaSets = new HashSet<Integer>();
            String userName = getUserName();
            if (!StringUtils.isBlank(userName)) {
                for (SchemaSet schemaSet : schemaSets) {
                    // Must not be a working copy, nor must it be checked out
                    if (!schemaSet.isWorkingCopy() && StringUtils.isBlank(schemaSet.getWorkingUser())) {
                        String permission = schemaSet.getRegStatus().equals(RegStatus.RELEASED) ? "er" : "d";
                        if (SecurityUtil.hasPerm(getUser(), "/schemasets", permission)) {
                            deletableSchemaSets.add(schemaSet.getId());
                        }
                    }
                }
            }
        }
        return deletableSchemaSets;
    }

    /**
     * @return the deletableSchemas
     * @throws Exception if operation fails
     */
    public Set<Integer> getDeletableSchemas() throws Exception {
        if (deletableSchemas == null) {
            deletableSchemas = new HashSet<Integer>();
            String userName = getUserName();
            if (!StringUtils.isBlank(userName)) {
                for (Schema schema : schemas) {
                    // Must be a root-level schema, and must not be a working copy nor must it be checked out
                    if (schema.getSchemaSetId() <= 0 && !schema.isWorkingCopy() && StringUtils.isBlank(schema.getWorkingUser())) {
                        String permission = schema.getRegStatus().equals(RegStatus.RELEASED) ? "er" : "d";
                        if (schema.getSchemaSetId() > 0) {
                            if (SecurityUtil.hasPerm(getUser(), "/schemasets", permission)) {
                                deletableSchemas.add(schema.getId());
                            }
                        } else {
                            if (SecurityUtil.hasPerm(getUser(), "/schemas", permission)) {
                                deletableSchemas.add(schema.getId());
                            }
                        }
                    }
                }
            }
        }
        return deletableSchemas;
    }

    /**
     * @return the schemas
     */
    public List<Schema> getSchemas() {
        return schemas;
    }

    /**
     * Sets the ids of selected schemas.
     *
     * @param selectedSchemas
     *            The list of ids in question.
     */
    public void setSelectedSchemas(List<Integer> selectedSchemas) {
        this.selectedSchemas = selectedSchemas;
    }

    public RegStatus[] getStatusTextsToDisplay() {
        return statusTextsToDisplay;
    }
}
