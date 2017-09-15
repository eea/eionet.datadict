package eionet.datadict.services;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetService {

    
   Document getDataSetXMLSchema(int id) throws  XmlExportException , ResourceNotFoundException;    
   
   Document getDataSetXMLInstance(int id) throws XmlExportException ,  ResourceNotFoundException;
   
   Document getDataSetXMLInstanceWithNS(int id) throws XmlExportException;
    
}
