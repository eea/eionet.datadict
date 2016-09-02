package eionet.datadict.services.impl.data;

import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.Dataset;
import eionet.datadict.services.data.DataElementDataService;
import eionet.meta.DDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataElementDataServiceImpl implements DataElementDataService{

    public final DataElementDao dataElementDao;
    public final DatasetDao datasetDao;
    public final DatasetTableDao datasetTableDao;
    
    @Autowired
    public DataElementDataServiceImpl(DataElementDao dataElementDao, DatasetDao datasetDao, DatasetTableDao datasetTableDao) {
        this.dataElementDao = dataElementDao;
        this.datasetDao = datasetDao;
        this.datasetTableDao = datasetTableDao;
    }
    
    @Override
    public DataElement getDataElement(int dataElementId) throws ResourceNotFoundException {
        DataElement dataElement = this.dataElementDao.getById(dataElementId);
        if (dataElement!=null) {
            return dataElement;
        } else {
            throw new ResourceNotFoundException("Data element with id: "+dataElementId+" does not exist.");
        }
    }

    @Override
    public boolean isWorkingUser(DataElement dataElement, DDUser user) {
        if (user == null) return false;
        
        //common dataelement
        if (dataElement.getWorkingCopy()!= null && dataElement.getWorkingCopy() && dataElement.getWorkingUser()!= null && dataElement.getWorkingUser().equals(user.getUserName())) {
            return true;
        }
        
        //non-common dataelement
        Integer parentTableId = this.dataElementDao.getParentTableId(dataElement.getId());
        Integer parentDatasetId = this.datasetTableDao.getParentDatasetId(parentTableId);
        Dataset parentDataset = this.datasetDao.getById(parentDatasetId);
        return (parentDataset.getWorkingCopy() && parentDataset.getWorkingUser() != null && parentDataset.getWorkingUser().equals(user.getUserName()));
    }
    
    
}
