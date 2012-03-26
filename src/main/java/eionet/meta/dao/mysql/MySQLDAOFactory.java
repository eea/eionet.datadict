package eionet.meta.dao.mysql;

import java.util.HashMap;
import java.util.Map;

import eionet.meta.dao.DAO;
import eionet.meta.dao.DAOFactory;
import eionet.meta.dao.SchemaSetDAO;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class MySQLDAOFactory extends DAOFactory{

    /** */
    private Map<Class<? extends DAO>, Class<? extends MySQLBaseDAO>> daoImplementations;

    /**
     * 
     */
    public MySQLDAOFactory(){

        daoImplementations = new HashMap<Class<? extends DAO>, Class<? extends MySQLBaseDAO>>();
        daoImplementations.put(SchemaSetDAO.class, MySQLSchemaSetDAO.class);
    }

    /**
     * @see eionet.meta.dao.DAOFactory#createDao(java.lang.Class)
     */
    @Override
    public <T extends DAO> T createDao(Class<T> implementedInterface) {

        try {
            Class<? extends MySQLBaseDAO> implClass = daoImplementations.get(implementedInterface);
            if (implClass != null) {
                MySQLBaseDAO newInstance = implClass.newInstance();
                return (T)newInstance;
            } else {
                return null;
            }
        } catch (Exception fatal) {
            throw new RuntimeException(fatal);
        }
    }

}
