package eionet.datadict.services.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.DataSetService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import eionet.datadict.model.Namespace;
import eionet.util.Props;
import eionet.util.PropsIF;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetServiceImpl implements DataSetService {

    private final DatasetDao datasetDao;
    private final DatasetTableDao datasetTableDao;
    private final AttributeValueDao attributeValueDao;
    private final AttributeDao attributeDao;

    private static final String DATASETS_NAMESPACE_ID = "1";
    private static final String ISOATTRS_NAMESPACE_ID = "2";
    private static final String DDATTRS_NAMESPACE_ID = "3";

    // BELOW Static variables should be moved to an utility class  , because they are common variables for many cases
    private static final String TARGET_NAMESPACE = "targetNamespace";
    private static final String NAMESPACE = "namespace";
    private static final String SCHEMA_LOCATION = "schemaLocation";
    private static final String TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME = "schema-tbl-";
    private static final String XSD_FILE_TYPE = ".xsd";
    private static final String ELEMENT = "element";
    private static final String ANNOTATION = "annotation";
    private static final String COMPLEX_TYPE = "complexType";
    private static final String SEQUENCE = "sequence";
    private static final String REF = "ref";
    private static final String DOCUMENTATION = "documentation";
    private static final String DEFAULT_XML_LANGUAGE = "en";

    protected String appContext = Props.getRequiredProperty(PropsIF.DD_URL);
    private final static String NS_PREFIX = "xs:";

    @Autowired
    public DataSetServiceImpl(DatasetDao datasetDao, DatasetTableDao datasetTableDao, AttributeValueDao attributeValueDao, AttributeDao attributeDao) {
        this.datasetDao = datasetDao;
        this.datasetTableDao = datasetTableDao;
        this.attributeValueDao = attributeValueDao;
        this.attributeDao = attributeDao;
    }

    @Override
    public Document getDataSetXMLSchema(String id) throws XmlExportException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, NS_PREFIX + "schema");
            schemaRoot.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", "http://www.w3.org/2001/XMLSchema http://www.w3.org/2001/XMLSchema.xsd");
            schemaRoot.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
            schemaRoot.setAttribute("xmlns:datasets", appContext + "/" + Namespace.URL_PREFIX + "/" + DATASETS_NAMESPACE_ID);
            schemaRoot.setAttribute("xmlns:isoattrs", appContext + "/" + Namespace.URL_PREFIX + "/" + ISOATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute("xmlns:ddattrs", appContext + "/" + Namespace.URL_PREFIX + "/" + DDATTRS_NAMESPACE_ID);
            DataSet dataset = this.getDataset(Integer.parseInt(id));
            schemaRoot.setAttribute(TARGET_NAMESPACE, appContext + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            List<DatasetTable> dsTables = datasetTableDao.getAllByDatasetId(dataset.getId());
            for (DatasetTable dsTable : dsTables) {
                schemaRoot.setAttribute("xmlns:dd" + dsTable.getCorrespondingNS().getId(), appContext + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId());
                Element importElement = doc.createElement(NS_PREFIX + "import");
                importElement.setAttribute(NAMESPACE, appContext + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId());
                importElement.setAttribute(SCHEMA_LOCATION, TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dsTable.getId() + XSD_FILE_TYPE);
                schemaRoot.appendChild(importElement);
            }
            Element element = doc.createElement(NS_PREFIX + ELEMENT);
            element.setAttribute("name", dataset.getIdentifier());
            schemaRoot.appendChild(element);
            Element annotation = doc.createElement(NS_PREFIX + ANNOTATION);
            element.appendChild(annotation);
            Element documentation = doc.createElement(NS_PREFIX + DOCUMENTATION);
            documentation.setAttribute("xml:lang", DEFAULT_XML_LANGUAGE);
            annotation.appendChild(documentation);
            List<AttributeValue> attributeValues = attributeValueDao.getByOwner(new DataDictEntity(dataset.getId(), DataDictEntity.Entity.DS));
            for (AttributeValue attributeValue : attributeValues) {
                Attribute attribute = attributeDao.getById(attributeValue.getAttributeId());
                Element attributeElement = doc.createElement(attribute.getNamespace().getShortName().concat(":").replace("_", "").concat(attribute.getShortName()).replace(" ", ""));
                attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                documentation.appendChild(attributeElement);
            }
            Element complexType = doc.createElement(NS_PREFIX + COMPLEX_TYPE);
            element.appendChild(complexType);
            Element sequence = doc.createElement(NS_PREFIX + SEQUENCE);
            complexType.appendChild(sequence);
            for (DatasetTable dsTable : dsTables) {
                Element tableElement = doc.createElement(NS_PREFIX + ELEMENT);
                tableElement.setAttribute(REF, "dd".concat(dsTable.getCorrespondingNS().getId().toString()).concat(":").concat(dsTable.getShortName()));
                tableElement.setAttribute("minOccurs", "1");
                tableElement.setAttribute("maxOccurs", "1");
                sequence.appendChild(tableElement);
            }
            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        } catch (ResourceNotFoundException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public DataSet getDataset(int id) throws ResourceNotFoundException {
        DataSet dataset = datasetDao.getById(id);
        if (dataset != null) {
            return dataset;
        } else {
            throw new ResourceNotFoundException("Dataset with id: " + Integer.toString(id) + " does not exist.");
        }
    }

    protected String getNamespacePrefix(Namespace ns) {
        return ns == null ? "dd" : "dd" + ns.getId();
    }
}
