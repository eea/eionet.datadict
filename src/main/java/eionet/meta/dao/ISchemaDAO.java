package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.Schema;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public interface ISchemaDAO{

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
     * 
     * @param schemaId
     * @param schemaSetId
     * @param fileName
     * @param userName
     * @return
     */
    int copyToSchemaSet(int schemaId, int schemaSetId, String fileName, String userName);
}
