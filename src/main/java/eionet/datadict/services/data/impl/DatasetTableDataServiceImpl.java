package eionet.datadict.services.data.impl;

import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.meta.DDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetTableDataServiceImpl implements DatasetTableDataService {

    public final DatasetTableDao datasetTableDao;
    public final DatasetDao datasetDao;
    
    @Autowired
    public DatasetTableDataServiceImpl(DatasetTableDao datasetTableDao, DatasetDao datasetDao) {
        this.datasetTableDao = datasetTableDao;
        this.datasetDao = datasetDao;
    }
    
    @Override
    public DatasetTable getDatasetTable(int id) throws ResourceNotFoundException {
        DatasetTable datasetTable = datasetTableDao.getById(id);
        if (datasetTable!= null) {
            return datasetTable;
        } else {
            throw new ResourceNotFoundException("Table with id: "+Integer.toString(id)+ " does not exist.");
        }
    }

    @Override
    public boolean isWorkingCopy(DatasetTable table, DDUser user) {
        if (user == null) return false;
        
        if (table.getWorkingCopy() != null && table.getWorkingCopy() && table.getWorkingUser() != null && table.getWorkingUser().equals(user.getUserName())){
            return true;
        }
        
        Integer parentDatasetId = this.datasetTableDao.getParentDatasetId(table.getId());
        DataSet parentDataset = this.datasetDao.getById(parentDatasetId);
        return (parentDataset.getWorkingCopy() && parentDataset.getWorkingUser() != null && parentDataset.getWorkingUser().equals(user.getUserName()));
    }
    
}
