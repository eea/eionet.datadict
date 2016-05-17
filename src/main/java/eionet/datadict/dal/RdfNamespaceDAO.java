package eionet.datadict.dal;

import eionet.datadict.model.RdfNamespace;
import java.util.List;

/**
 *
 * @author exorx-alk
 */
public interface RdfNamespaceDAO {
    public RdfNamespace getRdfNamespaceById(int id);
    public List<RdfNamespace> getRdfNamespaces();
}
