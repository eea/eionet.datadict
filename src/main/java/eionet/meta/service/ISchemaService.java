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

package eionet.meta.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.data.PagedRequest;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * Schema service.
 *
 * @author Juhan Voolaid
 */
public interface ISchemaService {

    /**
     * Returns schema sets.
     *
     * @return
     * @throws ServiceException
     */
    SchemaSetsResult getSchemaSets(PagedRequest pagedRequest) throws ServiceException;

    /**
     * Return schema set.
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    SchemaSet getSchemaSet(int id) throws ServiceException;

    /**
     * Creates new schema set.
     *
     * @param schemaSet
     * @param username
     * @return id of new schema set
     * @throws ServiceException
     */
    int addSchemaSet(SchemaSet schemaSet, String username) throws ServiceException;

    /**
     * Updates existing schema set.
     *
     * @param schemaSet
     * @param attributes
     * @param username
     * @throws ServiceException
     */
    void updateSchemaSet(SchemaSet schemaSet, Map<Integer, Set<String>> attributes, String username) throws ServiceException;

    /**
     * Deletes SchemaSets that have property "selected=true".
     *
     * @param schemaSets
     * @throws ServiceException
     */
    void deleteSchemaSets(List<Integer> ids) throws ServiceException;

    /**
     * Checks in schema set with given id.
     *
     * @param schemaSetId
     * @param username
     * @param comment
     * @throws ServiceException
     */
    void checkIn(int schemaSetId, String username, String comment) throws ServiceException;
}
