package eionet.meta.dao.mysql;

import eionet.meta.SchemaSet;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.SchemaSetDAO;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class MySQLSchemaSetDAO extends MySQLBaseDAO implements SchemaSetDAO{

    /*
     * (non-Javadoc)
     * @see eionet.meta.dao.SchemaSetDAO#add(eionet.meta.SchemaSet)
     */
    @Override
    public int add(SchemaSet schemaSet) throws DAOException {

        if (schemaSet == null){
            throw new IllegalArgumentException();
        }
        return 0;
    }
}
