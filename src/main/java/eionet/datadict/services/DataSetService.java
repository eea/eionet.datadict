package eionet.datadict.services;

import eionet.datadict.errors.IllegalParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import org.w3c.dom.Document;


/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface DataSetService {

    Document getDataSetXMLSchema(int datasetId) throws XmlExportException, ResourceNotFoundException;

    Document getDataSetXMLInstance(int datasetId) throws XmlExportException, ResourceNotFoundException;

    Document getDataSetXMLInstanceWithNS(int datasetId) throws XmlExportException;

    public void updateDatasetDisplayDownloadLinks(int datasetId, String dispDownloadLinkType, String dispDownloadLinkValue) throws IllegalParameterException;
}
