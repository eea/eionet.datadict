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

import eionet.meta.DDUser;
import net.sourceforge.stripes.action.FileBean;
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
     * @param userName
     *
     * @return
     * @throws ServiceException
     */
    List<SchemaSet> getSchemaSets(String userName) throws ServiceException;

    /**
     * Return schema set.
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    SchemaSet getSchemaSet(int id) throws ServiceException;

    /**
     * Return schema set.
     *
     * @param identifier
     * @param workingCopy
     * @return
     * @throws ServiceException
     */
    SchemaSet getSchemaSet(String identifier, boolean workingCopy) throws ServiceException;

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
     * @param user
     * @param includingContents
     * @throws ServiceException
     */
    void deleteSchemaSets(List<Integer> ids, DDUser user, boolean includingContents) throws ServiceException;

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
     * @param user
     * @param comment
     * @return
     * @throws ServiceException
     */
    int checkInSchemaSet(int schemaSetId, DDUser user, String comment) throws ServiceException;

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
     * @param userName
     * @return
     * @throws ServiceException
     */
    int checkOutSchemaSet(int schemaSetId, String userName) throws ServiceException;

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
     * @param user
     * @return
     * @throws ServiceException
     */
    int undoCheckOutSchemaSet(int schemaSetId, DDUser user) throws ServiceException;

    /**
     *
     * @param userName
     * @return
     * @throws ServiceException
     */
    List<SchemaSet> getSchemaSetWorkingCopiesOf(String userName) throws ServiceException;

    /**
     * Returns all attributes that are bound to schema sets.
     *
     * @return
     * @throws ServiceException
     */
    List<Attribute> getSchemaSetAttributes() throws ServiceException;

    /**
     * Returns all attributes that are bound to schemas.
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
     * Returns schema.
     *
     * @param schemaSetIdentifier
     * @param schemaFileName
     * @param workingCopy
     * @return
     * @throws ServiceException
     */
    Schema getSchema(String schemaSetIdentifier, String schemaFileName, boolean workingCopy) throws ServiceException;

    /**
     * Returns root level schema.
     *
     * @param schemaFileName
     * @param workingCopy
     * @return
     * @throws ServiceException
     */
    Schema getRootLevelSchema(String schemaFileName, boolean workingCopy) throws ServiceException;

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
     * @param schemaSetIdentifier
     *            The schema set identifier to check.
     * @return
     * @throws ServiceException
     */
    boolean schemaSetExists(String schemaSetIdentifier) throws ServiceException;

    /**
     *
     * @param schemaId
     * @param userName
     * @return
     * @throws ServiceException
     */
    int checkOutSchema(int schemaId, String userName) throws ServiceException;

    /**
     * Returns a list of all root-level schemas. If the given user name is blank, the returned list contains only schemas that are
     * not working copies and have their registration status set to Released. Otherwise the returned list contains schemas that are
     * either not working copies or are precisely the working copies of the given user.
     *
     * @param userName
     *
     * @return
     * @throws ServiceException
     */
    List<Schema> getRootLevelSchemas(String userName) throws ServiceException;

    /**
     * Returns working copy of the schema identified by the given id. This means the given id must denote a schema that has been
     * checked out by somebody.
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

    /**
     * Creates a full copy of this schema set, including its schemas and the contents of the schemas. The copy will automatically be
     * set to working copy and the working user will be set to the given user name. The new copy's identifier will be set to the
     * given identifier. The latter must not be null or blank, and if it's set to an already existing schema set identifier, the
     * method will throw a {@link ServiceException}.
     *
     * The method returns the new copy's auto-generated id.
     *
     * @param schemaSetId
     * @param userName
     * @param identifier
     * @return
     * @throws ServiceException
     */
    int copySchemaSet(int schemaSetId, String userName, String identifier) throws ServiceException;

    /**
     *
     * @param schemaId
     * @param userName
     * @param newFile
     * @return
     * @throws ServiceException
     */
    int copySchema(int schemaId, String userName, FileBean newFile) throws ServiceException;

    /**
     * Copies the schema with given schemaId to the schema set.
     *
     * @param schemaId
     * @param schemaSetId
     * @param userName
     * @param newFileName
     * @throws ServiceException
     */
    void copySchema(int schemaId, int schemaSetId, String userName, String newFileName) throws ServiceException;

    /**
     * Returns the list of schema sets matching the given continuity id, excluding the ones that are working copies and the ones
     * that match the given integer inputs. The method may choose to hide certain versions based on the permissions of the given
     * user.
     *
     * @param userName
     * @param continuityId
     * @param excludeIds
     *
     * @return
     * @throws ServiceException
     */
    List<SchemaSet> getSchemaSetVersions(String userName, String continuityId, int... excludeIds) throws ServiceException;

    /**
     * Returns the list of root-level schemas matching the given continuity id, excluding the ones that are working copies and the
     * ones that match the given integer inputs. The method may choose to hide certain versions based on the permissions of the
     * given user.
     *
     * @param userName
     * @param continuityId
     * @param excludeIds
     *
     * @return
     * @throws ServiceException
     */
    List<Schema> getSchemaVersions(String userName, String continuityId, int... excludeIds) throws ServiceException;

    /**
     * Returns true if the given schema exists within the given schema set. Otherwise returns false. The schema is given by its file
     * name, while the schema set is given by its auto-generated id. If the latter <=0, the schema is considered to be a root-level
     * schema.
     *
     * @param fileName
     * @param schemaSetId
     * @return
     * @throws ServiceException
     */
    boolean schemaExists(String fileName, int schemaSetId) throws ServiceException;

    List<Schema> getSchemasForObligation(String obligationId, boolean releasedOnly) throws ServiceException;
}
