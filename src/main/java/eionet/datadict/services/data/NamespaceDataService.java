package eionet.datadict.services.data;

import eionet.datadict.model.Namespace;
import java.util.List;

public interface NamespaceDataService {

    List<Namespace> getAttributeNamespaces();
    
}
