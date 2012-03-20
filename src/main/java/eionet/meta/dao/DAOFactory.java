package eionet.meta.dao;

import eionet.meta.dao.mysql.MySQLDAOFactory;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public abstract class DAOFactory {

    /** */
    public enum FactoryType {
        MYSQL
    }

    /** */
    private static final FactoryType DEFAULT_FACTORY_TYPE = FactoryType.MYSQL;

    /** */
    private static DAOFactory instance;

    /**
     * 
     * @return
     */
    public static DAOFactory get() {

        if (instance == null) {
            instance = createDAOFactory(DEFAULT_FACTORY_TYPE);
        }
        return instance;
    }

    /**
     * 
     * @param factoryType
     * @return
     */
    private static DAOFactory createDAOFactory(FactoryType factoryType) {

        if (factoryType.equals(FactoryType.MYSQL)) {
            return new MySQLDAOFactory();
        } else {
            throw new IllegalArgumentException("Unknown factory type: " + factoryType);
        }
    }

    /**
     * 
     * @param <T>
     * @param implementedInterface
     * @return
     */
    public abstract <T extends DAO> T getDao(Class<T> implementedInterface);
}
