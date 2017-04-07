package eionet.datadict.services.impl;

import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetTableService;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetTableServiceImpl implements DataSetTableService{

    @Override
    public Document getDataSetTableXMLSchema(int id) throws XmlExportException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
