package eionet.datadict.services.data.impl;

import eionet.datadict.dal.DatasetDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Dataset;
import eionet.datadict.services.data.DatasetDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetDataServiceImpl implements DatasetDataService {

    public final DatasetDao datasetDao;
    
    @Autowired
    public DatasetDataServiceImpl(DatasetDao datasetDao) {
        this.datasetDao = datasetDao;
    }
    
    @Override
    public Dataset getDataset(int id) throws ResourceNotFoundException {
        Dataset dataset = datasetDao.getById(id);
        if (dataset!=null){
            return dataset;
        }
        else{
            throw new ResourceNotFoundException("Dataset with id: "+Integer.toString(id)+ " does not exist.");
        }
    }
    
}
