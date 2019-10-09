package eionet.meta.dao;

import java.util.List;

import eionet.meta.DataElement;
import eionet.meta.dao.domain.RdfNamespace;

/**
 * RDF Namespace DAO operations.
 *
 * @author Kaido Laine
 */
public interface IRdfNamespaceDAO {
    /**
     * Checks if namespace exists.
     *
     * @param namespaceId
     *            namespace prefix
     * @return true if namespace is in the DB
     * @throws DAOException
     *             if query fails
     */
    boolean namespaceExists(String namespaceId) throws DAOException;

    /**
     * Checks if namespace exists.
     *
     * @param namespaceId
     *            namespace prefix
     * @return RDF Namespace object
     * @throws DAOException
     *             if query fails
     */
    RdfNamespace getNamespace(String namespaceId) throws DAOException;

    /**
     * returns list of namespace objects for dataelements.
     *
     * @param elements
     *            element set
     * @return array of namespace objects
     * @throws DAOException
     *             if query fails
     */
    List<RdfNamespace> getElementExternalNamespaces(List<DataElement> elements) throws DAOException;

    /**
     * Returns all rows in rdf namespace table.
     *
     * @return RDF Namespace objects
     * @throws DAOException
     *             if query fails
     */
    List<RdfNamespace> getRdfNamespaces() throws DAOException;
}
