package eionet.datadict.services.data;

import eionet.datadict.model.RdfNamespace;
import java.util.List;

public interface RdfNamespaceDataService {

    /**
     * Fetches all the rdf namespaces.
     * 
     * @return a {@link List} of all the {@link RdfNamespace} objects.
     */
    List<RdfNamespace> getRdfNamespaces();
    
}
