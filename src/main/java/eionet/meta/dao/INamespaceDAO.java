package eionet.meta.dao;

import eionet.meta.service.data.NamespaceFilter;
import eionet.meta.service.data.NamespaceResult;

/**
 * Namespace DAO operations.
 *
 * @author enver
 */
public interface INamespaceDAO {

    /**
     * Query all namespaces from NAMESPACE table ordered by short name.
     *
     * @param filter
     *            any filtering or search criteria.
     * @return list of all namespaces as a result set.
     * @throws eionet.meta.dao.DAOException
     *             when an error occurs.
     */
    NamespaceResult getNamespaces(NamespaceFilter filter) throws DAOException;
}
