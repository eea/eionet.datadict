package eionet.datadict.dal;

import eionet.datadict.model.Namespace;
import java.util.List;


public interface NamespaceDao {

    /**
     * Fetches the list of namespaces related to attributes.
     * 
     * @return a {@link java.util.List} containing the fetched {@link Namespace} objects.
     */
    public List<Namespace> getAttributeNamespaces();
}
