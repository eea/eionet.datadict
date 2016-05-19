package eionet.datadict.dal;

import eionet.datadict.model.RdfNamespace;
import java.util.List;

/**
 *
 * @author Aliki Kopaneli
 */
public interface RdfNamespaceDAO {
    
    /**
     * Fetches all entries of the T_RDF_NAMESPACE table
     * 
     * @return 
     */
    public List<RdfNamespace> getRdfNamespaces();
}
