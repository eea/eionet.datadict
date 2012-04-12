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
import eionet.meta.service.data.PagedRequest;
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
     * @throws DAOException
     */
    SchemaSetsResult getSchemaSets(PagedRequest pagedRequest);

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
     * @param schemaSet
     */
    void checkInSchemaSet(SchemaSet schemaSet, String username, String comment);

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
     * Deletes the attributes of the schema sets with given ids.
     *
     * @param ids
     */
    void deleteAttributes(List<Integer> ids);
}
