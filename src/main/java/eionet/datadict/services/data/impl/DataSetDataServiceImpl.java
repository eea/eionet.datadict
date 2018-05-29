package eionet.datadict.services.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
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

    @Override
    public DataSet getDatasetWithoutRelations(int dataSetId) {
        return datasetDao.getById(dataSetId);
    }

    @Override
    public void update(DataSet dataSet) {
        datasetDao.updateDataSet(dataSet);
    }

    @Override
    public void updateDatasetDispDownloadLinks(int dataSetId, DataSet.DISPLAY_DOWNLOAD_LINKS linkInfo) {
        DataSet dataSet = this.datasetDao.getById(dataSetId);
        //case when it is the first time that the new  mechanism for Display DownloadLinks is used for this Dataset
        if (dataSet.getSerializedDisplayDownloadLinks() == null || dataSet.getSerializedDisplayDownloadLinks().isEmpty()) {
            Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> defaultValues = new LinkedHashMap<>();
            for (DataSet.DISPLAY_DOWNLOAD_LINKS value : DataSet.DISPLAY_DOWNLOAD_LINKS.values()) {
                defaultValues.put(value, true);
            }
            defaultValues.put(linkInfo, linkInfo.getValue().equals("true") ? true : false);
            datasetDao.updateDataSetDispDownloadLinks(dataSetId, this.serializeDatasetDisplayDownloadLinks(defaultValues));
            return;
        }
        Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> deserializedResults = this.deserializeDatasetDisplayDownloadLinks(dataSet.getSerializedDisplayDownloadLinks());
        deserializedResults.put(linkInfo, linkInfo.getValue().equals("true") ? true : false);
        datasetDao.updateDataSetDispDownloadLinks(dataSetId, this.serializeDatasetDisplayDownloadLinks(deserializedResults));
    }

    @Override
    public Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> deserializeDatasetDisplayDownloadLinks(String serializedDisplayDownloadLinks) {
        Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> parsedResults = new LinkedHashMap<>();
        //initialize map with default values  
        for (DataSet.DISPLAY_DOWNLOAD_LINKS value : DataSet.DISPLAY_DOWNLOAD_LINKS.values()) {
            parsedResults.put(value, true);
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rawResults = mapper.readValue(serializedDisplayDownloadLinks, Map.class);
            for (DataSet.DISPLAY_DOWNLOAD_LINKS value : DataSet.DISPLAY_DOWNLOAD_LINKS.values()) {
                Object res = rawResults.get(value.name());
                if (res != null) {
                    parsedResults.put(value, (Boolean) res);
                }
            }
        } catch (IOException ex) {
            throw new SerializationFailedException("failed to Deserialize :" + serializedDisplayDownloadLinks, ex);
        }
        return parsedResults;
    }

    @Override
    public String serializeDatasetDisplayDownloadLinks(Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> deserializedDisplayDownloadLinks) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(deserializedDisplayDownloadLinks);
        } catch (IOException ex) {
            throw new SerializationFailedException("failed to Serialize :" + deserializedDisplayDownloadLinks, ex);

        }
    }

}
