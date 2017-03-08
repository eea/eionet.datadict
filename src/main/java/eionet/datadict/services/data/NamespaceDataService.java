package eionet.datadict.services.data;

import eionet.datadict.model.Namespace;
import java.util.List;

public interface NamespaceDataService {

    /**
     * Fetches all the attribute namespaces.
     * 
     * @return a {@link List} of the {@link Namespace} objects of the attributes. 
     */
    List<Namespace> getAttributeNamespaces();
    
}
