package eionet.meta.dao;

import eionet.meta.dao.domain.SchemaSet;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public interface SchemaSetDAO extends DAO{

    /**
     * 
     * @param schemaSet
     * @return
     * @throws DAOException
     */
    int add(SchemaSet schemaSet) throws DAOException;

    /**
     * 
     * @param identifier
     * @return
     * @throws DAOException
     */
    SchemaSet getByIdentifier(String identifier) throws DAOException;

    /**
     * 
     * @param id
     * @return
     * @throws DAOException
     */
    SchemaSet getById(int id) throws DAOException;
}
