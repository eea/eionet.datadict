package eionet.datadict.dal;

import eionet.datadict.model.DatasetTableElement;
import java.util.List;
import eionet.datadict.model.DataElement;

public interface DataElementDao {

    
    /**
     *Fetches all Data Elements which belong to the Dataset Table specified by the given id.
     * @param id the id of the dataset table whose data elements are to be fetched.
     * @return the {@list DataElement} for those data elements which belong to the Data table with the given id.
     **/
    List<DataElement> getDataElementsOfDatasetTableOrderByPositionAsc(int tableId);
    

    /**
     * Fetches the data element with the given id.
     * 
     * @param id the id of the data element to be fetched.
     * @return the {@link DataElement} with the specified id.
     */
    public DataElement getById(int id);
    
    
    /**
     * Fetches the id of the parent table of the data element with the given id.
     * 
     * @param elementId the id of the data element whose parent table is to be fetched.
     * @return an {@list Integer} corresponding to the id of the parent table.
     */
    public Integer getParentTableId(int elementId);
}
