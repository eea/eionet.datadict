package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.Schema;
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
}
