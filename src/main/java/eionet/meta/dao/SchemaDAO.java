package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.Schema;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public interface SchemaDAO extends DAO{

    /**
     * 
     * @param schema
     * @return
     * @throws DAOException
     */
    int add(Schema schema) throws DAOException;

    /**
     * 
     * @param schemaSetId
     * @return
     * @throws DAOException
     */
    List<Schema> listForSchemaSet(int schemaSetId) throws DAOException;
}
