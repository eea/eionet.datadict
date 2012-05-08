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

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import eionet.meta.service.data.SchemasResult;

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
    SchemaSetsResult searchSchemaSets(SchemaSetFilter searchFilter) throws ServiceException;

    /**
     * Finds schemas based on filter.
     *
     * @param searchFilter
     * @return
     * @throws ServiceException
     */
    SchemasResult searchSchemas(SchemaFilter searchFilter) throws ServiceException;

    /**
     * Returns schema sets.
     *
     * @param releasedOnly
     * @return
     * @throws ServiceException
     */
    List<SchemaSet> getSchemaSets(boolean releasedOnly) throws ServiceException;

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
     * @param attributes
     * @param username
     * @return id of new schema set
     * @throws ServiceException
     */
    int addSchemaSet(SchemaSet schemaSet, Map<Integer, Set<String>> attributes, String username) throws ServiceException;

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
     * Deletes SchemaSets with given id.
     *
     * @param ids
     * @param userName
     * @param includingContents
     * @throws ServiceException
     */
    void deleteSchemaSets(List<Integer> ids, String userName, boolean includingContents) throws ServiceException;

    /**
     * Deletes Schemas with given id..
     *
     * @param ids
     * @param userName
     * @param includingContents
     * @throws ServiceException
     */
    void deleteSchemas(List<Integer> ids, String userName, boolean includingContents) throws ServiceException;

    /**
     * Checks in schema set with given id.
     *
     * @param schemaSetId
     * @param username
     * @param comment
     * @return
     * @throws ServiceException
     */
    int checkInSchemaSet(int schemaSetId, String username, String comment) throws ServiceException;

    /**
     *
     * @param schema
     * @param attributes
     * @return
     * @throws ServiceException
     */
    int addSchema(Schema schema, Map<Integer, Set<String>> attributes) throws ServiceException;

    /**
     *
     * @param schemaSetId
     * @return
     * @throws ServiceException
     */
    List<Schema> listSchemaSetSchemas(int schemaSetId) throws ServiceException;

    /**
     *
     * @param schemaSetId
     * @param username
     * @param newIdentifier
     * @return
     * @throws ServiceException
     */
    int checkOutSchemaSet(int schemaSetId, String username, String newIdentifier) throws ServiceException;

    /**
     *
     * @param checkedOutCopyId
     * @return
     * @throws ServiceException
     */
    SchemaSet getWorkingCopyOfSchemaSet(int checkedOutCopyId) throws ServiceException;

    /**
     *
     * @param schemaSetId
     * @param username
     * @return
     * @throws ServiceException
     */
    int undoCheckOutSchemaSet(int schemaSetId, String username) throws ServiceException;

    /**
     *
     * @param userName
     * @return
     * @throws ServiceException
     */
    List<SchemaSet> getSchemaSetWorkingCopiesOf(String userName) throws ServiceException;

    /**
     * Returns all attributes that are binded to schema sets.
     *
     * @return
     * @throws ServiceException
     */
    List<Attribute> getSchemaSetAttributes() throws ServiceException;

    /**
     * Returns all attributes that are binded to schemas.
     *
     * @return
     * @throws ServiceException
     */
    List<Attribute> getSchemaAttributes() throws ServiceException;

    /**
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    Schema getSchema(int id) throws ServiceException;

    /**
     *
     * @param schema
     * @param attributes
     * @param username
     * @throws ServiceException
     */
    void updateSchema(Schema schema, Map<Integer, Set<String>> attributes, String username) throws ServiceException;

    /**
     *
     * @param userName
     * @return
     * @throws ServiceException
     */
    List<Schema> getSchemaWorkingCopiesOf(String userName) throws ServiceException;

    /**
     *
     * @param schemaId
     * @param userName
     * @param comment
     * @return
     * @throws ServiceException
     */
    int checkInSchema(int schemaId, String userName, String comment) throws ServiceException;

    /**
     * Returns true if a schema set by this identifier already exists. Otherwise return false.
     *
     * @param schemaSetIdentifier The schema set identifier to check.
     * @return
     * @throws ServiceException
     */
    boolean schemaSetExists(String schemaSetIdentifier) throws ServiceException;

    /**
     * Returns true if a schema by the given filename already exists, regardless of whether it
     * is a working copy or not. Otherwise return false.
     *
     * @param schemaFilename The filename to check.
     * @return The boolean in question.
     * @throws ServiceException
     */
    boolean schemaExists(String schemaFilename) throws ServiceException;

    /**
     *
     * @param schemaId
     * @param userName
     * @param object
     * @return
     * @throws ServiceException
     */
    int checkOutSchema(int schemaId, String userName, Object object) throws ServiceException;

    /**
     * Returns a list of all root-level schemas. If the given boolean is true,
     * only schemas in Released status are returned. Otherwise the status is ignored.
     * Note that the returned list does not contain any working copies!
     *
     * @param listReleasedOnly
     * @return
     * @throws ServiceException
     */
    List<Schema> getRootLevelSchemas(boolean listReleasedOnly) throws ServiceException;

    /**
     * Returns working copy of the schema identified by the given id.
     * This means the given id must denote a schema that has been checked out by somebody.
     *
     * @param schemaId
     * @return
     * @throws ServiceException
     */
    Schema getWorkingCopyOfSchema(int schemaId) throws ServiceException;

    /**
     *
     * @param schemaId
     * @param userName
     * @return
     * @throws ServiceException
     */
    int undoCheckOutSchema(int schemaId, String userName) throws ServiceException;
}
