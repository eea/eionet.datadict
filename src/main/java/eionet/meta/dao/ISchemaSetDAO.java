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

package eionet.meta.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * SchemaSet DAO.
 *
 * @author Juhan Voolaid
 */
public interface ISchemaSetDAO {

    /**
     * Returns paged schema sets result object.
     *
     * @param pagedRequest
     * @return
     */
    SchemaSetsResult searchSchemaSets(SchemaSetFilter searchFilter);

    /**
     * Returns schema sets.
     *
     * @param releasedOnly
     * @return
     */
    List<SchemaSet> getSchemaSets(boolean releasedOnly);

    /**
     * Returns schema sets by given ids.
     *
     * @param ids
     * @return
     * @throws DAOException
     */
    List<SchemaSet> getSchemaSets(List<Integer> ids);

    /**
     * Returns schema set with given id.
     *
     * @param id
     * @return
     */
    SchemaSet getSchemaSet(int id);

    /**
     * Inserts new schema set.
     *
     * @param schemaSet
     * @return
     */
    int createSchemaSet(SchemaSet schemaSet);

    /**
     * Updates existing schema set.
     *
     * @param schemaSet
     */
    void updateSchemaSet(SchemaSet schemaSet);

    /**
     * Modifies schema set's check in data.
     *
     * @param schemaSetId
     */
    void checkIn(int schemaSetId, String username, String comment);

    /**
     * Updates schema set dynamic attributes.
     *
     * @param schemaSetId
     * @param attributes
     */
    void updateSchemaSetAttributes(int schemaSetId, Map<Integer, Set<String>> attributes);

    /**
     * Deletes schema set rows with given ids.
     *
     * @param ids
     */
    void deleteSchemaSets(List<Integer> ids);

    /**
     *
     * @param schemaSetId1
     * @param schemaSetId2
     * @return
     */
    Map<Integer, Integer> getSchemaMappings(int schemaSetId1, int schemaSetId2);

    /**
     *
     * @param replacedId
     * @param substituteId
     */
    void replaceId(int replacedId, int substituteId);

    /**
     *
     * @param checkedOutCopyId
     * @return
     */
    SchemaSet getWorkingCopyOfSchemaSet(int checkedOutCopyId);

    /**
     *
     * @param userName
     * @return
     */
    List<SchemaSet> getWorkingCopiesOf(String userName);

    /**
     *
     * @param schemaSetIdentifier
     * @return
     */
    List<String> getSchemaFileNames(String schemaSetIdentifier);

    /**
     * Returns true if a schema set by this identifier already exists. Otherwise return false.
     *
     * @param schemaSetIdentifier The schema set identifier to check.
     * @return
     */
    boolean exists(String schemaSetIdentifier);

    /**
     * Sets the given schema set's WORKING_USER to the given user name.
     * The latter may be null, in which case WORKING_USER will also be set to null.
     *
     * @param schemaSetId
     * @param userName
     */
    void setWorkingUser(int schemaSetId, String userName);

    /**
     * Copies the T_SCHEMA_SET row identified by the given schema set id.
     * The new copy's WORKING_USER and USER_MODIFIED will be set to the given user name.
     * The new copy's IDENTIFIER will be set to the given new identifier, unless the latter
     * is null, in which case the new copy's IDENTIFIER shall remain the same
     * as original's.
     *
     * If the given identifier (i.e. the new identifier) is null, the new copy's
     * CHECKEDOUT_COPY_ID is set to the original's id, otherwise it is set to null.
     * This is basically an assumption that if there will be a new identifier, it
     * is not a check-out operation, therefore CHECKEDOUT_COPY_ID is irrelevant.
     *
     * The method returns the new copy's auto-generated id.
     *
     * @param schemaSetId
     * @param userName
     * @param newIdentifier
     */
    int copySchemaSetRow(int schemaSetId, String userName, String newIdentifier);
}
