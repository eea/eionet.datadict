package eionet.meta.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eionet.meta.dao.DAOException;
import eionet.meta.dao.INamespaceDAO;
import eionet.meta.dao.IRdfNamespaceDAO;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.service.data.NamespaceFilter;
import eionet.meta.service.data.NamespaceResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Namespace service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class NamespaceServiceImpl implements INamespaceService {

    /** Rdf namespace DAO. */
    @Autowired
    private IRdfNamespaceDAO rdfNamespaceDAO;

    /** Namespace DAO. */
    @Autowired
    private INamespaceDAO namespaceDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public NamespaceResult getNamespaces(NamespaceFilter filter) throws ServiceException {
        try {
            return namespaceDAO.getNamespaces(filter);
        } catch (DAOException daoe) {
            throw new ServiceException("Failed to get namespaces " + daoe.getMessage(), daoe);
        }
    } // end of method getNamespaces

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfNamespace> getRdfNamespaces() throws ServiceException {
        try {
            return rdfNamespaceDAO.getRdfNamespaces();
        } catch (DAOException daoe) {
            throw new ServiceException("Failed to get RDF namespaces " + daoe.getMessage(), daoe);
        }
    } // end of method getRdfNamespaces
} // end of class NamespaceServiceImpl
