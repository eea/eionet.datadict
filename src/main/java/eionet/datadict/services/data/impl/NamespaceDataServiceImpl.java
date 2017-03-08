package eionet.datadict.services.data.impl;

import eionet.datadict.dal.NamespaceDao;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.data.NamespaceDataService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceDataServiceImpl implements NamespaceDataService {

    private final NamespaceDao namespaceDao;
    
    @Autowired
    public NamespaceDataServiceImpl(NamespaceDao namespaceDao) {
        this.namespaceDao = namespaceDao;
    }
    
    @Override
    public List<Namespace> getAttributeNamespaces() {
        return this.namespaceDao.getAttributeNamespaces();
    }
    
}
