package eionet.meta.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemasResult;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public interface ISchemaDAO {

    /**
     *
     * @param schema
     * @return
     */
    int createSchema(Schema schema);

    /**
     *
     * @param schemaSetId
     * @return
     */
    List<Schema> listForSchemaSet(int schemaSetId);

    /**
     * Returns schemas by their ids.
     *
     * @param ids
     * @return
     */
    List<Schema> getSchemas(List<Integer> ids);

    /**
     *
     * @param schemaId
     * @return
     */
    Schema getSchema(int schemaId);

    /**
     * Returns schema ids of the given schema sets.
     *
     * @param schemaSetIds
     * @return
     */
    List<Integer> getSchemaIds(List<Integer> schemaSetIds);

    /**
     *
     * @param schemaId
     * @param schemaSetId
     * @param fileName
     * @param userName
     * @return
     */
    int copyToSchemaSet(int schemaId, int schemaSetId, String fileName, String userName);

    /**
     *
     * @param replacedId
     * @param substituteId
     */
    void replaceId(int replacedId, int substituteId);

    /**
     * Deletes schema rows with given ids.
     *
     * @param ids
     */
    void deleteSchemas(List<Integer> ids);

    /**
     * Finds schemas.
     *
     * @param searchFilter
     * @return
     */
    SchemasResult searchSchemas(SchemaFilter searchFilter);

    /**
     *
     * @param schema
     */
    void updateSchema(Schema schema);

    /**
     *
     * @param schemaId
     * @param attributes
     */
    void updateSchemaAttributes(int schemaId, Map<Integer, Set<String>> attributes);

    /**
     *
     * @param userName
     * @return
     */
    List<Schema> getWorkingCopiesOf(String userName);

    /**
     *
     * @param checkedOutCopyId
     */
    void unlock(int checkedOutCopyId);

    /**
     *
     * @param schemaId
     * @param username
     * @param comment
     */
    void checkIn(int schemaId, String username, String comment);

    /**
     * Returns true if a root-level schema by the given filename already exists, regardless of whether it is a working copy or not.
     * Otherwise return false.
     *
     * @param filename
     *            The filename to check.
     * @return The boolean in question.
     */
    boolean existsRootLevelSchema(String filename);

    /**
     * Returns a list of all root-level schemas. If the given user name is blank, the returned list contains only schemas that are
     * not working copies and have their registration status set to Released. Otherwise the returned list contains schemas that are
     * either not working copies or are precisely the working copies of the given user.
     *
     * @param userName
     *
     * @return
     */
    List<Schema> getRootLevelSchemas(String userName);

    /**
     * Returns working copy of the schema identified by the given id. This means the given id must denote a schema that has been
     * checked out by somebody.
     *
     * @param schemaId
     * @return
     */
    Schema getWorkingCopyOfSchema(int schemaId);

    /**
     * Sets the given root-level schema's WORKING_USER to the given user name. The latter may be null, in which case WORKING_USER
     * will also be set to null.
     *
     * @param schemaId
     * @param userName
     */
    void setWorkingUser(int schemaId, String userName);

    /**
     * Copies the T_SCHEMA row identified by the given schema id. This must be a root-level schema. The new copy's WORKING_USER and
     * USER_MODIFIED will be set to the given user name. The new copy's FILENAME will be set to the given new file name, unless the
     * latter is null, in which case the new copy's FILENAME shall remain the same as original's.
     *
     * If the given file name (i.e. the new file name) is null, the new copy's CHECKEDOUT_COPY_ID is set to the original's id,
     * otherwise it is set to null. This is basically an assumption that if there will be a new file name, it is not a check-out
     * operation, therefore CHECKEDOUT_COPY_ID is irrelevant.
     *
     * The method returns the new copy's auto-generated id.
     *
     * @param schemaSetId
     * @param userName
     * @param newIdentifier
     * @param regStatus
     */
    int copySchemaRow(int schemaId, String userName, String newFileName, SchemaSet.RegStatus regStatus);

    /**
     * Returns the list of root-level schemas matching the given continuity id, excluding the ones that are working copies and the
     * one that match the given integer inputs. The method may choose to hide certain versions based on the permissions of the
     * given user.
     *
     * @param userName
     * @param continuityId
     * @param excludeIds
     *
     * @return
     * @throws ServiceException
     */
    List<Schema> getSchemaVersions(String userName, String continuityId, int... excludeIds);

    /**
     * Returns true if the given schema exists within the given schema set. Otherwise returns false.
     * The schema is given by its file name, while the schema set is given by its auto-generated id.
     * If the latter <=0, the schema is considered to be a root-level schema.
     *
     * @param fileName
     * @param schemaSetId
     * @return
     */
    boolean schemaExists(String fileName, int schemaSetId);
}
