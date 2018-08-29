package eionet.datadict.services;

import eionet.datadict.errors.IllegalParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import org.apache.jena.rdf.model.Model;
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
    
    /**
     *
     * @return an Apache Jena Model Class with many utility methods to send the RDF as output to many formats.
     **/
    public Model getDatasetRdf(int datasetId) throws ResourceNotFoundException;
}
