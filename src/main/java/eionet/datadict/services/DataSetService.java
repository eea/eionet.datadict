package eionet.datadict.services;

import eionet.datadict.errors.XmlExportException;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetService {

   Document getDataSetXMLSchema(String id) throws  XmlExportException;    
}
