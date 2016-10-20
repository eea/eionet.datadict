package eionet.datadict.dal;

import eionet.datadict.model.DataSet;

public interface DataSetDao {

    DataSet getDataSetById(int dataSetId);
    
}
