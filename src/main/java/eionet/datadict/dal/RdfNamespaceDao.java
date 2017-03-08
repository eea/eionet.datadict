package eionet.datadict.dal;

import eionet.datadict.model.RdfNamespace;
import java.util.List;

/**
 *
 * @author Aliki Kopaneli
 */
public interface RdfNamespaceDao {
    
    /**
     * Fetches all RDF name-spaces.
     * 
     * @return a {@link java.util.List} containing {@link RdfNamespace} objects.
     */
    public List<RdfNamespace> getRdfNamespaces();
}
