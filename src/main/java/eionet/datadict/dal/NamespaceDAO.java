package eionet.datadict.dal;

import eionet.datadict.model.Namespace;
import java.util.List;

/**
 *
 * @author exorx-alk
 */
public interface NamespaceDAO {

    /**
     * Fetches the list of namespaces related to attributes
     * 
     * @return 
     */
    public List<Namespace> getAttributeNamespaces();
}
