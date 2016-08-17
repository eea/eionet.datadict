package eionet.datadict.services.impl.data;

import eionet.datadict.dal.RdfNamespaceDao;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.services.data.RdfNamespaceDataService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RdfNamespaceDataServiceImpl implements RdfNamespaceDataService {

    private final RdfNamespaceDao rdfNamespaceDao;

    @Autowired
    public RdfNamespaceDataServiceImpl(RdfNamespaceDao rdfNamespaceDao) {
        this.rdfNamespaceDao = rdfNamespaceDao;
    }
    
    @Override
    public List<RdfNamespace> getRdfNamespaces() {
        return this.rdfNamespaceDao.getRdfNamespaces();
    }
    
}
