package eionet.datadict.dal;

import eionet.datadict.model.DataSet;
import eionet.meta.dao.domain.DatasetRegStatus;
import java.util.List;

public interface DatasetDao {

    /**
     * Fetch the dataset with the given id.
     *
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     */
    public DataSet getById(int id);

    public void updateDataSet(DataSet dataSet);

    public void updateDataSetDispDownloadLinks(int id, String dispDownloadLinks);
    
    public List<DataSet> getDatasetsByIdentifierAndWorkingCopyAndRegStatuses(String datasetIdentifier, boolean workingCopy,List<DatasetRegStatus> statuses);
}
