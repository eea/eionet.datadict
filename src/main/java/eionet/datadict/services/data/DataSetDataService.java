package eionet.datadict.services.data;

import eionet.datadict.model.DataSet;

import java.util.Map;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetDataService {

    /**
     * Fetches the dataset with the given id along with its related entities
     *
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     *
     * @throws ResourceNotFoundException
     */
    DataSet getFullDataSetDefinition(int dataSetId);

    /**
     * Fetches the dataset with the given id without fetching the related
     * entities
     *
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     *
     * @throws ResourceNotFoundException
     */
    DataSet getDatasetWithoutRelations(int dataSetId);

    void update(DataSet dataSet);

    void updateDatasetDispDownloadLinks(int dataSetId, DataSet.DISPLAY_DOWNLOAD_LINKS linkInfo);

    Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> deserializeDatasetDisplayDownloadLinks(String serializedDisplayDownloadLinks);

    String serializeDatasetDisplayDownloadLinks(Map<DataSet.DISPLAY_DOWNLOAD_LINKS, Boolean> deserializedDisplayDownloadLinks);
}
