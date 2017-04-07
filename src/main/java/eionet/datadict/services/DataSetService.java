package eionet.datadict.services;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.DataSet;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetService {

    
   Document getDataSetXMLSchema(int id) throws  XmlExportException;    
   
   Document getDataSetXMLInstance(int id) throws XmlExportException;
   
   Document getDataSetXMLInstanceWithNS(int id) throws XmlExportException;
     /**
     * Fetches the dataset with the given id.
     * 
     * @param id the id of the dataset to be fetched.
     * @return the {@link Dataset} with the given id.
     * 
     * @throws ResourceNotFoundException 
     */
    public DataSet getDataset(int id) throws ResourceNotFoundException;
}
