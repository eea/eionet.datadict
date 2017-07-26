package eionet.datadict.services.data.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.orm.OrmUtils;
import eionet.datadict.services.data.DataSetDataService;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class DataSetDataServiceImpl implements DataSetDataService {

    private final DatasetDao datasetDao;
    private final DatasetTableDao datasetTableDao;
    private final AttributeValueDao attributeValueDao;
    private final AttributeDao attributeDao;
    private final DataElementDao dataElementDao;

    @Autowired
    public DataSetDataServiceImpl(DatasetDao datasetDao, DatasetTableDao datasetTableDao, AttributeValueDao attributeValueDao, AttributeDao attributeDao, DataElementDao dataElementDao) {
        this.datasetDao = datasetDao;
        this.datasetTableDao = datasetTableDao;
        this.attributeValueDao = attributeValueDao;
        this.attributeDao = attributeDao;
        this.dataElementDao = dataElementDao;
    }

    @Override
    public DataSet getFullDataSetDefinition(int dataSetId) {
        DataSet dataset = datasetDao.getById(dataSetId);
        List<DatasetTable> dsTables = datasetTableDao.getAllByDatasetId(dataset.getId());
        dataset.setDatasetTables(new HashSet<DatasetTable>(dsTables));
        OrmUtils.link(dataset, dsTables);
        List<AttributeValue> attributeValues = attributeValueDao.getByOwner(new DataDictEntity(dataset.getId(), DataDictEntity.Entity.DS));
        
        return dataset;
    }

}
