package eionet.datadict.services.data.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.orm.OrmCollectionUtils;
import eionet.datadict.orm.OrmUtils;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.meta.DDUser;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetTableDataServiceImpl implements DatasetTableDataService {

    private final DatasetTableDao datasetTableDao;
    private final DatasetDao datasetDao;
    private final AttributeDao attributeDao;
    private final DataElementDao dataElementDao;
    private final AttributeValueDao attributeValueDao;

    @Autowired
    public DatasetTableDataServiceImpl(DatasetTableDao datasetTableDao, DatasetDao datasetDao, AttributeDao attributeDao, DataElementDao dataElementDao, AttributeValueDao attributeValueDao) {
        this.datasetTableDao = datasetTableDao;
        this.datasetDao = datasetDao;
        this.attributeDao = attributeDao;
        this.dataElementDao = dataElementDao;
        this.attributeValueDao = attributeValueDao;
    }

    @Override
    public DatasetTable getDatasetTable(int id) throws ResourceNotFoundException {
        DatasetTable datasetTable = datasetTableDao.getById(id);
        if (datasetTable != null) {
            return datasetTable;
        } else {
            throw new ResourceNotFoundException("Table with id: " + Integer.toString(id) + " does not exist.");
        }
    }

    @Override
    public boolean isWorkingCopy(DatasetTable table, DDUser user) {
        if (user == null) {
            return false;
        }

        if (table.getWorkingCopy() != null && table.getWorkingCopy() && table.getWorkingUser() != null && table.getWorkingUser().equals(user.getUserName())) {
            return true;
        }

        Integer parentDatasetId = this.datasetTableDao.getParentDatasetId(table.getId());
        DataSet parentDataset = this.datasetDao.getById(parentDatasetId);
        return (parentDataset.getWorkingCopy() && parentDataset.getWorkingUser() != null && parentDataset.getWorkingUser().equals(user.getUserName()));
    }

    @Override
    public DatasetTable getFullDatasetTableDefinition(int tableId) throws ResourceNotFoundException {
        DatasetTable datasetTable = this.datasetTableDao.getById(tableId);

        if (datasetTable == null) {
            throw new ResourceNotFoundException(String.format("Table with id %d could not be found.", tableId));
        }
        Integer datasetId = this.datasetTableDao.getParentDatasetId(tableId);

        DataSet dataSet = datasetDao.getById(datasetId);
        datasetTable.setDataSet(dataSet);
        if (dataSet == null) {
            throw new ResourceNotFoundException(String.format("Dataset with id %d could not be found", datasetTable.getDataSet().getId()));
        }
        OrmUtils.link(dataSet, datasetTable);

        List<DataElement> dataElements = this.dataElementDao.getDataElementsOfDatasetTable(datasetTable.getId());
        datasetTable.setDataElements(OrmCollectionUtils.createChildCollection(dataElements));
        OrmUtils.link(datasetTable, dataElements);

        List<Attribute> dataSetTableAttributes = attributeDao.getByDataDictEntity(new DataDictEntity(datasetTable.getId(), DataDictEntity.Entity.T));

        datasetTable.setAttributes(OrmCollectionUtils.createChildCollection(dataSetTableAttributes));

        List<AttributeValue> datasetTableAttributeValues = attributeValueDao.getByOwner(new DataDictEntity(datasetTable.getId(), DataDictEntity.Entity.T));

        OrmUtils.link(dataSetTableAttributes, datasetTableAttributeValues);
        datasetTable.setAttributesValues(OrmCollectionUtils.createChildCollection(datasetTableAttributeValues));
      //  OrmUtils.link(datasetTable, datasetTableAttributeValues);
        return datasetTable;
    }

}
