package eionet.datadict.services;

import eionet.datadict.errors.XmlExportException;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetTableService {

    Document getDataSetTableXMLSchema(int id) throws XmlExportException;
    
    Document getDataSetTableXMLInstance(int id) throws XmlExportException;
}
