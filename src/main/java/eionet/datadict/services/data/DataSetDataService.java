package eionet.datadict.services.data;

import eionet.datadict.model.DataSet;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetDataService {

    DataSet getFullDataSetDefinition(int dataSetId);

}
