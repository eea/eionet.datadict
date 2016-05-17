package eionet.datadict.dal;

import eionet.datadict.model.Namespace;
import java.util.List;

/**
 *
 * @author exorx-alk
 */
public interface NamespaceDAO {

    public List<Namespace> getAttributeNamespaces();
}
